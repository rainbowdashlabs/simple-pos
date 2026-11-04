package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
    /**
     * Finds an ingredient.
     *
     * @throws NotFoundException if no ingredient with this id exists
     */
    default Ingredient require(int id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Ingredient", id));
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Ingredient SET price = ?2 where id = ?1")
    int updateByIdIs(int id, double price);

    @Query("SELECT Ingredient ")
    List<Ingredient> findBy(double price);
}
