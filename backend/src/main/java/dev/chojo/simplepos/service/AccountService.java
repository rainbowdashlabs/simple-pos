package dev.chojo.simplepos.service;

import dev.chojo.simplepos.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Balance;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.AccountDto;
import dev.chojo.simplepos.entity.dto.LazyProduct;
import dev.chojo.simplepos.entity.dto.PurchaseHistoryDto;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Opening, renaming, paying into and closing accounts, and their purchase history.
 * <p>
 * The register total is the sum of all cash entries and all deposits, and an account's balance is its deposits minus
 * its purchases. Closing an account zeroes its balance with one balance entry. For a credit, that entry is the payout:
 * the deposit leaves the register, and nothing else is written, because a cash entry as well would take the money out
 * twice. For a debt, the same entry would count money that never came in, so a cash entry of the same amount writes the
 * debt off and the register total stays as it was.
 */
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final CashRepository cashRepository;
    private final PurchaseRepository purchaseRepository;

    public AccountService(AccountRepository accountRepository, BalanceRepository balanceRepository,
                          CashRepository cashRepository, PurchaseRepository purchaseRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.cashRepository = cashRepository;
        this.purchaseRepository = purchaseRepository;
    }

    /**
     * Pays money into an account.
     *
     * @throws NotFoundException if the account does not exist or is deleted
     */
    @Transactional
    public Balance deposit(int id, BigDecimal amount) {
        Account account = accountRepository.requireNotDeletedLocked(id);
        return balanceRepository.save(new Balance(null, account, Instant.now(), amount));
    }

    /**
     * Settles an account's balance, anonymises it and marks it deleted, in one transaction.
     *
     * @throws NotFoundException if the account does not exist or is already deleted
     */
    @Transactional
    public void close(int id, User closedBy) {
        Account account = accountRepository.requireNotDeletedLocked(id);
        BigDecimal balance = accountRepository.findAccountById(id).map(AccountDto::getBalance).orElse(Money.ZERO);

        if (balance.signum() != 0) {
            balanceRepository.save(new Balance(null, account, Instant.now(), balance.negate()));
        }
        if (balance.signum() < 0) {
            String note = "Debt written off, account closed: %s".formatted(account.getName());
            cashRepository.save(new Cash(closedBy, balance, CashType.BALANCE, note));
        }

        account.setName("Deleted Account #" + account.getId());
        account.setDeleted(true);
        accountRepository.save(account);
    }

    /**
     * Opens an account with the given name.
     */
    @Transactional
    public Account create(String name) {
        return accountRepository.save(new Account(null, name, Instant.now()));
    }

    /**
     * Renames an account under its row lock, so a rename cannot bring back an account closed meanwhile.
     *
     * @throws NotFoundException if the account does not exist or is closed
     */
    @Transactional
    public Account rename(int id, String name) {
        Account account = accountRepository.requireNotDeletedLocked(id);
        account.setName(name);
        return account;
    }

    /**
     * An account's purchases, newest first, with the products they name.
     *
     * @throws NotFoundException if the account does not exist
     */
    @Transactional(readOnly = true)
    public PurchaseHistoryDto history(int id, Pageable page) {
        accountRepository.require(id);
        List<Purchase> purchases = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(id, page);
        Map<Integer, LazyProduct> products = purchases.stream()
                .map(Purchase::getProduct)
                .collect(Collectors.toMap(Product::getId, Product::asLazyProduct, (first, same) -> first));
        return new PurchaseHistoryDto(products, purchases.stream().map(Purchase::toDto).toList());
    }
}
