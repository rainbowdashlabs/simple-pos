package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    /**
     * Finds a product, including deleted ones.
     *
     * @throws NotFoundException if no product with this id exists
     */
    default Product require(int id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Product", id));
    }

    /**
     * Finds a product that is not deleted.
     *
     * @throws NotFoundException if no product with this id exists or it is deleted
     */
    default Product requireNotDeleted(int id) {
        return findById(id).filter(product -> !product.isDeleted())
                .orElseThrow(() -> new NotFoundException("Product", id));
    }

    List<Product> findAllByDeletedFalse();

    List<Product> findAllByDeletedFalseAndActiveTrue();
}
