package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Balance;
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
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.service.AccountService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;
    private final AccountService accountService;

    public AccountController(AccountRepository accountRepository, BalanceRepository balanceRepository, PurchaseService purchaseService, PurchaseRepository purchaseRepository, AccountService accountService) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.purchaseService = purchaseService;
        this.purchaseRepository = purchaseRepository;
        this.accountService = accountService;
    }

    @PostMapping
    ResponseEntity<Account> create(@RequestBody AccountDto account) {
        return ResponseEntity.accepted().body(accountRepository.save(new Account(null,account.getName(), Instant.now())));
    }

    @GetMapping("/{id}")
    ResponseEntity<AccountDto> get(@PathVariable int id) {
        return ResponseEntity.ok(accountRepository.findAccountById(id)
                .orElseThrow(() -> new NotFoundException("Account", id)));
    }

    @PatchMapping("/{id}")
    ResponseEntity<Account> rename(@PathVariable int id, @RequestBody AccountDto account) {
        Account existing = accountRepository.requireNotDeleted(id);
        existing.setName(account.getName());
        return ResponseEntity.ok(accountRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable int id) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        accountService.close(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    ResponseEntity<List<AccountDto>> get() {
        return ResponseEntity.ok(accountRepository.findAllAccounts());
    }

    @PostMapping("/{id}/balance")
    ResponseEntity<Balance> deposit(@PathVariable int id, @RequestBody DepositDto deposit) {
        return ResponseEntity.accepted().body(accountService.deposit(id, deposit.amount()));
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
        purchaseService.purchase(id, positions);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}/purchase")
    ResponseEntity<PurchaseHistoryDto> purchases(@PathVariable int id,
                                                 @RequestParam(defaultValue = "100") int limit,
                                                 @RequestParam(defaultValue = "0") int page) {
        accountRepository.require(id);
        List<Purchase> purchases = purchaseRepository.findAllByAccount_IdOrderByPurchasedDesc(id, PageRequest.of(page, limit));
        Map<Integer, LazyProduct> products = purchases.stream().map(Purchase::getProduct).distinct().collect(Collectors.toMap(Product::getId, Product::asLazyProduct));
        return ResponseEntity.ok(new PurchaseHistoryDto(products, purchases.stream().map(Purchase::toDto).toList()));
    }
}
