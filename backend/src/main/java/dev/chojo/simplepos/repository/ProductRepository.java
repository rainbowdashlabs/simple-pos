package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    /**
     * Finds a product, including deleted ones.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no product with this id exists
     */
    default Product require(Integer id) {
        return findById(requireId(id)).orElseThrow(() -> new NotFoundException("Product", id));
    }

    /**
     * Finds a product that is not deleted.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no product with this id exists or it is deleted
     */
    default Product requireNotDeleted(Integer id) {
        return findById(requireId(id)).filter(product -> !product.isDeleted())
                .orElseThrow(() -> new NotFoundException("Product", id));
    }

    List<Product> findAllByDeletedFalse();

    List<Product> findAllByDeletedFalseAndActiveTrue();

    private Integer requireId(Integer id) {
        if (id == null) throw new BadRequestException("Product id is missing");
        return id;
    }
}
