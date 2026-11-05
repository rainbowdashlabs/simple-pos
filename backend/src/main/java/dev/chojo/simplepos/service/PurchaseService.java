package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.PurchasePart;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.exceptions.BadRequestException;
import dev.chojo.simplepos.exceptions.ConflictException;
import dev.chojo.simplepos.exceptions.NotFoundException;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.PurchasePartRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.service.storage.StorageTransaction;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The shape of carts (present fields, positive amounts, limits) is checked by the request validation at the API,
 * not here again; a new caller has to pass validated requests.
 */
@Service
public class PurchaseService {
    private static final Logger log = getLogger(PurchaseService.class);
    private static final int NOTE_LENGTH = 255;
    private final StorageService storageService;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final AccountRepository accountRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchasePartRepository purchasePartRepository;
    private final CashRepository cashRepository;

    public PurchaseService(StorageService storageService, ProductRepository productRepository, IngredientRepository ingredientRepository, PurchaseRepository purchaseRepository, PurchasePartRepository purchasePartRepository,
                           AccountRepository accountRepository, CashRepository cashRepository) {
        this.accountRepository = accountRepository;
        this.cashRepository = cashRepository;
        this.storageService = storageService;
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.purchaseRepository = purchaseRepository;
        this.purchasePartRepository = purchasePartRepository;
    }

    /**
     * Books a purchase for every cart position and takes the ingredients of each product's recipe from storage.
     * <p>
     * Every purchase records {@code soldBy}, the user at the till. Runs in one transaction: if any position names an
     * unknown product, nothing is booked. A product whose ingredients are out of stock is still sold; the shortfall
     * is logged as a warning and recorded as units sold without stock, costed at the ingredient's current price.
     *
     * <p>
     * The account is loaded only here, under its row lock, so an account closed meanwhile is seen as closed.
     *
     * @return the ids of the purchases booked, one per position, in the order of the positions
     * @throws BadRequestException if a position names no product; the cart's shape (not empty, amounts of at least 1)
     *                             is checked by the request validation
     * @throws NotFoundException if a position names an unknown or deleted product, or the account does not exist or is
     *                           closed
     * @throws ConflictException if a product is not active
     */
    @Transactional
    public List<Integer> purchase(int accountId, List<CartPositionDto> positions, User soldBy) {
        Account account = accountRepository.requireNotDeletedLocked(accountId);
        return book(account, positions, soldBy).stream().map(Purchase::getId).toList();
    }

    /**
     * Sells the cart to a guest who pays cash: the purchases are booked like {@link #purchase} but without an account,
     * and their total goes into the till as one {@code sale} cash entry, all in one transaction.
     *
     * @return the ids of the purchases booked, one per position, in the order of the positions
     * @throws BadRequestException if a position names no product; the cart's shape (not empty, amounts of at least 1)
     *                             is checked by the request validation
     * @throws NotFoundException if a position names an unknown or deleted product
     * @throws ConflictException if a product is not active
     */
    @Transactional
    public List<Integer> purchaseAsGuest(List<CartPositionDto> positions, User soldBy) {
        List<Purchase> purchases = book(null, positions, soldBy);
        BigDecimal total = purchases.stream().map(PurchaseService::totalOf).reduce(Money.ZERO, BigDecimal::add);
        cashRepository.save(new Cash(soldBy, total, CashType.SALE, note("Guest sale", purchases)));
        return purchases.stream().map(Purchase::getId).toList();
    }

    private List<Purchase> book(Account account, List<CartPositionDto> positions, User soldBy) {
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
        List<Purchase> booked = new ArrayList<>();
        for (int index = 0; index < positions.size(); index++) {
            CartPositionDto position = positions.get(index);
            Product prod = products.get(index);
            Purchase purchase = new Purchase(null, account, prod, Instant.now(), position.amount(), prod.getPrice(), Collections.emptyList());
            purchase.setSoldBy(soldBy);
            purchase = purchaseRepository.save(purchase);
            ArrayList<PurchasePart> parts = new ArrayList<>();
            for (RecipeEntry recipe : prod.getRecipe().getEntries()) {
                Ingredient ingredient = recipe.getIngredient();
                int requested = recipe.getAmount() * position.amount();
                List<StorageTransaction> transactions = storageService.takeStorage(ingredient, requested);
                for (StorageTransaction transaction : transactions) {
                    parts.add(purchasePartRepository.save(new PurchasePart(purchase, transaction.storage(), ingredient,
                            transaction.amount(), transaction.cost())));
                }
                int missing = requested - transactions.stream().mapToInt(StorageTransaction::amount).sum();
                if (missing > 0) {
                    logShortfall(purchase, recipe, requested, requested - missing);
                    BigDecimal cost = ingredient.getPrice().multiply(BigDecimal.valueOf(missing));
                    parts.add(purchasePartRepository.save(new PurchasePart(purchase, null, ingredient, missing, cost)));
                }
            }
            purchase.setParts(parts);
            purchase.setCost(parts.stream().map(PurchasePart::getCost).reduce(Money.ZERO, BigDecimal::add));
            booked.add(purchaseRepository.save(purchase));
        }
        return booked;
    }

