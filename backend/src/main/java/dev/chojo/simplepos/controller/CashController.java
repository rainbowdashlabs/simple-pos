package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CashDto;
import dev.chojo.simplepos.entity.response.CashResponse;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/cash")
public class CashController {
    private final CashRepository cashRepository;
    private final PurchaseRepository purchaseRepository;
    private final BalanceRepository balanceRepository;

    public CashController(CashRepository cashRepository, PurchaseRepository purchaseRepository, BalanceRepository balanceRepository) {
        this.cashRepository = cashRepository;
        this.purchaseRepository = purchaseRepository;
        this.balanceRepository = balanceRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody CashDto cash) {
        var current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        cashRepository.save(cash.toNewEntry(current));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Cash> all(@RequestParam(defaultValue = "100") int limit,
                          @RequestParam(defaultValue = "0") int page) {
        return cashRepository.findByOrderByCreatedDesc(Pageable.ofSize(limit).withPage(page));
    }

    @GetMapping("/total")
    public CashResponse totalCash() {
        Double cash = Objects.requireNonNullElse(cashRepository.totalAmount(), 0.);
        Double balance = Objects.requireNonNullElse(purchaseRepository.totalPurchases(), 0.);
        return new CashResponse(cash + balance);
    }

    @GetMapping("/total/balance")
    public CashResponse balanceAmount() {
        Double balance = Objects.requireNonNullElse(balanceRepository.totalBalance(), 0.);
        Double purchases = Objects.requireNonNullElse(purchaseRepository.totalPurchases(), 0.);
        return new CashResponse(balance - purchases);
    }

    @GetMapping("/total/pledge")
    public CashResponse pledgeAmount() {
        Double cash = Objects.requireNonNullElse(cashRepository.totalAmount("pledge"), 0.);
        return new CashResponse(cash);
    }
}
