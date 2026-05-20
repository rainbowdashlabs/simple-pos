package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findAllByDeletedFalse();

    List<Product> findAllByDeletedFalseAndActiveTrue();
}
