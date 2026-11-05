package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.InboundStorage;
import dev.chojo.simplepos.entity.dto.InventoryCorrection;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.repository.UserRepository;
import dev.chojo.simplepos.service.storage.StorageTransaction;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.chojo.simplepos.MoneyAssertions.assertMoney;
import static dev.chojo.simplepos.MoneyAssertions.money;
import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StorageServiceTest {
    @Autowired
    StorageService storageService;
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    EntityManager entityManager;
    @Autowired
    CashRepository cashRepository;
    @Autowired
    UserRepository userRepository;

    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(new Category(null, "StorageServiceCat"));
        ingredient = ingredientRepository.save(
                new Ingredient(null, category, "StorageServiceIngr", 1.0, 24, 0, 0, 5));
    }

    @Test
    void shouldAddStorage() {
        Storage storage = storageService.addStorage(
                new Storage(null, ingredient, Instant.now(), 1.5, 24, 0));

        assertNotNull(storage.getId());
        assertEquals(24, storage.getAmount());
        assertEquals(0, storage.getSold());
    }

    @Test
    void shouldUpdateIngredientPriceWhenAddingStorageWithPrice() {
        entityManager.flush();
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 2.0, 10, 0));

        Ingredient updated = ingredientRepository.findById(ingredient.getId()).orElseThrow();
        assertMoney(2.0, updated.getPrice());
    }

    @Test
    void shouldNotUpdateIngredientPriceWhenPriceIsZero() {
        BigDecimal originalPrice = ingredient.getPrice();
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0, 10, 0));

        Ingredient found = ingredientRepository.findById(ingredient.getId()).orElseThrow();
        assertMoney(originalPrice, found.getPrice());
    }

    @Test
    void shouldTakeStorageFIFO() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now().minusSeconds(3600), 1.0, 5, 0));
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 1.5, 10, 0));
        entityManager.flush();

        Collection<StorageTransaction> transactions = storageService.takeStorage(ingredient, 3);

        assertFalse(transactions.isEmpty());
        int totalTaken = transactions.stream().mapToInt(StorageTransaction::amount).sum();
        assertEquals(3, totalTaken);
    }

    @Test
    void shouldTakeFromMultipleStorageBatches() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now().minusSeconds(3600), 1.0, 2, 0));
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 1.5, 5, 0));
        entityManager.flush();
        entityManager.clear();

        Ingredient freshIngredient = ingredientRepository.findById(ingredient.getId()).orElseThrow();
        Collection<StorageTransaction> transactions = storageService.takeStorage(freshIngredient, 4);

        int totalTaken = transactions.stream().mapToInt(StorageTransaction::amount).sum();
        assertEquals(4, totalTaken);
    }

    @Test
    void shouldHandleTakeMoreThanAvailable() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 1.0, 3, 0));
        entityManager.flush();

        Collection<StorageTransaction> transactions = storageService.takeStorage(ingredient, 5);

        int totalTaken = transactions.stream().mapToInt(StorageTransaction::amount).sum();
        assertEquals(3, totalTaken);
    }

    @Test
    void shouldTakeNothingFromEmptyStorage() {
        entityManager.flush();
        Collection<StorageTransaction> transactions = storageService.takeStorage(ingredient, 5);
        assertTrue(transactions.isEmpty());
    }

    @Test
    void shouldProcessInventoryCorrectionReduceStock() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 1.0, 10, 0));
        entityManager.flush();

        storageService.processInventoryCorrection(
                List.of(new InventoryCorrection(ingredient.getId(), 7)));

        entityManager.flush();
        StorageSummary summary = storageRepository.summaryById(ingredient.getId());
        assertEquals(7, summary.getStock());
    }

    @Test
    void shouldProcessInventoryCorrectionIncreaseStock() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 1.0, 5, 0));
        entityManager.flush();

        storageService.processInventoryCorrection(
                List.of(new InventoryCorrection(ingredient.getId(), 8)));

        entityManager.flush();
        StorageSummary summary = storageRepository.summaryById(ingredient.getId());
        assertEquals(8, summary.getStock());
    }

    @Test
    void shouldSkipCorrectionWhenStockMatches() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 1.0, 5, 0));
        entityManager.flush();
        long countBefore = storageRepository.count();

        storageService.processInventoryCorrection(
                List.of(new InventoryCorrection(ingredient.getId(), 5)));

        long countAfter = storageRepository.count();
        assertEquals(countBefore, countAfter);
    }

    @Test
    void shouldRejectCorrectionForNonExistentIngredient() {
        assertThrows(NotFoundException.class, () -> storageService.processInventoryCorrection(
                List.of(new InventoryCorrection(999999, 10))));
    }

    @Test
    void shouldNotApplyAnyCorrectionWhenOneIngredientIsUnknown() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 1.0, 10, 0));
        entityManager.flush();

        assertThrows(NotFoundException.class, () -> storageService.processInventoryCorrection(List.of(
                new InventoryCorrection(ingredient.getId(), 7),
                new InventoryCorrection(999999, 1))));

        entityManager.clear();
        assertEquals(10, storageRepository.summaryById(ingredient.getId()).getStock());
    }

    @Test
    void shouldProcessMultipleCorrections() {
        Category cat = categoryRepository.save(new Category(null, "MultiCorrCat"));
        Ingredient other = ingredientRepository.save(
                new Ingredient(null, cat, "OtherIngr", 1.0, 12, 0, 0, 0));

        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 1.0, 10, 0));
        storageService.addStorage(new Storage(null, other, Instant.now(), 2.0, 8, 0));
        entityManager.flush();

        storageService.processInventoryCorrection(List.of(
                new InventoryCorrection(ingredient.getId(), 7),
                new InventoryCorrection(other.getId(), 5)
        ));

        entityManager.flush();
        assertEquals(7, storageRepository.summaryById(ingredient.getId()).getStock());
        assertEquals(5, storageRepository.summaryById(other.getId()).getStock());
    }

    @Test
    void shouldBookADeliveryWithItsPayments() {
        User receiver = userRepository.save(new User(null, "receiver", "x", true, Role.STAFF));
        long cashBefore = cashRepository.count();

        Ingredient sentWithAnotherName = new Ingredient(ingredient.getId(), null, "sent name", 0.0, 1, 0, 0, 0);

        Storage stored = storageService.deliver(new InboundStorage(sentWithAnotherName, null, money(2.0), null, money(3.0), 12), receiver);

        assertEquals(12, stored.getAmount());
        assertNotNull(stored.getPurchased());
        assertEquals(cashBefore + 2, cashRepository.count());
        List<Cash> entries = cashRepository.findAll().stream()
                .filter(cash -> cash.getNote() != null && cash.getNote().startsWith(ingredient.getName()))
                .toList();
        assertEquals(Map.of(CashType.PURCHASE, money(-24.0), CashType.PLEDGE, money(-3.0)),
                entries.stream().collect(Collectors.toMap(Cash::getType, Cash::getAmount)));
    }

    @Test
    void shouldCountTheSameIngredientTwiceInOneRequestAsTheLastCount() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 3, 0));
        entityManager.flush();

        storageService.processInventoryCorrection(List.of(
                new InventoryCorrection(ingredient.getId(), 5), new InventoryCorrection(ingredient.getId(), 8)));

        assertEquals(8, storageRepository.summaryById(ingredient.getId()).getStock());
    }

    @Test
    void shouldRejectADeliveryOfAnUnknownIngredient() {
        User receiver = userRepository.save(new User(null, "receiver3", "x", true, Role.STAFF));
        Ingredient unknown = new Ingredient(999999, null, "unknown", 0.0, 1, 0, 0, 0);

        assertThrows(NotFoundException.class,
                () -> storageService.deliver(new InboundStorage(unknown, null, money(1.0), null, money(0.0), 1), receiver));
    }

    @Test
    void shouldBookADeliveryByItsTotalWithoutLosingCents() {
        User receiver = userRepository.save(new User(null, "receiver4", "x", true, Role.STAFF));

        Storage stored = storageService.deliver(new InboundStorage(ingredient, null, null, money(17.54), money(0.0), 20), receiver);

        assertMoney(17.54, stored.getCost());
        assertEquals(new BigDecimal("0.8770"), stored.getPrice());
        assertEquals(new BigDecimal("0.8770"), ingredientRepository.findById(ingredient.getId()).orElseThrow().getPrice());
        assertTrue(cashRepository.findAll().stream()
                .anyMatch(cash -> cash.getType() == CashType.PURCHASE && money(-17.54).equals(cash.getAmount())));
    }

}
