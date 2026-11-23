package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.dto.VatRateRequest;
import dev.chojo.simplepos.entity.response.VatRateResponse;
import dev.chojo.simplepos.repository.VatRateRepository;
import dev.chojo.simplepos.service.VatRateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * VAT rate management for administrators. Access is restricted to ADMIN by the security configuration.
 */
@RestController
@RequestMapping("/api/admin/vat-rates")
public class AdminVatRateController {
    private final VatRateRepository repository;
    private final VatRateService service;

    public AdminVatRateController(VatRateRepository repository, VatRateService service) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping
    @Transactional(readOnly = true)
    List<VatRateResponse> all() {
        return repository.findAllOrdered().stream().map(VatRateResponse::of).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    VatRateResponse create(@Valid @RequestBody VatRateRequest request) {
        return VatRateResponse.of(service.create(request));
    }

    @PatchMapping("/{id}")
    VatRateResponse update(@PathVariable int id, @Valid @RequestBody VatRateRequest request) {
        return VatRateResponse.of(service.update(id, request));
    }

    @PutMapping("/{id}/default")
    VatRateResponse makeDefault(@PathVariable int id) {
        return VatRateResponse.of(service.makeDefault(id));
    }

    @PatchMapping("/{id}/active")
    VatRateResponse toggleActive(@PathVariable int id) {
        return VatRateResponse.of(service.toggleActive(id));
    }
}
