package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.response.Listing;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/product")
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProductController(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Product> get(@PathVariable int id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Listing<Product>> get() {
        return ResponseEntity.ok(Listing.map(productRepository.findAll(), Product::getCategory));
    }

    @PostMapping("/")
    ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.accepted().body(productService.create(product));
    }

    @PutMapping("/")
    ResponseEntity<Product> update(@RequestBody Product product) {
        return ResponseEntity.accepted().body(productRepository.save(product));
    }
}
