package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.PurchasePart;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.exceptions.ConflictException;
import dev.chojo.simplepos.exceptions.NotFoundException;
import dev.chojo.simplepos.repository.AccountBalanceRepository;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.PurchasePartRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static dev.chojo.simplepos.MoneyAssertions.assertMoney;
import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PurchaseServiceTest {
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    PurchaseRepository purchaseRepository;
    @Autowired
    PurchasePartRepository purchasePartRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    ProductService productService;
    @Autowired
    StorageService storageService;
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    EntityManager entityManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CashService cashService;
    @Autowired
    AccountBalanceRepository accountBalanceRepository;

    private Account account;
    private Product product;
    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        account = accountRepository.save(new Account(null, "PurchaseTestAccount", Instant.now()));
        Category category = categoryRepository.save(new Category(null, "PurchaseServiceCat"));
        ingredient = ingredientRepository.save(
                new Ingredient(null, category, "PurchaseServiceIngr", 0.5, 24, 0, 0, 0));

        RecipeEntry entry = new RecipeEntry(null, ingredient, 1);
        Recipe recipe = new Recipe(null, null, List.of(entry));
        product = productService.create(new Product(null, category, "PurchaseServiceProduct", 2.0, true, recipe));
    }

    @Test
    void shouldCreatePurchase() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));

        entityManager.flush();
        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 1)), null);

        List<Purchase> purchases = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(
                account.getId(), PageRequest.of(0, 10));
        assertEquals(1, purchases.size());
        assertEquals(1, purchases.get(0).getAmount());
        assertMoney(2.0, purchases.get(0).getPrice());
    }

    @Test
    void shouldRecordWhoSold() {
        User cashier = userRepository.save(new User(null, "soldbycashier", "x", true, Role.STAFF));

        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 1)), cashier);
        entityManager.flush();
        entityManager.clear();

        Purchase sold = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(account.getId(), PageRequest.of(0, 1)).get(0);
        assertEquals("soldbycashier", sold.toDto().soldBy());
    }

    @Test
    void shouldCreatePurchaseWithMultipleQuantity() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));

        entityManager.flush();
        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 3)), null);

        List<Purchase> purchases = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(
                account.getId(), PageRequest.of(0, 10));
        assertEquals(1, purchases.size());
        assertEquals(3, purchases.get(0).getAmount());
    }

    @Test
    void shouldDeductStorageOnPurchase() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));

        entityManager.flush();
        entityManager.clear();
        Product freshProduct = productRepository.findById(product.getId()).orElseThrow();
        Account freshAccount = accountRepository.findById(account.getId()).orElseThrow();
        purchaseService.purchase(freshAccount.getId(), List.of(new CartPositionDto(freshProduct.getId(), 3)), null);

        entityManager.flush();
        entityManager.clear();
        assertEquals(7, storageRepository.summaryById(ingredient.getId()).getStock());
    }

    @Test
    void shouldCreatePurchaseParts() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));

        entityManager.flush();
        entityManager.clear();
        Product freshProduct = productRepository.findById(product.getId()).orElseThrow();
        Account freshAccount = accountRepository.findById(account.getId()).orElseThrow();
        purchaseService.purchase(freshAccount.getId(), List.of(new CartPositionDto(freshProduct.getId(), 2)), null);

        entityManager.flush();
        entityManager.clear();
        List<Purchase> purchases = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(
                account.getId(), PageRequest.of(0, 10));
        Purchase purchase = purchases.get(0);
        assertNotNull(purchase.getParts());
        assertFalse(purchase.getParts().isEmpty());
    }

    @Test
    void shouldHandleMultipleCartPositions() {
        Category cat = categoryRepository.save(new Category(null, "MultiCartCat"));
        Ingredient ingr2 = ingredientRepository.save(
                new Ingredient(null, cat, "Ingr2", 0.3, 12, 0, 0, 0));

        RecipeEntry entry2 = new RecipeEntry(null, ingr2, 1);
        Recipe recipe2 = new Recipe(null, null, List.of(entry2));
        Product product2 = productService.create(new Product(null, cat, "Product2", 1.5, true, recipe2));

        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));
        storageService.addStorage(new Storage(null, ingr2, Instant.now(), 0.3, 10, 0));

        entityManager.flush();
        purchaseService.purchase(account.getId(), List.of(
                new CartPositionDto(product.getId(), 2),
                new CartPositionDto(product2.getId(), 1)
        ), null);

        List<Purchase> purchases = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(
                account.getId(), PageRequest.of(0, 10));
        assertEquals(2, purchases.size());
    }

    @Test
    void shouldHandlePurchaseWithEmptyRecipe() {
        Recipe emptyRecipe = new Recipe(null, null, Collections.emptyList());
        Category cat = categoryRepository.save(new Category(null, "EmptyRecipeCat"));
        Product noIngredientProduct = productService.create(
                new Product(null, cat, "NoIngrProduct", 1.0, true, emptyRecipe));

        entityManager.flush();
        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(noIngredientProduct.getId(), 2)), null);

        List<Purchase> purchases = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(
                account.getId(), PageRequest.of(0, 10));
        assertEquals(1, purchases.size());
    }

    @Test
    void shouldHandlePurchaseWithInsufficientStock() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 2, 0));

        // Product recipe requires 1 ingredient each, ordering 5 but only 2 in stock
        entityManager.flush();
        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 5)), null);

        // Purchase should still be created
        List<Purchase> purchases = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(
                account.getId(), PageRequest.of(0, 10));
        assertEquals(1, purchases.size());
    }

    @Test
    void shouldRejectPurchaseWithUnknownProductWithoutBookingAnything() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));

        entityManager.flush();
        assertThrows(NotFoundException.class, () -> purchaseService.purchase(account.getId(), List.of(
                new CartPositionDto(product.getId(), 1),
                new CartPositionDto(999999, 1)), null));

        entityManager.clear();
        assertTrue(purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(account.getId(), PageRequest.of(0, 10)).isEmpty());
        assertEquals(10, storageRepository.summaryById(ingredient.getId()).getStock());
    }

    @Test
    void shouldTakeAvailableStockWhenStockIsInsufficient() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 2, 0));

        entityManager.flush();
        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 5)), null);

        entityManager.flush();
        entityManager.clear();
        assertEquals(0, storageRepository.summaryById(ingredient.getId()).getStock());
        List<Purchase> purchases = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(
                account.getId(), PageRequest.of(0, 10));
        assertEquals(1, purchases.size());
        assertEquals(5, purchases.get(0).getAmount());
        List<PurchasePart> parts = purchases.get(0).getParts().stream()
                .sorted(Comparator.comparing(PurchasePart::getId))
                .toList();
        assertEquals(2, parts.size());
        assertEquals(2, parts.get(0).getAmount());
        assertNotNull(parts.get(0).getStorage());
        assertEquals(3, parts.get(1).getAmount());
        assertNull(parts.get(1).getStorage());
    }

    @Test
    void shouldRecordCorrectPrice() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));

        entityManager.flush();
        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 1)), null);

        Purchase purchase = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(
                account.getId(), PageRequest.of(0, 1)).get(0);
        assertMoney(product.getPrice(), purchase.getPrice());
    }

    @Test
    void shouldRejectAProductThatIsNotOnSale() {
        product.setActive(false);
        productRepository.save(product);

        assertThrows(ConflictException.class,
                () -> purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 1)), null));
    }

    @Test
    void shouldRejectADeletedProduct() {
        product.setDeleted(true);
        productRepository.save(product);

        assertThrows(NotFoundException.class,
                () -> purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 1)), null));
    }

    @Test
    void shouldGiveTheStockBackWhenAPurchaseIsCancelled() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));
        entityManager.flush();
        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 3)), null);
        entityManager.flush();
        entityManager.clear();
        Purchase purchase = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(account.getId(), PageRequest.of(0, 1)).get(0);

        purchaseService.cancel(purchase.getId(), null);

        entityManager.flush();
        entityManager.clear();
        assertEquals(10, storageRepository.summaryById(ingredient.getId()).getStock());
        assertTrue(purchaseRepository.findById(purchase.getId()).isEmpty());
        assertEquals(0, purchasePartRepository.count());
        assertMoney(0.0, accountRepository.findAccountById(account.getId()).orElseThrow().getBalance());
    }

    @Test
    void shouldUndoAWholeSale() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));
        Product second = productService.create(new Product(null, product.getCategory(), "PurchaseServiceSecond", 1.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 2)))));
        entityManager.flush();
        List<Integer> sale = purchaseService.purchase(account.getId(),
                List.of(new CartPositionDto(product.getId(), 3), new CartPositionDto(second.getId(), 1)), null);
        entityManager.flush();
        entityManager.clear();

        purchaseService.cancelAll(sale, null);

        entityManager.flush();
        entityManager.clear();
        assertEquals(2, sale.size());
        assertEquals(10, storageRepository.summaryById(ingredient.getId()).getStock());
        assertTrue(purchaseRepository.findAllById(sale).isEmpty());
        assertMoney(0.0, accountRepository.findAccountById(account.getId()).orElseThrow().getBalance());
    }

    @Test
    void aGuestSalePaysIntoTheTillAndLeavesAccountsAlone() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));
        entityManager.flush();
        BigDecimal tillBefore = cashService.tillTotal();
        BigDecimal accountsBefore = Money.orZero(accountBalanceRepository.totalBalance());
        BigDecimal salesBefore = Money.orZero(purchaseRepository.totalPurchases());

        List<Integer> sale = purchaseService.purchaseAsGuest(List.of(new CartPositionDto(product.getId(), 3)), null);
        entityManager.flush();

        assertMoney(tillBefore.add(new BigDecimal("6.00")), cashService.tillTotal());
        assertMoney(accountsBefore, Money.orZero(accountBalanceRepository.totalBalance()));
        assertMoney(salesBefore.add(new BigDecimal("6.00")), Money.orZero(purchaseRepository.totalPurchases()));
        assertEquals(7, storageRepository.summaryById(ingredient.getId()).getStock());

        purchaseService.cancelAll(sale, null);
        entityManager.flush();
        entityManager.clear();

        assertMoney(tillBefore, cashService.tillTotal());
        assertEquals(10, storageRepository.summaryById(ingredient.getId()).getStock());
        assertTrue(purchaseRepository.findAllById(sale).isEmpty());
    }

    @Test
    void guestAndAccountPurchasesCanBeCancelledTogether() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));
        entityManager.flush();
        BigDecimal tillBefore = cashService.tillTotal();
        List<Integer> ids = new ArrayList<>(purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 2)), null));
        ids.addAll(purchaseService.purchaseAsGuest(List.of(new CartPositionDto(product.getId(), 1)), null));
        entityManager.flush();

        purchaseService.cancelAll(ids, null);
        entityManager.flush();
        entityManager.clear();

        assertMoney(tillBefore, cashService.tillTotal());
        assertMoney(0.0, accountRepository.findAccountById(account.getId()).orElseThrow().getBalance());
        assertEquals(10, storageRepository.summaryById(ingredient.getId()).getStock());
    }

    @Test
    void shouldCancelNothingWhenOneOfTheSaleIsGone() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));
        entityManager.flush();
        List<Integer> sale = purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 2)), null);
        entityManager.flush();

        assertThrows(NotFoundException.class, () -> purchaseService.cancelAll(List.of(sale.get(0), -1), null));
        entityManager.clear();
        assertTrue(purchaseRepository.findById(sale.get(0)).isPresent());
    }

    @Test
    void shouldGiveTheStockBackToEveryBatchItCameFrom() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now().minusSeconds(60), 0.5, 2, 0));
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 5, 0));
        entityManager.flush();
        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 4)), null);
        entityManager.flush();
        entityManager.clear();
        Purchase purchase = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(account.getId(), PageRequest.of(0, 1)).get(0);

        purchaseService.cancel(purchase.getId(), null);

        entityManager.flush();
        entityManager.clear();
        assertEquals(7, storageRepository.summaryById(ingredient.getId()).getStock());
        assertTrue(storageRepository.findAll().stream()
                .filter(storage -> storage.getIngredient().getId().equals(ingredient.getId()))
                .allMatch(storage -> storage.getSold() == 0));
    }

    @Test
    void shouldCancelAPurchaseSoldWhileOutOfStock() {
        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 2)), null);
        entityManager.flush();
        Purchase purchase = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(account.getId(), PageRequest.of(0, 1)).get(0);

        purchaseService.cancel(purchase.getId(), null);

        entityManager.flush();
        assertTrue(purchaseRepository.findById(purchase.getId()).isEmpty());
    }

    @Test
    void shouldNotCancelAPurchaseOfAClosedAccount() {
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));
        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product.getId(), 1)), null);
        Purchase purchase = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(account.getId(), PageRequest.of(0, 1)).get(0);
        account.setDeleted(true);
        accountRepository.save(account);

        assertThrows(ConflictException.class, () -> purchaseService.cancel(purchase.getId(), null));
    }
}
