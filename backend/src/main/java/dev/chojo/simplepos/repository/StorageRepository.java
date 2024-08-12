package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Storage;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StorageRepository extends JpaRepository<Storage, Integer> {
    @Query("SELECT i as ingredient, cast(coalesce(s.stock, 0) as integer) AS stock FROM Ingredient i LEFT JOIN (SELECT ingredient.id as id, sum(amount - sold) as stock FROM Storage WHERE amount > sold AND ingredient.id = ?1 GROUP BY ingredient) s ON i.id = s.id WHERE i.id = ?1")
    Tuple summaryById(int id);

    @Query("SELECT i as ingredient, cast(coalesce(s.stock, 0) as integer) as stock FROM Ingredient i LEFT JOIN (SELECT ingredient.id as id, sum(amount - sold) as stock FROM Storage WHERE amount > sold GROUP BY ingredient) s ON i.id = s.id")
    List<Tuple> summary();

    @Query("SELECT i as ingredient, cast(coalesce(s.stock, 0) as integer) as stock FROM Ingredient i LEFT JOIN (SELECT ingredient.id as id, sum(amount - sold) as stock FROM Storage WHERE amount > sold GROUP BY ingredient) s ON i.id = s.id WHERE i.minStock > stock ORDER BY stock / i.minStock ASC")
    List<Tuple> lowStock(PageRequest pageRequest);

    List<Storage> findAllByIngredient(Ingredient ingredient, Pageable pageable);

    @Query(value = "SELECT id FROM storage WHERE amount > sold AND ingredient_id = ?1 ORDER BY purchased LIMIT 1", nativeQuery = true)
    Optional<Integer> findNextIngredient(int ingredient);
}
