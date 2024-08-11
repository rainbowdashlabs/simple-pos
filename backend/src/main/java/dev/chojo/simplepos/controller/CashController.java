package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CashDto;
import dev.chojo.simplepos.entity.response.TotalCashResponse;
import dev.chojo.simplepos.repository.CashRepository;
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

    public CashController(CashRepository cashRepository) {
        this.cashRepository = cashRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody CashDto cash) {
        var current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        cashRepository.save(cash.toNewEntry(current));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Cash> all(@RequestParam(defaultValue = "100") int limit, @RequestParam(defaultValue = "0") int page) {
        return cashRepository.findByOrderByCreatedDesc(Pageable.ofSize(limit).withPage(page));
    }

    @GetMapping("/total")
    public TotalCashResponse totalCash() {
        return new TotalCashResponse(cashRepository.totalAmount());
    }
}
