package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.RecipeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeEntryRepository extends JpaRepository<RecipeEntry, Integer> {
    /**
     * Whether a recipe uses the ingredient. Entries no longer attached to a recipe do not count.
     */
    boolean existsByIngredient_IdAndRecipeIsNotNull(int ingredientId);

}
