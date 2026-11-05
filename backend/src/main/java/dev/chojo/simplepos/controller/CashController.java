package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.controller.validation.PageNumber;
import dev.chojo.simplepos.controller.validation.PageSize;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CashDto;
import dev.chojo.simplepos.entity.dto.EmptiesReturn;
import dev.chojo.simplepos.entity.dto.TillCountDto;
import dev.chojo.simplepos.entity.dto.TillCountRequest;
import dev.chojo.simplepos.entity.dto.InventoryCorrection;
import dev.chojo.simplepos.entity.response.CashResponse;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.repository.TillCountRepository;
import dev.chojo.simplepos.service.CashService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

@RestController
@RequestMapping("/api/cash")
public class CashController {
    private final CashRepository cashRepository;
    private final PurchaseRepository purchaseRepository;
    private final BalanceRepository balanceRepository;
    private final IngredientRepository ingredientRepository;
    private final TillCountRepository tillCountRepository;
    private final CashService cashService;

    public CashController(CashRepository cashRepository, PurchaseRepository purchaseRepository, BalanceRepository balanceRepository,
                          IngredientRepository ingredientRepository, TillCountRepository tillCountRepository, CashService cashService) {
        this.tillCountRepository = tillCountRepository;
        this.cashService = cashService;
        this.cashRepository = cashRepository;
        this.purchaseRepository = purchaseRepository;
        this.balanceRepository = balanceRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@Valid @RequestBody CashDto cash) {
        var current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        cashRepository.save(cash.toNewEntry(current));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Cash> all(@RequestParam(defaultValue = "100") @PageSize int limit,
                          @RequestParam(defaultValue = "0") @PageNumber int page) {
        return cashRepository.findByOrderByCreatedDesc(Pageable.ofSize(limit).withPage(page));
    }

    @GetMapping("/total")
    public CashResponse totalCash() {
        return new CashResponse(cashService.tillTotal());
    }

    @PostMapping("/pledge/return")
    @ResponseStatus(HttpStatus.CREATED)
    public CashResponse returnEmpties(@RequestBody @NotEmpty @Size(max = 500) List<@Valid @NotNull EmptiesReturn> returns) {
        var current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new CashResponse(cashService.returnEmpties(returns, current));
    }

    @PostMapping("/count")
    @ResponseStatus(HttpStatus.CREATED)
    public TillCountDto count(@Valid @RequestBody TillCountRequest request) {
        var current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return TillCountDto.of(cashService.count(request, current));
    }

    @GetMapping("/count")
    public List<TillCountDto> counts(@RequestParam(defaultValue = "20") @PageSize int limit,
                                     @RequestParam(defaultValue = "0") @PageNumber int page) {
        return tillCountRepository.findByOrderByCreatedDescIdDesc(Pageable.ofSize(limit).withPage(page)).stream()
                .map(TillCountDto::of)
                .toList();
    }

    @GetMapping("/total/balance")
    public CashResponse balanceAmount() {
        return new CashResponse(Money.orZero(balanceRepository.totalBalance()).subtract(Money.orZero(purchaseRepository.totalAccountPurchases())));
    }

    @GetMapping("/total/pledge")
    public CashResponse pledgeAmount() {
        return new CashResponse(Money.orZero(cashRepository.totalAmount(CashType.PLEDGE)));
    }
}
