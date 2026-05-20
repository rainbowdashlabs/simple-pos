package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Balance;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.AccountDto;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.entity.dto.DepositDto;
import dev.chojo.simplepos.entity.dto.LazyProduct;
import dev.chojo.simplepos.entity.dto.PurchaseHistoryDto;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.service.PurchaseService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;
    private final CashRepository cashRepository;

    public AccountController(AccountRepository accountRepository, BalanceRepository balanceRepository, PurchaseService purchaseService, PurchaseRepository purchaseRepository, CashRepository cashRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.purchaseService = purchaseService;
        this.purchaseRepository = purchaseRepository;
        this.cashRepository = cashRepository;
    }

    @PostMapping
    ResponseEntity<Account> create(@RequestBody AccountDto account) {
        return ResponseEntity.accepted().body(accountRepository.save(new Account(null,account.getName(), Instant.now())));
    }

    @GetMapping("/{id}")
    ResponseEntity<AccountDto> get(@PathVariable int id) {
        return accountRepository.findAccountById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    ResponseEntity<Account> rename(@PathVariable int id, @RequestBody AccountDto account) {
        Optional<Account> byId = accountRepository.findById(id);
        if (byId.isEmpty()) return ResponseEntity.notFound().build();
        Account existing = byId.get();
        if (existing.isDeleted()) return ResponseEntity.notFound().build();
        existing.setName(account.getName());
        return ResponseEntity.ok(accountRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable int id) {
        Optional<Account> byId = accountRepository.findById(id);
        if (byId.isEmpty()) return ResponseEntity.notFound().build();
        Account account = byId.get();
        if (account.isDeleted()) return ResponseEntity.notFound().build();

        // Calculate remaining balance
        Optional<AccountDto> accountDto = accountRepository.findAccountById(id);
        double remainingBalance = accountDto.map(AccountDto::getBalance).orElse(0.0);

        // If there is remaining balance (positive or negative), settle it via cash
        if (remainingBalance != 0) {
            var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            // Positive balance: account had money left, return it from cash register
            // Negative balance (debt): account owed money, register absorbs the loss
            String note = "Account closed: %s".formatted(account.getName());
            cashRepository.save(new Cash(currentUser, -remainingBalance, "balance", note));
            // Zero out the account balance by creating an offsetting balance entry
            balanceRepository.save(new Balance(null, account, Instant.now(), -remainingBalance));
        }

        // Anonymize and soft-delete
        account.setName("Deleted Account #" + account.getId());
        account.setDeleted(true);
        accountRepository.save(account);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    ResponseEntity<List<AccountDto>> get() {
        return ResponseEntity.ok(accountRepository.findAllAccounts());
    }

    @PostMapping("/{id}/balance")
    ResponseEntity<Balance> deposit(@PathVariable int id, @RequestBody DepositDto deposit) {
        Optional<Account> byId = accountRepository.findById(id);
        if (byId.isEmpty() || byId.get().isDeleted()) return ResponseEntity.notFound().build();
        var balance = balanceRepository.save(new Balance(null, byId.get(), Instant.now(), deposit.amount()));
        return ResponseEntity.accepted().body(balance);
    }

    @GetMapping("/{id}/balance")
    ResponseEntity<List<Balance>> deposit(@PathVariable int id,
                                          @RequestParam(defaultValue = "100") int limit,
                                          @RequestParam(defaultValue = "0") int page) {
        var balance = balanceRepository.findAllByAccount_IdOrderByAddedDesc(id, PageRequest.of(page, limit));
        return ResponseEntity.accepted().body(balance);
    }

    @PostMapping("/{id}/purchase")
    ResponseEntity<Void> purchase(@PathVariable int id, @RequestBody List<CartPositionDto> positions) {
        Optional<Account> byId = accountRepository.findById(id);
        if (byId.isEmpty() || byId.get().isDeleted()) return ResponseEntity.notFound().build();
        purchaseService.purchase(byId.get(), positions);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}/purchase")
    ResponseEntity<PurchaseHistoryDto> purchases(@PathVariable int id,
                                                 @RequestParam(defaultValue = "100") int limit,
                                                 @RequestParam(defaultValue = "0") int page) {
        Optional<Account> byId = accountRepository.findById(id);
        if (byId.isEmpty()) return ResponseEntity.notFound().build();
        List<Purchase> purchases = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(id, PageRequest.of(page, limit));
        Map<Integer, LazyProduct> products = purchases.stream().map(Purchase::getProduct).distinct().collect(Collectors.toMap(Product::getId, Product::asLazyProduct));
        return ResponseEntity.ok(new PurchaseHistoryDto(products, purchases.stream().map(Purchase::toDto).toList()));
    }
}
