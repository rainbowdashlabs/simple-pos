package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Balance;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BalanceRepositoryTest {
    @Autowired
    BalanceRepository balanceRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    PurchaseRepository purchaseRepository;

    private Account account;

    @BeforeEach
    void setUp() {
        account = accountRepository.save(new Account(null, "BalanceTestAccount", Instant.now()));
    }

    @Test
    void shouldSaveBalance() {
        Balance balance = balanceRepository.save(new Balance(null, account, Instant.now(), 100.0));
        assertNotNull(balance.getId());
        assertEquals(100.0, balance.getAmount(), 0.001);
    }

    @Test
    void shouldFindByAccountIdOrderedByAddedDesc() {
        Instant now = Instant.now();
        balanceRepository.save(new Balance(null, account, now.minus(2, ChronoUnit.HOURS), 10.0));
        balanceRepository.save(new Balance(null, account, now.minus(1, ChronoUnit.HOURS), 20.0));
        balanceRepository.save(new Balance(null, account, now, 30.0));

        List<Balance> results = balanceRepository.findAllByAccount_IdOrderByAddedDesc(account.getId(), PageRequest.of(0, 10));
        assertEquals(3, results.size());
        assertEquals(30.0, results.get(0).getAmount(), 0.001);
        assertEquals(20.0, results.get(1).getAmount(), 0.001);
        assertEquals(10.0, results.get(2).getAmount(), 0.001);
    }

    @Test
    void shouldPaginateResults() {
        for (int i = 0; i < 5; i++) {
            balanceRepository.save(new Balance(null, account, Instant.now().minus(i, ChronoUnit.HOURS), i * 10.0));
        }

        List<Balance> page = balanceRepository.findAllByAccount_IdOrderByAddedDesc(account.getId(), PageRequest.of(0, 2));
        assertEquals(2, page.size());
    }

    @Test
    void shouldCalculateTotalBalance() {
        balanceRepository.save(new Balance(null, account, Instant.now(), 50.0));
        balanceRepository.save(new Balance(null, account, Instant.now(), 30.0));

        Double total = balanceRepository.totalBalance();
        assertEquals(80.0, total, 0.001);
    }

    @Test
    void shouldReturnNullTotalBalanceWhenEmpty() {
        Double total = balanceRepository.totalBalance();
        assertNull(total);
    }

    @Test
    void shouldCalculateTotalDebt() {
        Account debtor1 = accountRepository.save(new Account(null, "Debtor1", Instant.now()));
        Account debtor2 = accountRepository.save(new Account(null, "Debtor2", Instant.now()));

        balanceRepository.save(new Balance(null, debtor1, Instant.now(), -20.0));
        balanceRepository.save(new Balance(null, debtor2, Instant.now(), -30.0));
        balanceRepository.save(new Balance(null, account, Instant.now(), 100.0));

        Double debt = balanceRepository.totalDept();
        assertNotNull(debt);
        assertEquals(-50.0, debt, 0.001);
    }

    @Test
    void shouldNotReturnResultsForDifferentAccount() {
        Account other = accountRepository.save(new Account(null, "Other", Instant.now()));
        balanceRepository.save(new Balance(null, other, Instant.now(), 100.0));

        List<Balance> results = balanceRepository.findAllByAccount_IdOrderByAddedDesc(account.getId(), PageRequest.of(0, 10));
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldHandleNegativeBalances() {
        balanceRepository.save(new Balance(null, account, Instant.now(), -15.0));
        Balance found = balanceRepository.findAllByAccount_IdOrderByAddedDesc(account.getId(), PageRequest.of(0, 1)).get(0);
        assertEquals(-15.0, found.getAmount(), 0.001);
    }
}
