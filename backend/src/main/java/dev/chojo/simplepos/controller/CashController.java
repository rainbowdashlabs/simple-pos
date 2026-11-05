package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.controller.validation.PageNumber;
import dev.chojo.simplepos.controller.validation.PageSize;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CashDto;
import dev.chojo.simplepos.entity.dto.EmptiesReturn;
import dev.chojo.simplepos.entity.dto.TillCountDto;
import dev.chojo.simplepos.entity.dto.TillCountRequest;
import dev.chojo.simplepos.entity.response.CashResponse;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.TillCountRepository;
import dev.chojo.simplepos.service.CashService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    private final TillCountRepository tillCountRepository;
    private final CashService cashService;

    public CashController(CashRepository cashRepository,
                          TillCountRepository tillCountRepository, CashService cashService) {
        this.tillCountRepository = tillCountRepository;
        this.cashService = cashService;
        this.cashRepository = cashRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@Valid @RequestBody CashDto cash, @CurrentUser User current) {
        cashRepository.save(cash.toNewEntry(current));
    }

    @GetMapping
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
    public CashResponse returnEmpties(@RequestBody @NotEmpty @Size(max = 500) List<@Valid @NotNull EmptiesReturn> returns,
                                      @CurrentUser User current) {
        return new CashResponse(cashService.returnEmpties(returns, current));
    }

    @PostMapping("/count")
    @ResponseStatus(HttpStatus.CREATED)
    public TillCountDto count(@Valid @RequestBody TillCountRequest request, @CurrentUser User current) {
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
        return new CashResponse(cashService.accountsTotal());
    }

    @GetMapping("/total/pledge")
    public CashResponse pledgeAmount() {
        return new CashResponse(cashService.pledgeTotal());
    }
}
