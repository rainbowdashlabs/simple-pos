package dev.chojo.simplepos.configuration;

import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Balance;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.repository.UserRepository;
import dev.chojo.simplepos.service.ProductService;
import dev.chojo.simplepos.service.PurchaseService;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String DEMO_USER = "demo";

    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductService productService;
    private final StorageRepository storageRepository;
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final PurchaseService purchaseService;
    private final CashRepository cashRepository;
    private final boolean resetDatabase;
    private User seller;

    public DemoDataSeeder(EntityManager entityManager, UserRepository userRepository, PasswordEncoder passwordEncoder,
                          CategoryRepository categoryRepository, IngredientRepository ingredientRepository,
                          ProductService productService, StorageRepository storageRepository,
                          AccountRepository accountRepository, BalanceRepository balanceRepository,
                          PurchaseService purchaseService, CashRepository cashRepository,
                          @Value("${demo.reset-database:false}") boolean resetDatabase) {
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.productService = productService;
        this.storageRepository = storageRepository;
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.purchaseService = purchaseService;
        this.cashRepository = cashRepository;
        this.resetDatabase = resetDatabase;
    }

    private boolean holdsRealUsers() {
        return userRepository.findAll().stream().anyMatch(user -> !DEMO_USER.equals(user.getUsername()));
    }

    /**
     * Wipes the database and fills it with demo data.
     * <p>
     * Refuses to wipe a database that holds users other than the demo user, unless {@code demo.reset-database} is
     * set: demo mode switched on by accident against a real database would otherwise delete everything in it on
     * startup.
     *
     * @throws IllegalStateException if the database holds real users and resetting was not explicitly allowed
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (!resetDatabase && holdsRealUsers()) {
            throw new IllegalStateException("demo.enabled is set, but the database holds users other than the demo "
                    + "user. Refusing to wipe it. Set demo.reset-database: true if this database really is a demo.");
        }
        log.info("Wiping database for demo mode...");
        entityManager.createNativeQuery("DO $$ DECLARE r RECORD; BEGIN " +
                "FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history') LOOP " +
                "EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE'; END LOOP; " +
                "END $$").executeUpdate();
        entityManager.flush();

        log.info("Seeding demo data...");

        User demoUser = userRepository.save(
                new User(null, DEMO_USER, passwordEncoder.encode("demo"), true, Role.STAFF));
        seller = demoUser;

        Category drinks = categoryRepository.save(new Category(null, "Drinks"));
        Category softDrinks = categoryRepository.save(new Category(null, "Soft Drinks"));
        Category snacks = categoryRepository.save(new Category(null, "Snacks"));
        Category spirits = categoryRepository.save(new Category(null, "Spirits"));
        Category cocktails = categoryRepository.save(new Category(null, "Cocktails"));
        Category hotDrinks = categoryRepository.save(new Category(null, "Hot Drinks"));

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
        Ingredient gin = ingredientRepository.save(
                new Ingredient(null, spirits, "Gin", 0.70, 0, 0, 0, 2));
        Ingredient whiskey = ingredientRepository.save(
                new Ingredient(null, spirits, "Whiskey", 0.80, 0, 0, 0, 2));
        Ingredient tonic = ingredientRepository.save(
                new Ingredient(null, softDrinks, "Tonic Water", 0.40, 24, 0.25, 3.00, 24));
        Ingredient orangeJuice = ingredientRepository.save(
                new Ingredient(null, softDrinks, "Orange Juice", 0.50, 12, 0, 0, 12));
        Ingredient appleJuice = ingredientRepository.save(
                new Ingredient(null, softDrinks, "Apple Juice", 0.50, 12, 0, 0, 12));
        Ingredient energyDrink = ingredientRepository.save(
                new Ingredient(null, softDrinks, "Energy Drink", 0.90, 24, 0.25, 6.00, 24));
        Ingredient icedTea = ingredientRepository.save(
                new Ingredient(null, softDrinks, "Iced Tea", 0.40, 24, 0.25, 3.00, 24));
        Ingredient pretzel = ingredientRepository.save(
                new Ingredient(null, snacks, "Pretzel", 0.60, 0, 0, 0, 10));
        Ingredient nachos = ingredientRepository.save(
                new Ingredient(null, snacks, "Nachos", 1.00, 0, 0, 0, 5));
        Ingredient coffee = ingredientRepository.save(
                new Ingredient(null, hotDrinks, "Coffee", 0.30, 0, 0, 0, 50));
        Ingredient hotChocolate = ingredientRepository.save(
                new Ingredient(null, hotDrinks, "Hot Chocolate", 0.40, 0, 0, 0, 30));

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

        Product pTonic = productService.create(new Product(null, softDrinks, "Tonic Water", 1.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, tonic, 1)))));
        Product pOJ = productService.create(new Product(null, softDrinks, "Orange Juice", 2.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, orangeJuice, 1)))));
        Product pAppleJuice = productService.create(new Product(null, softDrinks, "Apple Juice", 2.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, appleJuice, 1)))));
        Product pEnergy = productService.create(new Product(null, softDrinks, "Energy Drink", 3.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, energyDrink, 1)))));
        Product pIcedTea = productService.create(new Product(null, softDrinks, "Iced Tea", 1.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, icedTea, 1)))));
        Product pPretzel = productService.create(new Product(null, snacks, "Pretzel", 1.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, pretzel, 1)))));
        Product pNachos = productService.create(new Product(null, snacks, "Nachos", 2.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, nachos, 1)))));
        Product pCoffee = productService.create(new Product(null, hotDrinks, "Coffee", 2.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, coffee, 1)))));
        Product pHotChoc = productService.create(new Product(null, hotDrinks, "Hot Chocolate", 2.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, hotChocolate, 1)))));
        Product pWhiskey = productService.create(new Product(null, spirits, "Whiskey", 4.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, whiskey, 1)))));

        Product pCubaLibre = productService.create(new Product(null, cocktails, "Cuba Libre", 4.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, rum, 1), new RecipeEntry(null, cola, 1)))));
        Product pVodkaSprite = productService.create(new Product(null, cocktails, "Vodka Sprite", 4.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, vodka, 1), new RecipeEntry(null, sprite, 1)))));
        Product pGinTonic = productService.create(new Product(null, cocktails, "Gin & Tonic", 4.50, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, gin, 1), new RecipeEntry(null, tonic, 1)))));
        Product pScrewdriver = productService.create(new Product(null, cocktails, "Screwdriver", 4.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, vodka, 1), new RecipeEntry(null, orangeJuice, 1)))));
        Product pVodkaEnergy = productService.create(new Product(null, cocktails, "Vodka Energy", 5.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, vodka, 1), new RecipeEntry(null, energyDrink, 1)))));
        Product pWhiskeyCola = productService.create(new Product(null, cocktails, "Whiskey Cola", 5.00, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, whiskey, 1), new RecipeEntry(null, cola, 1)))));

        productService.create(new Product(null, drinks, "Old Lemonade", 1.00, false,
                new Recipe(null, null, Collections.emptyList())));

        Instant now = Instant.now();
        Instant weekAgo = now.minus(7, ChronoUnit.DAYS);
        Instant delivered = now.minus(30, ChronoUnit.DAYS);
        addStock(cola, 48, 0.45, delivered);
        addStock(fanta, 48, 0.45, delivered);
        addStock(sprite, 48, 0.45, delivered);
        addStock(water, 24, 0.20, delivered);
        addStock(beer, 40, 0.80, delivered);
        addStock(beerSmall, 48, 0.65, delivered);
        addStock(chips, 10, 1.20, delivered);
        addStock(peanuts, 8, 0.90, delivered);
        addStock(vodka, 15, 0.50, delivered);
        addStock(rum, 10, 0.60, delivered);
        addStock(gin, 8, 0.70, delivered);
        addStock(whiskey, 8, 0.80, delivered);
        addStock(tonic, 48, 0.40, delivered);
        addStock(orangeJuice, 24, 0.50, delivered);
        addStock(appleJuice, 24, 0.50, delivered);
        addStock(energyDrink, 24, 0.90, delivered);
        addStock(icedTea, 48, 0.40, delivered);
        addStock(pretzel, 20, 0.60, delivered);
        addStock(nachos, 10, 1.00, delivered);
        addStock(coffee, 100, 0.30, delivered);
        addStock(hotChocolate, 50, 0.40, delivered);

        Account alice = accountRepository.save(new Account(null, "Alice", now.minus(30, ChronoUnit.DAYS)));
        Account bob = accountRepository.save(new Account(null, "Bob", now.minus(20, ChronoUnit.DAYS)));
        Account charlie = accountRepository.save(new Account(null, "Charlie", now.minus(10, ChronoUnit.DAYS)));
        Account dave = accountRepository.save(new Account(null, "Dave", now.minus(5, ChronoUnit.DAYS)));
        Account eve = accountRepository.save(new Account(null, "Eve", now.minus(28, ChronoUnit.DAYS)));
        Account frank = accountRepository.save(new Account(null, "Frank", now.minus(25, ChronoUnit.DAYS)));
        Account grace = accountRepository.save(new Account(null, "Grace", now.minus(18, ChronoUnit.DAYS)));
        Account heidi = accountRepository.save(new Account(null, "Heidi", now.minus(14, ChronoUnit.DAYS)));
        Account ivan = accountRepository.save(new Account(null, "Ivan", now.minus(12, ChronoUnit.DAYS)));
        Account judy = accountRepository.save(new Account(null, "Judy", now.minus(7, ChronoUnit.DAYS)));

        balanceRepository.save(new Balance(null, alice, now.minus(25, ChronoUnit.DAYS), 50.0));
        balanceRepository.save(new Balance(null, alice, now.minus(10, ChronoUnit.DAYS), 30.0));
        balanceRepository.save(new Balance(null, bob, now.minus(15, ChronoUnit.DAYS), 20.0));
        balanceRepository.save(new Balance(null, bob, now.minus(3, ChronoUnit.DAYS), 40.0));
        balanceRepository.save(new Balance(null, charlie, now.minus(8, ChronoUnit.DAYS), 25.0));
        balanceRepository.save(new Balance(null, dave, now.minus(4, ChronoUnit.DAYS), 100.0));
        balanceRepository.save(new Balance(null, eve, now.minus(26, ChronoUnit.DAYS), 40.0));
        balanceRepository.save(new Balance(null, eve, now.minus(5, ChronoUnit.DAYS), 20.0));
        balanceRepository.save(new Balance(null, frank, now.minus(20, ChronoUnit.DAYS), 60.0));
        balanceRepository.save(new Balance(null, grace, now.minus(15, ChronoUnit.DAYS), 35.0));
        balanceRepository.save(new Balance(null, heidi, now.minus(12, ChronoUnit.DAYS), 30.0));
        balanceRepository.save(new Balance(null, ivan, now.minus(10, ChronoUnit.DAYS), 50.0));
        balanceRepository.save(new Balance(null, judy, now.minus(6, ChronoUnit.DAYS), 25.0));

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
        addPurchase(eve, pBeer, 4, now.minus(20, ChronoUnit.DAYS));
        addPurchase(eve, pCola, 3, now.minus(15, ChronoUnit.DAYS));
        addPurchase(eve, pChips, 2, now.minus(10, ChronoUnit.DAYS));
        addPurchase(eve, pVodkaSprite, 1, now.minus(3, ChronoUnit.DAYS));
        addPurchase(frank, pBeer, 6, now.minus(18, ChronoUnit.DAYS));
        addPurchase(frank, pCubaLibre, 3, now.minus(12, ChronoUnit.DAYS));
        addPurchase(frank, pPeanuts, 2, now.minus(8, ChronoUnit.DAYS));
        addPurchase(frank, pFanta, 1, now.minus(2, ChronoUnit.DAYS));
        addPurchase(grace, pWater, 4, now.minus(14, ChronoUnit.DAYS));
        addPurchase(grace, pSprite, 3, now.minus(10, ChronoUnit.DAYS));
        addPurchase(grace, pBeerSmall, 2, now.minus(5, ChronoUnit.DAYS));
        addPurchase(grace, pChips, 1, now.minus(1, ChronoUnit.DAYS));
        addPurchase(heidi, pCola, 5, now.minus(11, ChronoUnit.DAYS));
        addPurchase(heidi, pBeer, 2, now.minus(7, ChronoUnit.DAYS));
        addPurchase(heidi, pCubaLibre, 1, now.minus(3, ChronoUnit.DAYS));
        addPurchase(ivan, pBeerSmall, 6, now.minus(9, ChronoUnit.DAYS));
        addPurchase(ivan, pFanta, 3, now.minus(6, ChronoUnit.DAYS));
        addPurchase(ivan, pVodkaSprite, 2, now.minus(2, ChronoUnit.DAYS));
        addPurchase(ivan, pPeanuts, 1, now.minus(1, ChronoUnit.DAYS));
        addPurchase(judy, pBeer, 3, now.minus(5, ChronoUnit.DAYS));
        addPurchase(judy, pCola, 2, now.minus(3, ChronoUnit.DAYS));
        addPurchase(judy, pChips, 2, now.minus(1, ChronoUnit.DAYS));

        addPurchase(alice, pGinTonic, 2, now.minus(6, ChronoUnit.DAYS));
        addPurchase(alice, pCoffee, 3, now.minus(2, ChronoUnit.DAYS));
        addPurchase(alice, pNachos, 1, now.minus(1, ChronoUnit.DAYS));
        addPurchase(bob, pEnergy, 3, now.minus(6, ChronoUnit.DAYS));
        addPurchase(bob, pVodkaEnergy, 2, now.minus(3, ChronoUnit.DAYS));
        addPurchase(bob, pPretzel, 2, now.minus(1, ChronoUnit.DAYS));
        addPurchase(charlie, pOJ, 2, now.minus(5, ChronoUnit.DAYS));
        addPurchase(charlie, pScrewdriver, 1, now.minus(2, ChronoUnit.DAYS));
        addPurchase(charlie, pCoffee, 2, now.minus(1, ChronoUnit.DAYS));
        addPurchase(dave, pGinTonic, 3, now.minus(3, ChronoUnit.DAYS));
        addPurchase(dave, pWhiskey, 2, now.minus(2, ChronoUnit.DAYS));
        addPurchase(dave, pWhiskeyCola, 1, now.minus(1, ChronoUnit.DAYS));
        addPurchase(eve, pIcedTea, 4, now.minus(8, ChronoUnit.DAYS));
        addPurchase(eve, pAppleJuice, 2, now.minus(4, ChronoUnit.DAYS));
        addPurchase(eve, pHotChoc, 1, now.minus(1, ChronoUnit.DAYS));
        addPurchase(frank, pEnergy, 2, now.minus(10, ChronoUnit.DAYS));
        addPurchase(frank, pVodkaEnergy, 3, now.minus(5, ChronoUnit.DAYS));
        addPurchase(frank, pNachos, 2, now.minus(1, ChronoUnit.DAYS));
        addPurchase(grace, pCoffee, 4, now.minus(8, ChronoUnit.DAYS));
        addPurchase(grace, pTonic, 2, now.minus(4, ChronoUnit.DAYS));
        addPurchase(grace, pGinTonic, 1, now.minus(2, ChronoUnit.DAYS));
        addPurchase(heidi, pAppleJuice, 3, now.minus(9, ChronoUnit.DAYS));
        addPurchase(heidi, pPretzel, 2, now.minus(5, ChronoUnit.DAYS));
        addPurchase(heidi, pScrewdriver, 2, now.minus(2, ChronoUnit.DAYS));
        addPurchase(ivan, pWhiskeyCola, 2, now.minus(7, ChronoUnit.DAYS));
        addPurchase(ivan, pIcedTea, 3, now.minus(4, ChronoUnit.DAYS));
        addPurchase(ivan, pHotChoc, 2, now.minus(1, ChronoUnit.DAYS));
        addPurchase(judy, pOJ, 3, now.minus(4, ChronoUnit.DAYS));
        addPurchase(judy, pWhiskey, 1, now.minus(2, ChronoUnit.DAYS));
        addPurchase(judy, pCoffee, 2, now.minus(1, ChronoUnit.DAYS));

        cashRepository.save(new Cash(demoUser, 200.0, CashType.DEPOSIT, "Initial cash register"));
        cashRepository.save(new Cash(demoUser, -15.50, CashType.PURCHASE, "Cola x 24 + Fanta x 24"));
        cashRepository.save(new Cash(demoUser, -3.50, CashType.PLEDGE, "Cola case pledge"));

        log.info("Demo data seeded successfully. Login with demo/demo");
    }

    private void addStock(Ingredient ingredient, int amount, double price, Instant purchased) {
        storageRepository.save(new Storage(null, ingredient, purchased, price, amount, 0));
    }

    private void addPurchase(Account account, Product product, int amount, Instant purchased) {
        purchaseService.purchase(account.getId(), List.of(new CartPositionDto(product, amount)), seller, purchased);
    }
}
