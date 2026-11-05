package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.exceptions.BadRequestException;
import dev.chojo.simplepos.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * The products with these ids, with their category and recipe, in one query.
     */
    @EntityGraph(attributePaths = {"category", "recipe", "recipe.entries", "recipe.entries.ingredient", "recipe.entries.ingredient.category"})
    List<Product> findAllByIdIn(Collection<Integer> ids);

    /**
     * A page of the ids of the products that are not deleted, optionally only those on sale and of one category (0 for
     * those without one), by category name (products without one last), then name. The products themselves are
     * loaded by id, because their recipes are collections a page cannot be cut from in one query.
     */
    @Query(value = "SELECT p.id FROM Product p LEFT JOIN p.category c WHERE p.deleted = false AND (:activeOnly = false OR p.active = true) AND (:category IS NULL OR c.id = :category OR (:category = 0 AND c IS NULL)) ORDER BY c.name ASC NULLS LAST, c.id, p.name, p.id",
            countQuery = "SELECT count(p) FROM Product p LEFT JOIN p.category c WHERE p.deleted = false AND (:activeOnly = false OR p.active = true) AND (:category IS NULL OR c.id = :category OR (:category = 0 AND c IS NULL))")
    Page<Integer> findIdPage(@Param("activeOnly") boolean activeOnly, @Param("category") Integer category, Pageable pageable);

    long countByCategory_IdAndDeletedFalse(int categoryId);

    /**
     * Detaches deleted products from a category, so the category can be deleted. Deleted products are never listed.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Product p SET p.category = null WHERE p.category.id = ?1 AND p.deleted = true")
    int detachDeletedFromCategory(int categoryId);
}
