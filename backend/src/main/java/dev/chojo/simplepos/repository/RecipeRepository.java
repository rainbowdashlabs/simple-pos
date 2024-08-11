package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
}
