package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.PurchasePart;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.dto.StockProjectionDto;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.PurchasePartRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StockProjectionServiceTest {
    @Autowired
    StockProjectionService projectionService;
    @Autowired
    ProductService productService;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    PurchaseRepository purchaseRepository;
    @Autowired
    PurchasePartRepository purchasePartRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    EntityManager entityManager;

    private final Instant now = Instant.parse("2026-06-01T12:00:00Z");
    private Ingredient ingredient;
    private Storage batch;
    private Product product;
    private Account account;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(new Category(null, "ProjectionCat"));
        ingredient = ingredientRepository.save(new Ingredient(null, category, "Projected", 1.0, 0, 0, 0, 0));
        batch = storageRepository.save(new Storage(null, ingredient, now.minus(Duration.ofDays(200)), 1.0, 25, 0));
        product = productService.create(new Product(null, category, "NoRecipeNeeded", 2.0, true,
                new Recipe(null, null, Collections.emptyList())));
        account = accountRepository.save(new Account(null, "Projector", now));
    }

    @Test
    void usageOfANewIngredientIsAveragedOverTheDaysItExisted() {
        take(10, now.minus(Duration.ofDays(10)));
        take(10, now.minus(Duration.ofDays(1)));

        StockProjectionDto projection = projectionOf(ingredient);

        assertEquals(2.0, projection.dailyConsumption(), 0.001);
        assertEquals(5, projection.currentStock());
        assertEquals(2, projection.daysRemaining());
    }

    @Test
    void usageIsAveragedOverAtMostTheWindow() {
        take(3, now.minus(Duration.ofDays(150)));
        take(18, now.minus(Duration.ofDays(5)));

        StockProjectionDto projection = projectionOf(ingredient);

        assertEquals(0.2, projection.dailyConsumption(), 0.001);
    }

    @Test
    void ingredientsWithoutRecentUseAndFutureTakesAreLeftOut() {
        take(5, now.minus(Duration.ofDays(120)));
        take(5, now.plus(Duration.ofDays(2)));

        assertTrue(projectionService.soonestEmpty(now).stream()
                .noneMatch(p -> p.ingredient().getId().equals(ingredient.getId())));
    }

    @Test
    void windowIsAtLeastOneDay() {
        assertEquals(1, StockProjectionService.windowDays(now.minusSeconds(60), now));
        assertEquals(1, StockProjectionService.windowDays(now.plus(Duration.ofDays(1)), now));
        assertEquals(StockProjectionService.WINDOW_DAYS, StockProjectionService.windowDays(now.minus(Duration.ofDays(400)), now));
    }

    private void take(int units, Instant when) {
        Purchase purchase = purchaseRepository.save(new Purchase(null, account, product, when, units, 2.0, Collections.emptyList()));
        for (int i = 0; i < units; i++) {
            purchasePartRepository.save(new PurchasePart(null, purchase, batch));
            batch.take();
        }
        storageRepository.save(batch);
        entityManager.flush();
    }

    private StockProjectionDto projectionOf(Ingredient ingredient) {
        return projectionService.soonestEmpty(now).stream()
                .filter(p -> p.ingredient().getId().equals(ingredient.getId()))
                .findFirst()
                .orElseThrow();
    }
}
