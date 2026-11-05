package dev.chojo.simplepos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Synchronize;

/**
 * The units of an ingredient still in storage. Read from the {@code ingredient_stock} view, the one place this rule is
 * written down. Pending changes to the tables it reads are written before it is queried.
 */
@Entity
@Immutable
@Synchronize({"ingredient", "storage"})
@Table(name = "ingredient_stock")
public class IngredientStock {
    @Id
    @Column(name = "ingredient_id")
    private Integer ingredientId;

    private int stock;

    public Integer getIngredientId() {
        return ingredientId;
    }

    public int getStock() {
        return stock;
    }
}
