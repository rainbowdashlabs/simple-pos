package dev.chojo.simplepos.configuration;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DemoDataSeederTest {
    @Autowired
    EntityManager entityManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    ProductService productService;
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    BalanceRepository balanceRepository;
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    CashRepository cashRepository;

    @Test
    void shouldRefuseToWipeADatabaseWithRealUsers() {
        userRepository.save(new User(null, "realcashier", "x", true, Role.STAFF));

        IllegalStateException refused = assertThrows(IllegalStateException.class, () -> seeder(false).run());

        assertTrue(refused.getMessage().contains("demo.reset-database"));
        assertTrue(userRepository.findByUsername("realcashier").isPresent());
    }

    @Test
    void shouldKeepWipingADatabaseThatOnlyHoldsTheDemoUser() {
        userRepository.deleteAll();
        userRepository.save(new User(null, "demo", "x", true, Role.STAFF));
        entityManager.flush();
        entityManager.clear();

        seeder(false).run();

        assertEquals(1, userRepository.count());
        assertTrue(userRepository.findByUsername("demo").isPresent());
        assertTrue(categoryRepository.count() > 0);
        assertTrue(entityManager.createQuery("SELECT count(pp) FROM PurchasePart pp", Long.class).getSingleResult() > 0);
        assertTrue(entityManager.createQuery("SELECT sum(p.cost) FROM Purchase p", BigDecimal.class).getSingleResult().signum() > 0);
        assertTrue(entityManager.createQuery("SELECT min(p.purchased) FROM Purchase p", Instant.class).getSingleResult()
                .isBefore(Instant.now().minus(1, ChronoUnit.DAYS)));
        assertEquals(0, entityManager.createQuery(
                "SELECT count(p) FROM Purchase p WHERE p.soldBy IS NULL OR p.soldBy.username <> 'demo'", Long.class).getSingleResult());
    }

    @Test
    void shouldWipeRealUsersWhenResettingIsExplicitlyAllowed() {
        userRepository.save(new User(null, "realcashier", "x", true, Role.STAFF));

        seeder(true).run();

        assertTrue(userRepository.findByUsername("realcashier").isEmpty());
        assertTrue(userRepository.findByUsername("demo").isPresent());
    }

    private DemoDataSeeder seeder(boolean resetDatabase) {
        return new DemoDataSeeder(entityManager, userRepository, passwordEncoder, categoryRepository,
                ingredientRepository, productService, storageRepository, accountRepository, balanceRepository,
                purchaseService, cashRepository, resetDatabase);
    }
}
