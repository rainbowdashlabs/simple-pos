package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.exceptions.BadRequestException;
import dev.chojo.simplepos.exceptions.NotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public interface IngredientRepository extends LookupRepository<Ingredient> {
    @Override
    default String entityName() {
        return "Ingredient";
    }

    /**
     * Finds an ingredient and locks its row until the surrounding transaction ends. Without a surrounding
     * transaction the lock only lasts for this single statement.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no ingredient with this id exists
     */
    default Ingredient requireLocked(Integer id) {
        return findByIdForUpdate(requireId(id)).orElseThrow(() -> new NotFoundException(entityName(), id));
    }

    /**
     * Locks the rows of all given ingredients in ascending id order. Every caller that locks more than one
     * ingredient goes through here, so two transactions never wait on each other's locks in opposite order.
     *
     * @throws BadRequestException if one of the ids is missing
     * @throws NotFoundException if one of the ingredients does not exist
     */
    default void requireLockedInOrder(Collection<Integer> ids) {
        ids.stream().map(this::requireId).distinct().sorted().forEach(this::requireLocked);
    }

    /**
     * A page of the ingredients, optionally of one category (0 for those without one), by category name (ingredients
     * without one last), then name.
     */
    @Query(value = "SELECT i FROM Ingredient i LEFT JOIN FETCH i.category c WHERE (:category IS NULL OR c.id = :category OR (:category = 0 AND c IS NULL)) ORDER BY c.name ASC NULLS LAST, c.id, i.name, i.id",
            countQuery = "SELECT count(i) FROM Ingredient i LEFT JOIN i.category c WHERE (:category IS NULL OR c.id = :category OR (:category = 0 AND c IS NULL))")
    Page<Ingredient> findPage(@Param("category") Integer category, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Ingredient i WHERE i.id = ?1")
    Optional<Ingredient> findByIdForUpdate(int id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Ingredient SET price = ?2 where id = ?1")
    int updateByIdIs(int id, BigDecimal price);

    long countByCategory_Id(int categoryId);
}
