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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/sales/stat")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<ProductSalesStatDto>> get(@PathVariable int id,
                                                  @RequestParam(defaultValue = "100") int limit,
                                                  @RequestParam(defaultValue = "0") int page) {
        List<ProductSalesStatDto> list = purchaseRepository.getSalesStatByProduct(id, PageRequest.of(page, limit)).stream()
                .map(ProductSalesStatDto::new)
                .toList();
        return ResponseEntity.ok(list);
    }


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Listing<Product>> get() {
        return ResponseEntity.ok(Listing.map(productRepository.findAll(), Product::getCategory));
    }

    @PostMapping
    ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.accepted().body(productService.create(product));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Product> delete(@PathVariable int id) {
        boolean exists = productRepository.existsById(id);
        if (!exists) return ResponseEntity.notFound().build();
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    ResponseEntity<Product> update(@RequestBody Product product) {
        return ResponseEntity.accepted().body(productRepository.save(product));
    }
}
