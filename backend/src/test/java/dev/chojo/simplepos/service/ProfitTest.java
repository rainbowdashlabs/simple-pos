package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.entity.dto.InboundStorage;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static dev.chojo.simplepos.MoneyAssertions.assertMoney;
import static dev.chojo.simplepos.MoneyAssertions.assertUnitPrice;
import static dev.chojo.simplepos.MoneyAssertions.money;

/**
 * A delivery known only by its total, which does not divide into cents per unit: selling all of it costs exactly
 * what was paid, so the profit is exactly revenue minus the total.
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
}
