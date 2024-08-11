package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.PurchasePart;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.PurchasePartRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.service.storage.StorageTransaction;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseService {
    private final StorageService storageService;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchasePartRepository purchasePartRepository;

    public PurchaseService(StorageService storageService, ProductRepository productRepository, PurchaseRepository purchaseRepository, PurchasePartRepository purchasePartRepository) {
        this.storageService = storageService;
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
        this.purchasePartRepository = purchasePartRepository;
    }

    public synchronized void purchase(Account account, List<CartPositionDto> positions) {
        for (CartPositionDto position : positions) {
            Optional<Product> optProduct = productRepository.findById(position.product().getId());
            if (optProduct.isEmpty()) continue;
            optProduct.ifPresent(prod -> {
                Purchase purchase = new Purchase(null, account, prod, Instant.now(), position.amount(), prod.getPrice(), Collections.emptyList());
                purchase = purchaseRepository.save(purchase);
                ArrayList<PurchasePart> parts = new ArrayList<>();
                for (int x = 0; x < position.amount(); x++) {
                    for (RecipeEntry recipe : prod.getRecipe().getEntries()) {
                        Collection<StorageTransaction> transactions = storageService.takeStorage(recipe.getIngredient(), recipe.getAmount());
                        for (StorageTransaction transaction : transactions) {
                            for (int i = 0; i < transaction.getAmount(); i++) {
                                parts.add(purchasePartRepository.save(new PurchasePart(null, purchase, transaction.getStorage())));
                            }
                        }
                    }
                }
                purchase.setParts(parts);
                purchaseRepository.save(purchase);
            });
        }
    }
}
