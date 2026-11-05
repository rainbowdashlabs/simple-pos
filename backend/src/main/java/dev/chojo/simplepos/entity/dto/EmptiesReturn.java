package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Empty bottles and crates of one ingredient given back to the supplier.
 */
public record EmptiesReturn(@NotNull Integer ingredientId,
                            @NotNull @PositiveOrZero @Max(100_000) Integer bottles,
                            @NotNull @PositiveOrZero @Max(100_000) Integer crates) {
}
