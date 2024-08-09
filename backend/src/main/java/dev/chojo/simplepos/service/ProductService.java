package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product create(Product product) {
        return repository.save(product);
    }
}