    /**
     * Cancels a purchase: the account gets its money back, because its balance counts purchases, and the stock gets
     * its units back. A guest purchase is paid back out of the till instead, booked by {@code cancelledBy}. Runs in one
     * transaction under the same ingredient locks a purchase takes.
     *
     * @throws NotFoundException if the purchase does not exist
     * @throws ConflictException if the purchase belongs to a closed account, whose balance was already settled
     */
    @Transactional
    public void cancel(int id, User cancelledBy) {
        cancelAll(List.of(id), cancelledBy);
    }

    /**
     * Cancels several purchases, such as all positions of one sale, in one transaction: either all of them are
     * cancelled or none. The accounts are locked first, then all ingredients involved in ascending order, as a
     * purchase does, so a cancellation cannot deadlock with a sale.
     *
     * @throws BadRequestException if no purchase is named
     * @throws NotFoundException if a purchase does not exist
     * @throws ConflictException if a purchase belongs to a closed account, whose balance was already settled
     */
    @Transactional
    public void cancelAll(Collection<Integer> ids, User cancelledBy) {
        List<Integer> distinct = ids.stream().distinct().sorted().toList();
        if (distinct.isEmpty()) throw new BadRequestException("No purchase to cancel");
        Map<Integer, Integer> accountOf = new HashMap<>();
        for (Object[] row : purchaseRepository.findAccountIdsOf(distinct)) {
            accountOf.put((Integer) row[0], (Integer) row[1]);
        }
        for (int id : distinct) {
            if (!accountOf.containsKey(id)) throw new NotFoundException("Purchase", id);
        }
        List<Integer> accountIds = accountOf.values().stream().filter(Objects::nonNull).distinct().sorted().toList();
        for (int accountId : accountIds) {
            if (accountRepository.requireLocked(accountId).isDeleted()) {
                throw new ConflictException("The purchase belongs to a closed account and cannot be cancelled");
            }
        }
        ingredientRepository.requireLockedInOrder(distinct.stream()
                .flatMap(id -> purchasePartRepository.findIngredientIdsOf(id).stream())
                .toList());
        distinct.forEach(id -> cancelLocked(id, cancelledBy));
    }

    private void cancelLocked(int id, User cancelledBy) {
        Purchase purchase = purchaseRepository.findById(id).orElseThrow(() -> new NotFoundException("Purchase", id));
        if (purchase.getAccount() == null) {
            cashRepository.save(new Cash(cancelledBy, totalOf(purchase).negate(), CashType.SALE,
                    note("Guest sale cancelled", List.of(purchase))));
        }
        List<PurchasePart> parts = purchasePartRepository.findAllByPurchase(purchase);
        for (PurchasePart part : parts) {
            Storage storage = part.getStorage();
            if (storage != null) storage.setSold(Math.max(0, storage.getSold() - part.getAmount()));
        }
        purchasePartRepository.deleteAll(parts);
        purchaseRepository.delete(purchase);
    }

    private static BigDecimal totalOf(Purchase purchase) {
        return Money.of(purchase.getPrice().multiply(BigDecimal.valueOf(purchase.getAmount())));
    }

    private static String note(String prefix, List<Purchase> purchases) {
        String note = prefix + ": " + purchases.stream()
                .map(purchase -> "%d× %s".formatted(purchase.getAmount(), purchase.getProduct().getName()))
                .collect(Collectors.joining(", "));
        if (note.codePointCount(0, note.length()) <= NOTE_LENGTH) return note;
        return note.substring(0, note.offsetByCodePoints(0, NOTE_LENGTH - 1)) + "…";
    }

    private static void logShortfall(Purchase purchase, RecipeEntry recipe, int requested, int taken) {
        Product product = purchase.getProduct();
        Object buyer = purchase.getAccount() == null ? "guest" : purchase.getAccount().getId();
        log.warn("Purchase {} by account {} sold product {} ({}) with insufficient stock of ingredient {} ({}): took {} of {}",
                purchase.getId(), buyer, product.getId(), product.getName(),
                recipe.getIngredient().getId(), recipe.getIngredient().getName(), taken, requested);
    }
}
