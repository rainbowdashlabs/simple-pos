package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * The counted stock of one ingredient.
 */
public record InventoryCorrection(@NotNull Integer ingredient, @NotNull @PositiveOrZero Integer amount) {
}
