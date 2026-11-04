package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Ingredient;

import java.time.Instant;

public record InboundStorage(Ingredient ingredient, Instant purchased, Double price, Double pledge, Integer amount) {
    /**
     * The id of the delivered ingredient, or null if the request names none.
     */
    public Integer ingredientId() {
        return ingredient == null ? null : ingredient.getId();
    }
}
