package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Balance;
import dev.chojo.simplepos.entity.dto.AccountDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountRepositoryTest {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    BalanceRepository balanceRepository;
    @Autowired
    PurchaseRepository purchaseRepository;

    @Test
    void shouldSaveAccount() {
        Account account = accountRepository.save(new Account(null, "John", Instant.now()));
        assertNotNull(account.getId());
        assertEquals("John", account.getName());
    }

    @Test
    void shouldFindAccountByIdWithZeroBalance() {
        Account account = accountRepository.save(new Account(null, "Jane", Instant.now()));
        Optional<AccountDto> found = accountRepository.findAccountById(account.getId());
        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getName());
        assertEquals(0.0, found.get().getBalance(), 0.001);
    }

    @Test
    void shouldFindAccountByIdWithBalance() {
        Account account = accountRepository.save(new Account(null, "Funded", Instant.now()));
        balanceRepository.save(new Balance(null, account, Instant.now(), 50.0));
        balanceRepository.save(new Balance(null, account, Instant.now(), 25.0));

        Optional<AccountDto> found = accountRepository.findAccountById(account.getId());
        assertTrue(found.isPresent());
        assertEquals(75.0, found.get().getBalance(), 0.001);
    }

    @Test
    void shouldReturnEmptyForMissingAccount() {
        Optional<AccountDto> found = accountRepository.findAccountById(999999);
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindAllAccounts() {
        accountRepository.save(new Account(null, "Alice", Instant.now()));
        accountRepository.save(new Account(null, "Bob", Instant.now()));

        List<AccountDto> all = accountRepository.findAllAccounts();
        assertEquals(2, all.size());
    }

    @Test
    void shouldFindAllAccountsSortedByName() {
        accountRepository.save(new Account(null, "Zara", Instant.now()));
        accountRepository.save(new Account(null, "Anna", Instant.now()));

        List<AccountDto> all = accountRepository.findAllAccounts();
        assertEquals("Anna", all.get(0).getName());
        assertEquals("Zara", all.get(1).getName());
    }

    @Test
    void shouldDeleteAccount() {
        Account account = accountRepository.save(new Account(null, "Deletable", Instant.now()));
        accountRepository.deleteById(account.getId());
        assertFalse(accountRepository.existsById(account.getId()));
    }

    @Test
    void shouldCheckExistsById() {
        Account account = accountRepository.save(new Account(null, "Exists", Instant.now()));
        assertTrue(accountRepository.existsById(account.getId()));
        assertFalse(accountRepository.existsById(999999));
    }
}
