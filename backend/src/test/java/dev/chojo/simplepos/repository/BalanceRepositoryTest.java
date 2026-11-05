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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static dev.chojo.simplepos.MoneyAssertions.assertMoney;
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
        assertMoney(100.0, balance.getAmount());
    }

    @Test
    void shouldFindByAccountIdOrderedByAddedDesc() {
        Instant now = Instant.now();
        balanceRepository.save(new Balance(null, account, now.minus(2, ChronoUnit.HOURS), 10.0));
        balanceRepository.save(new Balance(null, account, now.minus(1, ChronoUnit.HOURS), 20.0));
        balanceRepository.save(new Balance(null, account, now, 30.0));

        List<Balance> results = balanceRepository.findAllByAccount_IdOrderByAddedDesc(account.getId(), PageRequest.of(0, 10));
        assertEquals(3, results.size());
        assertMoney(30.0, results.get(0).getAmount());
        assertMoney(20.0, results.get(1).getAmount());
        assertMoney(10.0, results.get(2).getAmount());
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

        BigDecimal total = balanceRepository.totalBalance();
        assertMoney(80.0, total);
    }

    @Test
    void shouldReturnNullTotalBalanceWhenEmpty() {
        BigDecimal total = balanceRepository.totalBalance();
        assertNull(total);
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
        assertMoney(-15.0, found.getAmount());
    }
}
