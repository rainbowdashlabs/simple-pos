package dev.chojo.simplepos.service;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.controller.exceptions.ConflictException;
import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.PurchasePart;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.PurchasePartRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.service.storage.StorageTransaction;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class PurchaseService {
    private static final Logger log = getLogger(PurchaseService.class);
    private final StorageService storageService;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final AccountRepository accountRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchasePartRepository purchasePartRepository;

    public PurchaseService(StorageService storageService, ProductRepository productRepository, IngredientRepository ingredientRepository, PurchaseRepository purchaseRepository, PurchasePartRepository purchasePartRepository,
                           AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.storageService = storageService;
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.purchaseRepository = purchaseRepository;
        this.purchasePartRepository = purchasePartRepository;
    }

    /**
     * Books a purchase for every cart position and takes the ingredients of each product's recipe from storage.
     * <p>
     * Runs in one transaction: if any position names an unknown product, nothing is booked. A product whose
     * ingredients are out of stock is still sold; the shortfall is logged as a warning.
     *
     * <p>
     * The account is loaded only here, under its row lock, so an account closed meanwhile is seen as closed.
     *
     * @throws BadRequestException if the cart is empty or a position's amount is below 1
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if a position names an unknown or deleted
     *                                                                     product, or the account does not exist or
     *                                                                     is closed
     * @throws ConflictException if a product is not active
     */
    @Transactional
    public synchronized void purchase(int accountId, List<CartPositionDto> positions) {
        if (positions == null || positions.isEmpty()) throw new BadRequestException("The cart is empty");
        for (CartPositionDto position : positions) {
            if (position.amount() < 1) throw new BadRequestException("Every position needs an amount of at least 1");
        }
        Account account = accountRepository.requireNotDeletedLocked(accountId);
        List<Product> products = positions.stream()
                .map(position -> productRepository.requireNotDeleted(position.productId()))
                .toList();
        for (Product product : products) {
            if (!product.isActive()) throw new ConflictException("%s is not on sale".formatted(product.getName()));
        }
        ingredientRepository.requireLockedInOrder(products.stream()
                .flatMap(product -> product.getRecipe().getEntries().stream())
                .map(entry -> entry.getIngredient().getId())
                .toList());
        for (int index = 0; index < positions.size(); index++) {
            CartPositionDto position = positions.get(index);
            Product prod = products.get(index);
            Purchase purchase = new Purchase(null, account, prod, Instant.now(), position.amount(), prod.getPrice(), Collections.emptyList());
            purchase = purchaseRepository.save(purchase);
            ArrayList<PurchasePart> parts = new ArrayList<>();
            for (RecipeEntry recipe : prod.getRecipe().getEntries()) {
                int requested = recipe.getAmount() * position.amount();
                Collection<StorageTransaction> transactions = storageService.takeStorage(recipe.getIngredient(), requested);
                logShortfall(purchase, recipe, requested, transactions);
                for (StorageTransaction transaction : transactions) {
                    for (int i = 0; i < transaction.getAmount(); i++) {
                        parts.add(purchasePartRepository.save(new PurchasePart(null, purchase, transaction.getStorage())));
                    }
                }
            }
            purchase.setParts(parts);
            purchaseRepository.save(purchase);
        }
    }

    /**
     * Cancels a purchase: the account gets its money back, because its balance counts purchases, and the stock gets
     * its units back. Runs in one transaction under the same ingredient locks a purchase takes.
     *
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if the purchase does not exist
     * @throws ConflictException if the purchase belongs to a closed account, whose balance was already settled
     */
    @Transactional
    public synchronized void cancel(int id) {
        Integer accountId = purchaseRepository.findAccountIdOf(id).orElseThrow(() -> new NotFoundException("Purchase", id));
        if (accountRepository.requireLocked(accountId).isDeleted()) {
            throw new ConflictException("The purchase belongs to a closed account and cannot be cancelled");
        }
        ingredientRepository.requireLockedInOrder(purchasePartRepository.findIngredientIdsOf(id));
        Purchase purchase = purchaseRepository.findById(id).orElseThrow(() -> new NotFoundException("Purchase", id));
        List<PurchasePart> parts = purchasePartRepository.findAllByPurchase(purchase);
        for (PurchasePart part : parts) {
            Storage storage = part.getStorage();
            storage.setSold(Math.max(0, storage.getSold() - 1));
        }
        purchase.setParts(new ArrayList<>());
        purchasePartRepository.deleteAll(parts);
        purchaseRepository.delete(purchase);
    }

    private static void logShortfall(Purchase purchase, RecipeEntry recipe, int requested,
                                     Collection<StorageTransaction> transactions) {
        int taken = transactions.stream().mapToInt(StorageTransaction::getAmount).sum();
        if (taken >= requested) return;
        Product product = purchase.getProduct();
        log.warn("Purchase {} by account {} sold product {} ({}) with insufficient stock of ingredient {} ({}): took {} of {}",
                purchase.getId(), purchase.getAccount().getId(), product.getId(), product.getName(),
                recipe.getIngredient().getId(), recipe.getIngredient().getName(), taken, requested);
    }
}
