package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Ingredient;
import jakarta.persistence.Tuple;

public class StorageSummary implements Comparable<StorageSummary> {
    private Ingredient ingredient;
    private int stock;

    public StorageSummary(Ingredient ingredient, int stock) {
        this.ingredient = ingredient;
        this.stock = stock;
    }

    public StorageSummary(Tuple tuple){
        this.ingredient = (Ingredient) tuple.get("ingredient");
        this.stock = (int) tuple.get("stock");
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }


    @Override
    public int compareTo(StorageSummary o) {
        return ingredient.compareTo(o.ingredient);
    }
}
