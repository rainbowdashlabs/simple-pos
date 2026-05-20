package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CashRepositoryTest {
    @Autowired
    CashRepository cashRepository;
    @Autowired
    UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(null, "cashier", "pass", true));
    }

    @Test
    void shouldSaveCashEntry() {
        Cash cash = cashRepository.save(new Cash(user, 50.0, "deposit", "Initial deposit"));
        assertNotNull(cash.getId());
        assertEquals(50.0, cash.getAmount(), 0.001);
    }

    @Test
    void shouldFindByOrderCreatedDesc() {
        cashRepository.save(new Cash(user, 10.0, "deposit", "first"));
        cashRepository.save(new Cash(user, 20.0, "deposit", "second"));
        cashRepository.save(new Cash(user, 30.0, "deposit", "third"));

        List<Cash> results = cashRepository.findByOrderByCreatedDesc(Pageable.ofSize(10));
        assertEquals(3, results.size());
        assertTrue(results.get(0).getCreated().compareTo(results.get(1).getCreated()) >= 0);
    }

    @Test
    void shouldPaginateResults() {
        for (int i = 0; i < 5; i++) {
            cashRepository.save(new Cash(user, i * 10.0, "deposit", "entry " + i));
        }
        List<Cash> page = cashRepository.findByOrderByCreatedDesc(Pageable.ofSize(2).withPage(0));
        assertEquals(2, page.size());
    }

    @Test
    void shouldCalculateTotalAmount() {
        cashRepository.save(new Cash(user, 100.0, "deposit", ""));
        cashRepository.save(new Cash(user, -30.0, "purchase", ""));

        Double total = cashRepository.totalAmount();
        assertEquals(70.0, total, 0.001);
    }

    @Test
    void shouldCalculateTotalAmountByType() {
        cashRepository.save(new Cash(user, 100.0, "deposit", ""));
        cashRepository.save(new Cash(user, -30.0, "purchase", ""));
        cashRepository.save(new Cash(user, -10.0, "pledge", ""));
        cashRepository.save(new Cash(user, -5.0, "pledge", ""));

        Double pledgeTotal = cashRepository.totalAmount("pledge");
        assertEquals(-15.0, pledgeTotal, 0.001);

        Double depositTotal = cashRepository.totalAmount("deposit");
        assertEquals(100.0, depositTotal, 0.001);
    }

    @Test
    void shouldReturnNullTotalWhenEmpty() {
        Double total = cashRepository.totalAmount();
        assertNull(total);
    }

    @Test
    void shouldReturnNullTotalByTypeWhenEmpty() {
        Double total = cashRepository.totalAmount("nonexistent");
        assertNull(total);
    }

    @Test
    void shouldPersistAllFields() {
        Cash cash = cashRepository.save(new Cash(user, 42.0, "custom_type", "A note"));
        Cash found = cashRepository.findById(cash.getId()).orElseThrow();
        assertEquals(42.0, found.getAmount(), 0.001);
        assertEquals("custom_type", found.getType());
        assertEquals("A note", found.getNote());
        assertNotNull(found.getUser());
        assertNotNull(found.getCreated());
    }
}
