package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
}
