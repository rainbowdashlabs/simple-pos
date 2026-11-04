package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Balance;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Closing an account must leave the register total right: a credit is paid out, a debt is written off without money
 * appearing.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountServiceTest {
    @Autowired
    AccountService accountService;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    BalanceRepository balanceRepository;
    @Autowired
    CashRepository cashRepository;
    @Autowired
    PurchaseRepository purchaseRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductService productService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager entityManager;

    private User cashier;
    private Account account;

    @BeforeEach
    void setUp() {
        cashier = userRepository.save(new User(null, "closingcashier", "x", true, Role.STAFF));
        account = accountRepository.save(new Account(null, "Closing", Instant.now()));
    }

    @Test
    void shouldPayOutACreditOnce() {
        balanceRepository.save(new Balance(null, account, Instant.now(), 20));
        double before = registerTotal();

        accountService.close(account.getId(), cashier);

        assertEquals(before - 20, registerTotal(), 0.001);
        assertEquals(0, balanceOf(account), 0.001);
        assertEquals(List.of(), closingEntries());
    }

    @Test
    void shouldWriteOffADebtWithoutChangingTheRegister() {
        Category category = categoryRepository.save(new Category(null, "ClosingCat"));
        Product product = productService.create(new Product(null, category, "ClosingProduct", 5, true,
                new Recipe(null, null, Collections.emptyList())));
        purchaseRepository.save(new Purchase(null, account, product, Instant.now(), 1, 5, List.of()));
        double before = registerTotal();

        accountService.close(account.getId(), cashier);

        assertEquals(before, registerTotal(), 0.001);
        assertEquals(0, balanceOf(account), 0.001);
        assertEquals(List.of(-5.0), closingEntries().stream().map(Cash::getAmount).toList());
    }

    @Test
    void shouldRefuseToCloseAClosedAccount() {
        accountService.close(account.getId(), cashier);

        assertThrows(NotFoundException.class, () -> accountService.close(account.getId(), cashier));
    }

    @Test
    void shouldAnonymiseAndDeleteTheAccount() {
        accountService.close(account.getId(), cashier);

        entityManager.flush();
        entityManager.clear();
        Account closed = accountRepository.findById(account.getId()).orElseThrow();
        assertTrue(closed.isDeleted());
        assertEquals("Deleted Account #" + account.getId(), closed.getName());
    }

    private List<Cash> closingEntries() {
        entityManager.flush();
        return cashRepository.findAll().stream()
                .filter(cash -> "balance".equals(cash.getType()) && cash.getNote() != null && cash.getNote().contains("Closing"))
                .toList();
    }

    private double registerTotal() {
        entityManager.flush();
        return Objects.requireNonNullElse(cashRepository.totalAmount(), 0.)
                + Objects.requireNonNullElse(balanceRepository.totalBalance(), 0.);
    }

    private double balanceOf(Account account) {
        entityManager.flush();
        return accountRepository.findAccountById(account.getId()).orElseThrow().getBalance();
    }
}
