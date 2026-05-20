package dev.chojo.simplepos.configuration;

import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Balance;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.repository.UserRepository;
import dev.chojo.simplepos.service.ProductService;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Component
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductService productService;
    private final StorageRepository storageRepository;
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final PurchaseRepository purchaseRepository;
    private final CashRepository cashRepository;

    public DemoDataSeeder(EntityManager entityManager, UserRepository userRepository, PasswordEncoder passwordEncoder,
                          CategoryRepository categoryRepository, IngredientRepository ingredientRepository,
                          ProductService productService, StorageRepository storageRepository,
                          AccountRepository accountRepository, BalanceRepository balanceRepository,
                          PurchaseRepository purchaseRepository, CashRepository cashRepository) {
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.productService = productService;
        this.storageRepository = storageRepository;
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.purchaseRepository = purchaseRepository;
        this.cashRepository = cashRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Wiping database for demo mode...");
        // Ensure schema is up to date before truncating
        entityManager.createNativeQuery(
                "ALTER TABLE account ADD COLUMN IF NOT EXISTS deleted boolean DEFAULT false"
        ).executeUpdate();
        entityManager.createNativeQuery("DO $$ DECLARE r RECORD; BEGIN " +
                "FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP " +
                "EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE'; END LOOP; " +
                "FOR r IN (SELECT sequencename FROM pg_sequences WHERE schemaname = 'public') LOOP " +
                "EXECUTE 'ALTER SEQUENCE ' || quote_ident(r.sequencename) || ' RESTART WITH 1'; END LOOP; " +
                "END $$").executeUpdate();
        entityManager.flush();

        log.info("Seeding demo data...");

        // Demo user
        User demoUser = userRepository.save(
                new User(null, "demo", passwordEncoder.encode("demo"), true));

        // Categories
        Category drinks = categoryRepository.save(new Category(null, "Drinks"));
        Category softDrinks = categoryRepository.save(new Category(null, "Soft Drinks"));
        Category snacks = categoryRepository.save(new Category(null, "Snacks"));
        Category spirits = categoryRepository.save(new Category(null, "Spirits"));

        // Ingredients
        Ingredient cola = ingredientRepository.save(
                new Ingredient(null, softDrinks, "Cola", 0.45, 24, 0.25, 3.50, 24));
        Ingredient fanta = ingredientRepository.save(
                new Ingredient(null, softDrinks, "Fanta", 0.45, 24, 0.25, 3.50, 24));
        Ingredient sprite = ingredientRepository.save(
                new Ingredient(null, softDrinks, "Sprite", 0.45, 24, 0.25, 3.50, 24));
        Ingredient water = ingredientRepository.save(
                new Ingredient(null, softDrinks, "Water", 0.20, 12, 0.25, 1.50, 12));
        Ingredient beer = ingredientRepository.save(
                new Ingredient(null, drinks, "Beer 0.5L", 0.80, 20, 0.08, 1.50, 20));
        Ingredient beerSmall = ingredientRepository.save(
                new Ingredient(null, drinks, "Beer 0.33L", 0.65, 24, 0.08, 1.50, 24));
        Ingredient chips = ingredientRepository.save(
                new Ingredient(null, snacks, "Chips", 1.20, 0, 0, 0, 5));
        Ingredient peanuts = ingredientRepository.save(
                new Ingredient(null, snacks, "Peanuts", 0.90, 0, 0, 0, 5));
        Ingredient vodka = ingredientRepository.save(
                new Ingredient(null, spirits, "Vodka", 0.50, 0, 0, 0, 2));
        Ingredient rum = ingredientRepository.save(
                new Ingredient(null, spirits, "Rum", 0.60, 0, 0, 0, 2));

        // Products (single ingredient)
        Product pCola = productService.create(new Product(null, softDrinks, "Cola", 1.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, cola, 1)))));
        Product pFanta = productService.create(new Product(null, softDrinks, "Fanta", 1.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, fanta, 1)))));
        Product pSprite = productService.create(new Product(null, softDrinks, "Sprite", 1.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, sprite, 1)))));
        Product pWater = productService.create(new Product(null, softDrinks, "Water", 1.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, water, 1)))));
        Product pBeer = productService.create(new Product(null, drinks, "Beer 0.5L", 2.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, beer, 1)))));
        Product pBeerSmall = productService.create(new Product(null, drinks, "Beer 0.33L", 2.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, beerSmall, 1)))));
        Product pChips = productService.create(new Product(null, snacks, "Chips", 2.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, chips, 1)))));
        Product pPeanuts = productService.create(new Product(null, snacks, "Peanuts", 1.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, peanuts, 1)))));

        // Multi-ingredient products
        Product pCubaLibre = productService.create(new Product(null, spirits, "Cuba Libre", 4.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, rum, 1), new RecipeEntry(null, cola, 1)))));
        Product pVodkaSprite = productService.create(new Product(null, spirits, "Vodka Sprite", 4.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, vodka, 1), new RecipeEntry(null, sprite, 1)))));

        // Inactive product
        productService.create(new Product(null, drinks, "Old Lemonade", 1.00, false,
                new Recipe(null, null, Collections.emptyList())));

        // Storage (inventory)
        Instant now = Instant.now();
        Instant weekAgo = now.minus(7, ChronoUnit.DAYS);
        addStock(cola, 48, 0.45, weekAgo);
        addStock(fanta, 48, 0.45, weekAgo);
        addStock(sprite, 48, 0.45, weekAgo);
        addStock(water, 24, 0.20, weekAgo);
        addStock(beer, 40, 0.80, weekAgo);
        addStock(beerSmall, 48, 0.65, weekAgo);
        addStock(chips, 10, 1.20, weekAgo);
        addStock(peanuts, 8, 0.90, weekAgo);
        addStock(vodka, 5, 0.50, weekAgo);
        addStock(rum, 5, 0.60, weekAgo);

        // Low stock item to show in overview
        addStock(chips, 2, 1.20, now);

        // Accounts
        Account alice = accountRepository.save(new Account(null, "Alice", now.minus(30, ChronoUnit.DAYS)));
        Account bob = accountRepository.save(new Account(null, "Bob", now.minus(20, ChronoUnit.DAYS)));
        Account charlie = accountRepository.save(new Account(null, "Charlie", now.minus(10, ChronoUnit.DAYS)));
        Account dave = accountRepository.save(new Account(null, "Dave", now.minus(5, ChronoUnit.DAYS)));

        // Deposits
        balanceRepository.save(new Balance(null, alice, now.minus(25, ChronoUnit.DAYS), 50.0));
        balanceRepository.save(new Balance(null, alice, now.minus(10, ChronoUnit.DAYS), 30.0));
        balanceRepository.save(new Balance(null, bob, now.minus(15, ChronoUnit.DAYS), 20.0));
        balanceRepository.save(new Balance(null, bob, now.minus(3, ChronoUnit.DAYS), 40.0));
        balanceRepository.save(new Balance(null, charlie, now.minus(8, ChronoUnit.DAYS), 25.0));
        balanceRepository.save(new Balance(null, dave, now.minus(4, ChronoUnit.DAYS), 100.0));

        // Purchases (spread over the past weeks for sales stats)
        addPurchase(alice, pCola, 2, weekAgo.minus(1, ChronoUnit.DAYS));
        addPurchase(alice, pBeer, 3, weekAgo);
        addPurchase(alice, pChips, 1, weekAgo);
        addPurchase(alice, pCubaLibre, 2, now.minus(3, ChronoUnit.DAYS));
        addPurchase(bob, pFanta, 2, now.minus(5, ChronoUnit.DAYS));
        addPurchase(bob, pBeer, 2, now.minus(4, ChronoUnit.DAYS));
        addPurchase(bob, pPeanuts, 1, now.minus(4, ChronoUnit.DAYS));
        addPurchase(bob, pBeerSmall, 4, now.minus(2, ChronoUnit.DAYS));
        addPurchase(charlie, pWater, 3, now.minus(6, ChronoUnit.DAYS));
        addPurchase(charlie, pSprite, 2, now.minus(3, ChronoUnit.DAYS));
        addPurchase(charlie, pVodkaSprite, 1, now.minus(1, ChronoUnit.DAYS));
        addPurchase(dave, pBeer, 5, now.minus(2, ChronoUnit.DAYS));
        addPurchase(dave, pCola, 3, now.minus(1, ChronoUnit.DAYS));
        addPurchase(dave, pCubaLibre, 2, now.minus(1, ChronoUnit.DAYS));

        // Cash register entries
        cashRepository.save(new Cash(demoUser, 200.0, "deposit", "Initial cash register"));
        cashRepository.save(new Cash(demoUser, -15.50, "purchase", "Cola x 24 + Fanta x 24"));
        cashRepository.save(new Cash(demoUser, -3.50, "pledge", "Cola case pledge"));

        log.info("Demo data seeded successfully. Login with demo/demo");
    }

    private void addStock(Ingredient ingredient, int amount, double price, Instant purchased) {
        storageRepository.save(new Storage(null, ingredient, purchased, price, amount, 0));
    }

    private void addPurchase(Account account, Product product, int amount, Instant purchased) {
        purchaseRepository.save(new Purchase(null, account, product, purchased, amount, product.getPrice(), Collections.emptyList()));
    }
}
