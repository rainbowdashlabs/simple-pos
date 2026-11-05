package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.repository.projection.TotalsRow;
import dev.chojo.simplepos.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static dev.chojo.simplepos.MoneyAssertions.assertMoney;
import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PurchaseRepositoryTest {
    @Autowired
    PurchaseRepository purchaseRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    ProductService productService;
    @Autowired
    PurchasePartRepository purchasePartRepository;
    @Autowired
    StorageRepository storageRepository;

    private Account account;
    private Product product;
    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        account = accountRepository.save(new Account(null, "Buyer", Instant.now()));
        Category category = categoryRepository.save(new Category(null, "PurchaseCat"));
        ingredient = ingredientRepository.save(new Ingredient(null, category, "PurchaseIngr", 0.5, 12, 0, 0, 0));
        Recipe recipe = new Recipe(null, null, Collections.emptyList());
        product = productService.create(new Product(null, category, "PurchaseProduct", 2.5, true, recipe));
    }

    @Test
    void shouldSavePurchase() {
        Purchase purchase = purchaseRepository.save(
                new Purchase(null, account, product, Instant.now(), 3, 2.5, Collections.emptyList()));
        assertNotNull(purchase.getId());
    }

    @Test
    void shouldFindByAccountIdOrderedByPurchasedDesc() {
        Instant now = Instant.now();
        purchaseRepository.save(new Purchase(null, account, product, now.minus(2, ChronoUnit.HOURS), 1, 2.5, Collections.emptyList()));
        purchaseRepository.save(new Purchase(null, account, product, now.minus(1, ChronoUnit.HOURS), 2, 2.5, Collections.emptyList()));
        purchaseRepository.save(new Purchase(null, account, product, now, 3, 2.5, Collections.emptyList()));

        List<Purchase> results = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(
                account.getId(), PageRequest.of(0, 10));
        assertEquals(3, results.size());
        assertEquals(3, results.get(0).getAmount());
        assertEquals(2, results.get(1).getAmount());
        assertEquals(1, results.get(2).getAmount());
    }

    @Test
    void shouldCalculateTotalPurchases() {
        purchaseRepository.save(new Purchase(null, account, product, Instant.now(), 2, 2.5, Collections.emptyList()));
        purchaseRepository.save(new Purchase(null, account, product, Instant.now(), 3, 1.0, Collections.emptyList()));

        BigDecimal total = purchaseRepository.totalPurchases();
        assertMoney(8.0, total);
    }

    @Test
    void shouldCalculateTotalPurchasesAfterDate() {
        Instant cutoff = Instant.now().minus(1, ChronoUnit.HOURS);
        purchaseRepository.save(new Purchase(null, account, product, Instant.now().minus(2, ChronoUnit.HOURS), 1, 2.5, Collections.emptyList()));
        purchaseRepository.save(new Purchase(null, account, product, Instant.now(), 2, 3.0, Collections.emptyList()));

        BigDecimal total = purchaseRepository.totalPurchases(cutoff);
        assertMoney(6.0, total);
    }

    @Test
    void shouldReturnNullTotalWhenEmpty() {
        BigDecimal total = purchaseRepository.totalPurchases();
        assertNull(total);
    }

    @Test
    void shouldPaginatePurchases() {
        for (int i = 0; i < 5; i++) {
            purchaseRepository.save(new Purchase(null, account, product, Instant.now().minus(i, ChronoUnit.MINUTES), 1, 2.5, Collections.emptyList()));
        }

        List<Purchase> page = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(
                account.getId(), PageRequest.of(0, 2));
        assertEquals(2, page.size());
    }

    @Test
    void shouldConvertToDto() {
        Purchase purchase = purchaseRepository.save(
                new Purchase(null, account, product, Instant.now(), 2, 2.5, Collections.emptyList()));
        var dto = purchase.toDto();
        assertEquals(purchase.getId(), dto.id());
        assertEquals(product.getId(), dto.productId());
        assertEquals(2, dto.amount());
        assertMoney(2.5, dto.price());
    }

    @Test
    void shouldNotReturnPurchasesForDifferentAccount() {
        Account other = accountRepository.save(new Account(null, "OtherBuyer", Instant.now()));
        purchaseRepository.save(new Purchase(null, other, product, Instant.now(), 1, 2.5, Collections.emptyList()));

        List<Purchase> results = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(
                account.getId(), PageRequest.of(0, 10));
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldDeletePurchase() {
        Purchase purchase = purchaseRepository.save(
                new Purchase(null, account, product, Instant.now(), 1, 2.5, Collections.emptyList()));
        assertTrue(purchaseRepository.existsById(purchase.getId()));
        purchaseRepository.deleteById(purchase.getId());
        assertFalse(purchaseRepository.existsById(purchase.getId()));
    }

    @Test
    void shouldListUnsoldButNotDeletedProductsAsBottomSales() {
        Category category = categoryRepository.save(new Category(null, "BottomCat"));
        Product unsold = productService.create(
                new Product(null, category, "Unsold", 1.0, true, new Recipe(null, null, Collections.emptyList())));
        Product deleted = productService.create(
                new Product(null, category, "Deleted", 1.0, true, new Recipe(null, null, Collections.emptyList())));
        deleted.setDeleted(true);
        productRepository.save(deleted);
        purchaseRepository.save(new Purchase(null, account, product, Instant.now(), 2, 2.5, Collections.emptyList()));

        List<Integer> ids = purchaseRepository.getBottomSales(Instant.now().minus(1, ChronoUnit.DAYS), PageRequest.of(0, 100))
                .stream()
                .map(t -> t.getProductId())
                .toList();

        assertTrue(ids.contains(unsold.getId()));
        assertTrue(ids.contains(product.getId()));
        assertFalse(ids.contains(deleted.getId()));
        assertTrue(ids.indexOf(unsold.getId()) < ids.indexOf(product.getId()));
    }

    @Test
    void shouldLimitProductSalesStatToTheWindow() {
        Instant now = Instant.now();
        purchaseRepository.save(new Purchase(null, account, product, now.minus(40, ChronoUnit.DAYS), 4, 2.5, Collections.emptyList()));
        purchaseRepository.save(new Purchase(null, account, product, now.minus(2, ChronoUnit.DAYS), 1, 2.5, Collections.emptyList()));

        var days = purchaseRepository.getSalesStatByProduct(product.getId(), now.minus(30, ChronoUnit.DAYS), "Europe/Berlin", PageRequest.of(0, 100));

        assertEquals(1, days.size());
        assertEquals(1, days.get(0).getSales().intValue());
    }

    @Test
    void salesByCategoryAreGroupedPerCategory() {
        Category category = categoryRepository.save(new Category(null, "GroupedCat"));
        Product second = productService.create(new Product(null, category, "Second", 1.0, true, new Recipe(null, null, Collections.emptyList())));
        Product third = productService.create(new Product(null, category, "Third", 1.0, true, new Recipe(null, null, Collections.emptyList())));
        Product loose = productService.create(new Product(null, null, "Loose", 1.0, true, new Recipe(null, null, Collections.emptyList())));
        Instant now = Instant.now();
        purchaseRepository.save(new Purchase(null, account, second, now, 2, 1.0, Collections.emptyList()));
        purchaseRepository.save(new Purchase(null, account, third, now, 3, 1.0, Collections.emptyList()));
        purchaseRepository.save(new Purchase(null, account, loose, now, 1, 1.0, Collections.emptyList()));

        var rows = purchaseRepository.getSalesByCategory(now.minus(1, ChronoUnit.HOURS));

        var grouped = rows.stream().filter(t -> t.getCategoryId().equals(category.getId())).toList();
        assertEquals(1, grouped.size());
        assertEquals(5, grouped.get(0).getSales().intValue());
        assertTrue(rows.stream().anyMatch(t -> t.getCategoryId() == Category.UNCATEGORISED_ID
                && t.getSales() == 1));
    }

    @Test
    void daysEndAtMidnightInTheBusinessTimeZone() {
        Instant afterMidnightInBerlin = Instant.parse("2026-06-10T22:30:00Z");
        purchaseRepository.save(new Purchase(null, account, product, afterMidnightInBerlin, 1, 2.5, Collections.emptyList()));

        var berlin = purchaseRepository.getDailyStats(Instant.parse("2026-06-09T00:00:00Z"), "Europe/Berlin");
        var utc = purchaseRepository.getDailyStats(Instant.parse("2026-06-09T00:00:00Z"), "UTC");

        assertEquals(LocalDate.of(2026, 6, 11), berlin.get(0).getDay());
        assertEquals(LocalDate.of(2026, 6, 10), utc.get(0).getDay());
    }

    @Test
    void theSummaryTotalsEverySaleSinceTheDateIncludingGuests() {
        Instant after = Instant.parse("2001-05-01T00:00:00Z");
        TotalsRow before = purchaseRepository.getSalesSummary(after);
        Purchase sold = new Purchase(null, account, product, Instant.parse("2001-05-02T12:00:00Z"), 3, 2.5, Collections.emptyList());
        sold.setCost(new BigDecimal("1.20"));
        purchaseRepository.save(sold);
        purchaseRepository.save(new Purchase(null, null, product, Instant.parse("2001-05-03T12:00:00Z"), 1, 2.5, Collections.emptyList()));
        purchaseRepository.save(new Purchase(null, account, product, Instant.parse("2001-04-30T12:00:00Z"), 9, 2.5, Collections.emptyList()));

        TotalsRow summary = purchaseRepository.getSalesSummary(after);

        assertEquals(4, summary.getSales() - before.getSales());
        assertMoney(10.0, summary.getRevenue().subtract(before.getRevenue()));
        assertMoney(8.8, summary.getProfit().subtract(before.getProfit()));
    }

    @Test
    void storageCannotSellMoreThanItHolds() {
        assertThrows(DataIntegrityViolationException.class,
                () -> storageRepository.saveAndFlush(new Storage(null, ingredient, Instant.now(), 1.0, 2, 3)));
    }
}
