package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.controller.validation.PageNumber;
import dev.chojo.simplepos.controller.validation.PageSize;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Balance;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.AccountDto;
import dev.chojo.simplepos.entity.dto.AccountRequest;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.entity.dto.DepositDto;
import dev.chojo.simplepos.entity.dto.PurchaseHistoryDto;
import dev.chojo.simplepos.entity.dto.PurchaseReceipt;
import dev.chojo.simplepos.exceptions.NotFoundException;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.service.AccountService;
import dev.chojo.simplepos.service.PurchaseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final PurchaseService purchaseService;
    private final AccountService accountService;

    public AccountController(AccountRepository accountRepository, BalanceRepository balanceRepository,
                             PurchaseService purchaseService, AccountService accountService) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.purchaseService = purchaseService;
        this.accountService = accountService;
    }

    @PostMapping
    ResponseEntity<Account> create(@Valid @RequestBody AccountRequest account) {
        return ResponseEntity.accepted().body(accountService.create(account.name()));
    }

    @GetMapping("/{id}")
    ResponseEntity<AccountDto> get(@PathVariable int id) {
        return ResponseEntity.ok(accountRepository.findAccountById(id)
                .orElseThrow(() -> new NotFoundException("Account", id)));
    }

    @PatchMapping("/{id}")
    ResponseEntity<Account> rename(@PathVariable int id, @Valid @RequestBody AccountRequest account) {
        return ResponseEntity.ok(accountService.rename(id, account.name()));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable int id, @CurrentUser User currentUser) {
        accountService.close(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    ResponseEntity<List<AccountDto>> get() {
        return ResponseEntity.ok(accountRepository.findAllAccounts());
    }

    @PostMapping("/{id}/balance")
    ResponseEntity<Balance> deposit(@PathVariable int id, @Valid @RequestBody DepositDto deposit) {
        return ResponseEntity.accepted().body(accountService.deposit(id, deposit.amount()));
    }

    @GetMapping("/{id}/balance")
    ResponseEntity<List<Balance>> deposit(@PathVariable int id,
                                          @RequestParam(defaultValue = "100") @PageSize int limit,
                                          @RequestParam(defaultValue = "0") @PageNumber int page) {
        var balance = balanceRepository.findAllByAccount_IdOrderByAddedDesc(id, PageRequest.of(page, limit));
        return ResponseEntity.accepted().body(balance);
    }

    @PostMapping("/{id}/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    PurchaseReceipt purchase(@PathVariable int id,
                             @RequestBody @NotEmpty @Size(max = 100) List<@Valid @NotNull CartPositionDto> positions,
                             @CurrentUser User current) {
        return new PurchaseReceipt(purchaseService.purchase(id, positions, current));
    }

    @GetMapping("/{id}/purchase")
    ResponseEntity<PurchaseHistoryDto> purchases(@PathVariable int id,
                                                 @RequestParam(defaultValue = "100") @PageSize int limit,
                                                 @RequestParam(defaultValue = "0") @PageNumber int page) {
        return ResponseEntity.ok(accountService.history(id, PageRequest.of(page, limit)));
    }
}
