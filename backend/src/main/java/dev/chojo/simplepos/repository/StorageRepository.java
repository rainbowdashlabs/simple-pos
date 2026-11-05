package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.dto.IngredientUsage;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface StorageRepository extends JpaRepository<Storage, Integer> {
    boolean existsByIngredient_Id(int ingredientId);

    List<Storage> findAllBy(Pageable pageable);

    /**
     * The stock of one ingredient, or null if there is no ingredient with this id.
     */
    @Query("SELECT NEW dev.chojo.simplepos.entity.dto.StorageSummary(i, s.stock) FROM Ingredient i LEFT JOIN FETCH i.category JOIN IngredientStock s ON s.ingredientId = i.id WHERE i.id = ?1")
    StorageSummary summaryById(int id);

    @Query("SELECT NEW dev.chojo.simplepos.entity.dto.StorageSummary(i, s.stock) FROM Ingredient i LEFT JOIN FETCH i.category JOIN IngredientStock s ON s.ingredientId = i.id WHERE i.active = true")
    List<StorageSummary> summary();

    @Query("SELECT NEW dev.chojo.simplepos.entity.dto.StorageSummary(i, s.stock) FROM Ingredient i LEFT JOIN FETCH i.category JOIN IngredientStock s ON s.ingredientId = i.id WHERE i.active = true AND i.minStock > s.stock AND s.stock > 0 ORDER BY cast(s.stock as double) / i.minStock ASC, i.name ASC")
    List<StorageSummary> lowStock(PageRequest pageRequest);

    @Query("SELECT NEW dev.chojo.simplepos.entity.dto.StorageSummary(i, s.stock) FROM Ingredient i LEFT JOIN FETCH i.category JOIN IngredientStock s ON s.ingredientId = i.id WHERE i.active = true AND s.stock = 0 ORDER BY i.name ASC")
    List<StorageSummary> outOfStock(PageRequest pageRequest);

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
            SELECT NEW dev.chojo.simplepos.entity.dto.IngredientUsage(pp.ingredient.id,
                   sum(pp.amount) FILTER (WHERE pp.purchase.purchased > ?1 AND pp.purchase.purchased <= ?2),
                   min(pp.purchase.purchased))
            FROM PurchasePart pp
            GROUP BY pp.ingredient.id
            HAVING sum(pp.amount) FILTER (WHERE pp.purchase.purchased > ?1 AND pp.purchase.purchased <= ?2) > 0
            """)
    List<IngredientUsage> usageByIngredient(Instant after, Instant until);

    /**
     * The stock batches bought in {@code [from, to)}, oldest first, with their ingredients, read as a stream.
     */
    @QueryHints({@QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "500"), @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")})
    @Query("SELECT s FROM Storage s JOIN FETCH s.ingredient WHERE s.purchased >= ?1 AND s.purchased < ?2 ORDER BY s.purchased, s.id")
    Stream<Storage> streamBetween(Instant from, Instant to);
}
