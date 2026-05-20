package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

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

    private Account account;
    private Product product;

    @BeforeEach
    void setUp() {
        account = accountRepository.save(new Account(null, "Buyer", Instant.now()));
        Category category = categoryRepository.save(new Category(null, "PurchaseCat"));
        ingredientRepository.save(new Ingredient(null, category, "PurchaseIngr", 0.5, 12, 0, 0, 0));
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

        Double total = purchaseRepository.totalPurchases();
        // 2*2.5 + 3*1.0 = 8.0
        assertEquals(8.0, total, 0.001);
    }

    @Test
    void shouldCalculateTotalPurchasesAfterDate() {
        Instant cutoff = Instant.now().minus(1, ChronoUnit.HOURS);
        purchaseRepository.save(new Purchase(null, account, product, Instant.now().minus(2, ChronoUnit.HOURS), 1, 2.5, Collections.emptyList()));
        purchaseRepository.save(new Purchase(null, account, product, Instant.now(), 2, 3.0, Collections.emptyList()));

        Double total = purchaseRepository.totalPurchases(cutoff);
        // Only the recent purchase: 2*3.0 = 6.0
        assertEquals(6.0, total, 0.001);
    }

    @Test
    void shouldReturnNullTotalWhenEmpty() {
        Double total = purchaseRepository.totalPurchases();
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
        assertEquals(2.5, dto.price(), 0.001);
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
}
