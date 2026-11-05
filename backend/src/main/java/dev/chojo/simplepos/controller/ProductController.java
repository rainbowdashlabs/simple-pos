package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.BusinessTimeZone;
import dev.chojo.simplepos.controller.validation.PageNumber;
import dev.chojo.simplepos.controller.validation.PageSize;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.dto.DayStat;
import dev.chojo.simplepos.entity.dto.ProductRequest;
import dev.chojo.simplepos.entity.response.PageResponse;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Objects;

@RestController
@RequestMapping("api/product")
public class ProductController {
    private final BusinessTimeZone businessTimeZone;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final PurchaseRepository purchaseRepository;

    public ProductController(ProductRepository productRepository, ProductService productService, PurchaseRepository purchaseRepository, BusinessTimeZone businessTimeZone) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.purchaseRepository = purchaseRepository;
        this.businessTimeZone = businessTimeZone;
    }

    @GetMapping("/{id}")
    ResponseEntity<Product> get(@PathVariable int id) {
        return ResponseEntity.ok(productRepository.require(id));
    }

    @GetMapping("/{id}/sales/stat")
    PageResponse<DayStat> getSalesStat(@PathVariable int id,
                                       @RequestParam(required = false) Instant after,
                                       @RequestParam(defaultValue = "100") @PageSize int limit,
                                       @RequestParam(defaultValue = "0") @PageNumber int page) {
        Instant since = Objects.requireNonNullElse(after, Instant.EPOCH);
        return PageResponse.of(purchaseRepository.getSalesStatByProduct(id, since, businessTimeZone.id(),
                PageRequest.of(page, limit)), DayStat::of);
    }

    @GetMapping("/{id}/sales/stat/summary")
    ResponseEntity<DayStat> getSalesStatSummary(@PathVariable int id, @RequestParam Instant after) {
        return ResponseEntity.ok(DayStat.of(purchaseRepository.getSalesStatSummary(id, after)));
    }

    @GetMapping
    PageResponse<Product> get(@RequestParam(required = false) Integer categoryId,
                              @RequestParam(defaultValue = "100") @PageSize int limit,
                              @RequestParam(defaultValue = "0") @PageNumber int page) {
        return productService.page(false, categoryId, PageRequest.of(page, limit));
    }

    @GetMapping("/active")
    PageResponse<Product> getActive(@RequestParam(required = false) Integer categoryId,
                                    @RequestParam(defaultValue = "100") @PageSize int limit,
                                    @RequestParam(defaultValue = "0") @PageNumber int page) {
        return productService.page(true, categoryId, PageRequest.of(page, limit));
    }

    @PostMapping
    ResponseEntity<Product> create(@Valid @RequestBody ProductRequest product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(product));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable int id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    ResponseEntity<Product> toggleActive(@PathVariable int id) {
        return ResponseEntity.ok(productService.toggleActive(id));
    }

    @PutMapping("/{id}")
    ResponseEntity<Product> update(@PathVariable int id, @Valid @RequestBody ProductRequest product) {
        return ResponseEntity.ok(productService.update(id, product));
    }
}
