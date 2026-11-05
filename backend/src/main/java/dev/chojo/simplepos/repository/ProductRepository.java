package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.exceptions.BadRequestException;
import dev.chojo.simplepos.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends LookupRepository<Product> {
    @Override
    default String entityName() {
        return "Product";
    }

    /**
     * Finds a product that is not deleted.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no product with this id exists or it is deleted
     */
    default Product requireNotDeleted(Integer id) {
        return findById(requireId(id)).filter(product -> !product.isDeleted())
                .orElseThrow(() -> new NotFoundException(entityName(), id));
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

    long countByCategory_IdAndDeletedFalse(int categoryId);

    /**
     * Detaches deleted products from a category, so the category can be deleted. Deleted products are never listed.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Product p SET p.category = null WHERE p.category.id = ?1 AND p.deleted = true")
    int detachDeletedFromCategory(int categoryId);
}
