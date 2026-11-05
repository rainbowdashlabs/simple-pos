package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Ingredient;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A delivery of an ingredient. What was paid is given either per unit ({@code price}) or for the whole delivery
 * ({@code total}), never both. Amounts may carry more decimals than money has, as a form computing them in floating
 * point sends; they are rounded when booked.
 */
public record InboundStorage(@NotNull Ingredient ingredient,
                             Instant purchased,
                             @PositiveOrZero @DecimalMax("99999999") BigDecimal price,
                             @PositiveOrZero @DecimalMax("9999999999") BigDecimal total,
                             @NotNull @PositiveOrZero @DecimalMax("9999999999") BigDecimal pledge,
                             @NotNull @Min(1) @Max(1_000_000) Integer amount) {
    /**
     * Exactly one of the price per unit and the total is given.
     */
    @AssertTrue(message = "{simplepos.validation.priced_once}")
    public boolean isPricedOnce() {
        return (price == null) != (total == null);
    }

    /**
     * The id of the delivered ingredient, or null if the request names none.
     */
    public Integer ingredientId() {
        return ingredient == null ? null : ingredient.getId();
    }
}
