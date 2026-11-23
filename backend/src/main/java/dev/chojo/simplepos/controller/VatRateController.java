package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.response.VatRateResponse;
import dev.chojo.simplepos.repository.VatRateRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The VAT rates staff can choose from. Managing them is for administrators, see {@link AdminVatRateController}.
 */
@RestController
@RequestMapping("/api/vat-rates")
public class VatRateController {
    private final VatRateRepository repository;

    public VatRateController(VatRateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    List<VatRateResponse> active() {
        return repository.findActive().stream().map(VatRateResponse::of).toList();
    }
}
