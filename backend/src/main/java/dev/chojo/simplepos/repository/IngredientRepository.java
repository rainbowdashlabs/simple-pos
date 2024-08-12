package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
    @Modifying
    @Query("UPDATE Ingredient SET price = ?2 where id = ?1")
    int updateByIdIs(int id, double price);

    @Query("SELECT Ingredient ")
    List<Ingredient> findBy(double price);

}
