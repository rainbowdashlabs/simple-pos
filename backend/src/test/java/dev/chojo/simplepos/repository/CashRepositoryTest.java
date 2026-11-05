package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static dev.chojo.simplepos.MoneyAssertions.assertMoney;
import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CashRepositoryTest {
    @Autowired
    CashRepository cashRepository;
    @Autowired
    EntityManager entityManager;
    @Autowired
    UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(null, "cashier", "pass", true, Role.STAFF));
    }

    @Test
    void shouldSaveCashEntry() {
        Cash cash = cashRepository.save(new Cash(user, 50.0, CashType.DEPOSIT, "Initial deposit"));
        assertNotNull(cash.getId());
        assertMoney(50.0, cash.getAmount());
    }

    @Test
    void shouldFindByOrderCreatedDesc() {
        cashRepository.save(new Cash(user, 10.0, CashType.DEPOSIT, "first"));
        cashRepository.save(new Cash(user, 20.0, CashType.DEPOSIT, "second"));
        cashRepository.save(new Cash(user, 30.0, CashType.DEPOSIT, "third"));

        List<Cash> results = cashRepository.findByOrderByCreatedDesc(Pageable.ofSize(10));
        assertEquals(3, results.size());
        assertTrue(results.get(0).getCreated().compareTo(results.get(1).getCreated()) >= 0);
    }

    @Test
    void shouldPaginateResults() {
        for (int i = 0; i < 5; i++) {
            cashRepository.save(new Cash(user, i * 10.0, CashType.DEPOSIT, "entry " + i));
        }
        List<Cash> page = cashRepository.findByOrderByCreatedDesc(Pageable.ofSize(2).withPage(0));
        assertEquals(2, page.size());
    }

    @Test
    void shouldCalculateTotalAmount() {
        cashRepository.save(new Cash(user, 100.0, CashType.DEPOSIT, ""));
        cashRepository.save(new Cash(user, -30.0, CashType.PURCHASE, ""));

        BigDecimal total = cashRepository.totalAmount();
        assertMoney(70.0, total);
    }

    @Test
    void shouldCalculateTotalAmountByType() {
        cashRepository.save(new Cash(user, 100.0, CashType.DEPOSIT, ""));
        cashRepository.save(new Cash(user, -30.0, CashType.PURCHASE, ""));
        cashRepository.save(new Cash(user, -10.0, CashType.PLEDGE, ""));
        cashRepository.save(new Cash(user, -5.0, CashType.PLEDGE, ""));

        BigDecimal pledgeTotal = cashRepository.totalAmount(CashType.PLEDGE);
        assertMoney(-15.0, pledgeTotal);

        BigDecimal depositTotal = cashRepository.totalAmount(CashType.DEPOSIT);
        assertMoney(100.0, depositTotal);
    }

    @Test
    void shouldReturnNullTotalWhenEmpty() {
        BigDecimal total = cashRepository.totalAmount();
        assertNull(total);
    }

    @Test
    void shouldReturnNullTotalByTypeWhenEmpty() {
        BigDecimal total = cashRepository.totalAmount(CashType.OTHER);
        assertNull(total);
    }

    @Test
    void shouldPersistAllFields() {
        Cash cash = cashRepository.save(new Cash(user, 42.0, CashType.OTHER, "A note"));
        Cash found = cashRepository.findById(cash.getId()).orElseThrow();
        assertMoney(42.0, found.getAmount());
        assertEquals(CashType.OTHER, found.getType());
        assertEquals("A note", found.getNote());
        assertNotNull(found.getUser());
        assertNotNull(found.getCreated());
    }

    @Test
    void typeIsStoredAsItsLowerCaseName() {
        Cash cash = cashRepository.saveAndFlush(new Cash(user, 5.0, CashType.PLEDGE, "stored"));
        Object stored = entityManager.createNativeQuery("SELECT type FROM cash WHERE id = ?1")
                .setParameter(1, cash.getId())
                .getSingleResult();
        assertEquals("pledge", stored);
    }
}
