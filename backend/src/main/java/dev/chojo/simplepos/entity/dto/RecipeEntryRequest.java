package dev.chojo.simplepos.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * One entry of a recipe: an existing ingredient by id, or a new ingredient to create with the product, and how many
 * units of it go into one product.
 */
public record RecipeEntryRequest(Integer ingredientId,
                                 @Valid IngredientRequest ingredient,
                                 @NotNull @Positive Integer amount) {
}
