package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Balance;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.dto.AccountDto;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.entity.dto.DepositDto;
import dev.chojo.simplepos.entity.dto.LazyProduct;
import dev.chojo.simplepos.entity.dto.PurchaseHistoryDto;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.service.PurchaseService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    public AccountController(AccountRepository accountRepository, BalanceRepository balanceRepository, PurchaseService purchaseService, PurchaseRepository purchaseRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.purchaseService = purchaseService;
        this.purchaseRepository = purchaseRepository;
    }

    @PostMapping("/")
    ResponseEntity<Account> create(Account account) {
        return ResponseEntity.accepted().body(accountRepository.save(account));
    }

    @GetMapping("/{id}")
    ResponseEntity<AccountDto> get(@PathVariable int id) {
        return accountRepository.findAccountById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<AccountDto> delete(@PathVariable int id) {
        boolean b = accountRepository.existsById(id);
        if (!b) return ResponseEntity.notFound().build();
        accountRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    ResponseEntity<List<AccountDto>> get() {
        return ResponseEntity.ok(accountRepository.findAllAccounts());
    }

    @PostMapping("/{id}/balance")
    ResponseEntity<Balance> deposit(@PathVariable int id, DepositDto deposit) {
        Optional<Account> byId = accountRepository.findById(id);
        if (byId.isEmpty()) return ResponseEntity.notFound().build();
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
        if (byId.isEmpty()) return ResponseEntity.notFound().build();
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
