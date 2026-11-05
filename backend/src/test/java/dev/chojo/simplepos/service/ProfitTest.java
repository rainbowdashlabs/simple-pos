package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.PurchasePart;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.entity.dto.InboundStorage;
import dev.chojo.simplepos.entity.dto.InventoryCorrection;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.PurchasePartRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static dev.chojo.simplepos.MoneyAssertions.assertMoney;
import static dev.chojo.simplepos.MoneyAssertions.assertUnitPrice;
import static dev.chojo.simplepos.MoneyAssertions.money;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * What sales cost. A delivery known only by its total, which does not divide into cents per unit, costs exactly what
 * was paid once it is sold out; units sold without stock and stock found in an inventory check are valued at the
 * ingredient's current price.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProfitTest {
    @Autowired
    StorageService storageService;
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    ProductService productService;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PurchaseRepository purchaseRepository;
    @Autowired
    EntityManager entityManager;
    @Autowired
    PurchasePartRepository purchasePartRepository;
    @Autowired
    StorageRepository storageRepository;

    @Test
    void sellingABatchCostsExactlyItsTotal() {
        Category category = categoryRepository.save(new Category(null, "ProfitCat"));
        Ingredient bottle = ingredientRepository.save(new Ingredient(null, category, "Bottle", 1.0, 12, 0, 0, 0));
        Product product = productService.create(new Product(null, category, "Lemonade", 2.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, bottle, 1)))));
        Account account = accountRepository.save(new Account(null, "Buyer", Instant.now()));
        User staff = userRepository.save(new User(null, "profitstaff", "x", true, Role.STAFF));

        Storage batch = storageService.deliver(new InboundStorage(bottle, null, null, money(10.52), money(0.0), 12), staff);
        assertUnitPrice(0.8767, batch.getPrice());

        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product, 12)));
        entityManager.flush();

        Tuple summary = purchaseRepository.getSalesStatSummary(product.getId(), Instant.now().minus(1, ChronoUnit.HOURS));
        assertMoney(24.0, Money.fromQuery((Number) summary.get("revenue")));
        assertMoney(13.48, Money.fromQuery((Number) summary.get("profit")));
    }

    @Test
    void aSaleTakesWholeBatchesOldestFirstAndCostsMissingUnitsAtTheCurrentPrice() {
        Category category = categoryRepository.save(new Category(null, "BatchCat"));
        Ingredient can = ingredientRepository.save(new Ingredient(null, category, "Can", 0.5, 0, 0, 0, 0));
        Product product = productService.create(new Product(null, category, "Soda", 1.5, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, can, 1)))));
        Account account = accountRepository.save(new Account(null, "Batcher", Instant.now()));
        Instant delivered = Instant.now().minus(1, ChronoUnit.DAYS);
        Storage first = storageRepository.save(new Storage(null, can, delivered, 0.4, 3, 0));
        Storage second = storageRepository.save(new Storage(null, can, delivered, 0.3, 2, 0));
        entityManager.flush();

        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product, 7)));
        entityManager.flush();

        List<PurchasePart> parts = purchasePartRepository.findAll(Sort.by("id")).stream()
                .filter(part -> part.getIngredient().getId().equals(can.getId()))
                .toList();
        assertEquals(3, parts.size());
        assertEquals(first.getId(), parts.get(0).getStorage().getId(), "same delivery time: the batch with the lower id goes first");
        assertEquals(3, parts.get(0).getAmount());
        assertEquals(second.getId(), parts.get(1).getStorage().getId());
        assertEquals(2, parts.get(1).getAmount());
        assertMoney(1.2, parts.get(0).getCost());
        assertMoney(0.6, parts.get(1).getCost());
        assertNull(parts.get(2).getStorage());
        assertEquals(2, parts.get(2).getAmount());
        assertMoney(1.0, parts.get(2).getCost());
    }

    @Test
    void stockFoundInAnInventoryCheckIsValuedAtTheCurrentPrice() {
        Category category = categoryRepository.save(new Category(null, "FoundCat"));
        Ingredient chips = ingredientRepository.save(new Ingredient(null, category, "Chips", 1.25, 0, 0, 0, 0));
        entityManager.flush();

        storageService.processInventoryCorrection(List.of(new InventoryCorrection(chips.getId(), 4)));
        entityManager.flush();

        Storage found = storageRepository.findAll().stream()
                .filter(storage -> storage.getIngredient().getId().equals(chips.getId()))
                .findFirst().orElseThrow();
        assertMoney(5.0, found.getCost());
        assertUnitPrice(1.25, ingredientRepository.findById(chips.getId()).orElseThrow().getPrice());
    }
}
