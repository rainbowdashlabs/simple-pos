package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
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

    @EntityGraph(attributePaths = {"category", "recipe", "recipe.entries", "recipe.entries.ingredient", "recipe.entries.ingredient.category"})
    List<Product> findAllByDeletedFalse();

    @EntityGraph(attributePaths = {"category", "recipe", "recipe.entries", "recipe.entries.ingredient", "recipe.entries.ingredient.category"})
    List<Product> findAllByDeletedFalseAndActiveTrue();

    /**
     * The products with these ids, with their category and recipe, in one query.
     */
    @EntityGraph(attributePaths = {"category", "recipe", "recipe.entries", "recipe.entries.ingredient", "recipe.entries.ingredient.category"})
    List<Product> findAllByIdIn(Collection<Integer> ids);

    private Integer requireId(Integer id) {
        if (id == null) throw new BadRequestException("Product id is missing");
        return id;
    }

    long countByCategory_IdAndDeletedFalse(int categoryId);

    /**
     * Detaches deleted products from a category, so the category can be deleted. Deleted products are never listed.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Product p SET p.category = null WHERE p.category.id = ?1 AND p.deleted = true")
    int detachDeletedFromCategory(int categoryId);
}
