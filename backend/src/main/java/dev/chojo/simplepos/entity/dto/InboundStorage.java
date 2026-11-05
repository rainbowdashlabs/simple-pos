package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Ingredient;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A delivery of an ingredient. What was paid is given either per unit ({@code price}) or for the whole delivery
 * ({@code total}), never both.
 */
public record InboundStorage(Ingredient ingredient, Instant purchased, BigDecimal price, BigDecimal total,
                             BigDecimal pledge, Integer amount) {
    /**
     * The id of the delivered ingredient, or null if the request names none.
     */
    public Integer ingredientId() {
        return ingredient == null ? null : ingredient.getId();
    }
}
