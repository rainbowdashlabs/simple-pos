package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Storage;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StorageRepository extends JpaRepository<Storage, Integer> {
    boolean existsByIngredient_Id(int ingredientId);

    List<Storage> findAllBy(Pageable pageable);

    @Query("SELECT i as ingredient, cast(coalesce(s.stock, 0) as integer) AS stock FROM Ingredient i LEFT JOIN FETCH i.category LEFT JOIN (SELECT ingredient.id as id, sum(amount - sold) as stock FROM Storage WHERE amount > sold AND ingredient.id = ?1 GROUP BY ingredient) s ON i.id = s.id WHERE i.id = ?1")
    Tuple summaryById(int id);

    @Query("SELECT i as ingredient, cast(coalesce(s.stock, 0) as integer) as stock FROM Ingredient i LEFT JOIN FETCH i.category LEFT JOIN (SELECT ingredient.id as id, sum(amount - sold) as stock FROM Storage WHERE amount > sold GROUP BY ingredient) s ON i.id = s.id WHERE i.active = true")
    List<Tuple> summary();

    @Query("SELECT i as ingredient, cast(coalesce(s.stock, 0) as integer) as stock FROM Ingredient i LEFT JOIN FETCH i.category LEFT JOIN (SELECT ingredient.id as id, sum(amount - sold) as stock FROM Storage WHERE amount > sold GROUP BY ingredient) s ON i.id = s.id WHERE i.active = true AND i.minStock > stock AND coalesce(s.stock, 0) > 0 ORDER BY cast(coalesce(s.stock, 0) as double) / i.minStock ASC, i.name ASC")
    List<Tuple> lowStock(PageRequest pageRequest);

    @Query("SELECT i as ingredient, cast(coalesce(s.stock, 0) as integer) as stock FROM Ingredient i LEFT JOIN FETCH i.category LEFT JOIN (SELECT ingredient.id as id, sum(amount - sold) as stock FROM Storage WHERE amount > sold GROUP BY ingredient) s ON i.id = s.id WHERE i.active = true AND coalesce(s.stock, 0) = 0 ORDER BY i.name ASC")
    List<Tuple> outOfStock(PageRequest pageRequest);

    List<Storage> findAllByIngredient(Ingredient ingredient, Pageable pageable);

    /**
     * The oldest batch of the ingredient that still holds units: by delivery time, then by id.
     */
    @Query(value = "SELECT id FROM storage WHERE amount > sold AND ingredient_id = ?1 ORDER BY purchased, id LIMIT 1", nativeQuery = true)
    Optional<Integer> findNextIngredient(int ingredient);

    /**
     * Per ingredient used for a sale between {@code after} and {@code until}, how many units sales used in that time,
     * from storage or sold without stock, and when a unit of it was first used for a sale at all.
     */
    @Query("""
            SELECT pp.ingredient.id AS ingredient_id,
                   sum(pp.amount) FILTER (WHERE pp.purchase.purchased > ?1 AND pp.purchase.purchased <= ?2) AS taken,
                   min(pp.purchase.purchased) AS first_taken
            FROM PurchasePart pp
            GROUP BY pp.ingredient.id
            HAVING sum(pp.amount) FILTER (WHERE pp.purchase.purchased > ?1 AND pp.purchase.purchased <= ?2) > 0
            """)
    List<Tuple> usageByIngredient(Instant after, Instant until);
}
