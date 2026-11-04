package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.dto.ProductSalesStatDto;
import dev.chojo.simplepos.entity.response.Listing;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.service.ProductService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("api/product")
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final PurchaseRepository purchaseRepository;

    public ProductController(ProductRepository productRepository, ProductService productService, PurchaseRepository purchaseRepository) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.purchaseRepository = purchaseRepository;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Product> get(@PathVariable int id) {
        return ResponseEntity.ok(productRepository.require(id));
    }

    @GetMapping("/{id}/sales/stat")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<ProductSalesStatDto>> getSalesStat(@PathVariable int id,
                                                           @RequestParam(required = false) Instant after,
                                                           @RequestParam(defaultValue = "100") int limit,
                                                           @RequestParam(defaultValue = "0") int page) {
        Instant since = Objects.requireNonNullElse(after, Instant.EPOCH);
        List<ProductSalesStatDto> list = purchaseRepository.getSalesStatByProduct(id, since, PageRequest.of(page, limit)).stream()
                .map(ProductSalesStatDto::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/sales/stat/summary")
    ResponseEntity<ProductSalesStatDto> getSalesStatSummary(@PathVariable int id, @RequestParam Instant after) {
        ProductSalesStatDto dto = new ProductSalesStatDto(purchaseRepository.getSalesStatSummary(id, after), after);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Listing<Product>> get() {
        return ResponseEntity.ok(Listing.map(productRepository.findAllByDeletedFalse(), Product::getCategory));
    }

    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Listing<Product>> getActive() {
        return ResponseEntity.ok(Listing.map(productRepository.findAllByDeletedFalseAndActiveTrue(), Product::getCategory));
    }

    @PostMapping
    ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.accepted().body(productService.create(product));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable int id) {
        Product product = productRepository.requireNotDeleted(id);
        product.setDeleted(true);
        product.setActive(false);
        productRepository.save(product);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/active")
    ResponseEntity<Product> toggleActive(@PathVariable int id) {
        Product product = productRepository.requireNotDeleted(id);
        product.setActive(!product.isActive());
        return ResponseEntity.ok(productRepository.save(product));
    }

    @PutMapping
    ResponseEntity<Product> update(@RequestBody Product product) {
        return ResponseEntity.accepted().body(productService.update(product));
    }
}
