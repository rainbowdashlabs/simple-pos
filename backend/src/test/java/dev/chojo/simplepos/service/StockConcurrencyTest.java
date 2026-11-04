package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks stock changes from parallel, committed transactions: parallel purchases take exactly the sold amount, and
 * a transaction holding an ingredient makes every other stock change of it wait until it commits. Also checks that
 * a refresh token can be used only once when two refreshes race.
 * <p>
 * Because these tests commit, the context is discarded afterwards, which drops the schema so no data leaks into
 * other tests.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class StockConcurrencyTest {
    private static final int BUYERS = 4;
    private static final int UNITS_PER_BUYER = 5;

    @Autowired
    PurchaseService purchaseService;
    @Autowired
    ProductService productService;
    @Autowired
    StorageService storageService;
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    TransactionTemplate transactionTemplate;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    UserRepository userRepository;

    @Test
    void shouldNotLoseStockUpdatesUnderConcurrentPurchases() throws Exception {
        Category category = categoryRepository.save(new Category(null, "ConcurrencyCat"));
        Ingredient ingredient = ingredientRepository.save(new Ingredient(null, category, "ConcurrencyIngr", 0.5, 24, 0, 0, 0));
        Product product = productService.create(new Product(null, category, "ConcurrencyProduct", 2.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 1)))));
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, BUYERS * UNITS_PER_BUYER, 0));
        Account account = accountRepository.save(new Account(null, "ConcurrencyAccount", Instant.now()));

        try (ExecutorService executor = Executors.newFixedThreadPool(BUYERS)) {
            List<Future<?>> purchases = new ArrayList<>();
            for (int i = 0; i < BUYERS; i++) {
                purchases.add(executor.submit(() ->
                        purchaseService.purchase(account, List.of(new CartPositionDto(product, UNITS_PER_BUYER)))));
            }
            for (Future<?> purchase : purchases) purchase.get(30, TimeUnit.SECONDS);
        }

        assertEquals(0, new StorageSummary(storageRepository.summaryById(ingredient.getId())).getStock());
    }

    @Test
    void shouldMakeStockChangesWaitWhileAnotherTransactionHoldsTheIngredient() throws Exception {
        Category category = categoryRepository.save(new Category(null, "LockCat"));
        Ingredient ingredient = ingredientRepository.save(new Ingredient(null, category, "LockIngr", 0.5, 24, 0, 0, 0));
        storageService.addStorage(new Storage(null, ingredient, Instant.now(), 0.5, 10, 0));
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> holder = executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {
                ingredientRepository.requireLocked(ingredient.getId());
                locked.countDown();
                awaitUninterruptibly(release);
            }));
            if (!locked.await(10, TimeUnit.SECONDS)) holder.get(10, TimeUnit.SECONDS);
            assertEquals(0, locked.getCount(), "holder did not acquire the lock");
            Future<?> taker = executor.submit(() -> storageService.takeStorage(ingredient, 1));

            try {
                assertThrows(TimeoutException.class, () -> taker.get(500, TimeUnit.MILLISECONDS));
            } finally {
                release.countDown();
            }
            holder.get(30, TimeUnit.SECONDS);
            taker.get(10, TimeUnit.SECONDS);
        }

        assertEquals(9, new StorageSummary(storageRepository.summaryById(ingredient.getId())).getStock());
    }

    @Test
    void shouldLetOnlyOneOfTwoConcurrentRefreshesUseTheToken() throws Exception {
        User user = userRepository.save(new User(null, "concurrentrefresh", "password", true, Role.STAFF));
        authenticationService.saveRefreshToken("single-use", user, Duration.ofDays(1));

        List<Boolean> results = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Boolean>> refreshes = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                refreshes.add(executor.submit(() -> {
                    awaitUninterruptibly(start);
                    return authenticationService.invalidateRefreshToken(user, "single-use");
                }));
            }
            start.countDown();
            for (Future<Boolean> refresh : refreshes) results.add(refresh.get(30, TimeUnit.SECONDS));
        }

        assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
    }

    private static void awaitUninterruptibly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
