package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StorageRepository extends JpaRepository<Storage, Integer> {
    @Query("SELECT ingredient, sum(amount - sold) FROM Storage WHERE amount > sold AND ingredient.id = ?1 GROUP BY ingredient")
    StorageSummary summaryById(int id);

    @Query("SELECT ingredient, sum(amount - sold) FROM Storage WHERE amount > sold GROUP BY ingredient")
    List<StorageSummary> summary();

    List<Storage> findAllByIngredient(Ingredient ingredient, Pageable pageable, Sort sort);

    @Query("SELECT Storage FROM Storage WHERE amount > sold AND ingredient = ?1 ORDER BY purchased ASC LIMIT 1")
    Optional<Storage> findNextIngredient(Ingredient ingredient);
}
