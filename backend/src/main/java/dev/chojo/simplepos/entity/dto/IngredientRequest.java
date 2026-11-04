package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * What a client may set on an ingredient. Its id comes from the path, and whether it is active only changes through
 * the toggle endpoint.
 */
public record IngredientRequest(@NotBlank @Size(max = 100) String name,
                                @NotNull Integer categoryId,
                                @NotNull @PositiveOrZero Double price,
                                @NotNull @PositiveOrZero Integer containerSize,
                                @NotNull @PositiveOrZero Double pledge,
                                @NotNull @PositiveOrZero Double pledgeContainer,
                                @NotNull @PositiveOrZero Integer minStock) {

    /**
     * A new, active ingredient with these values.
     */
    public Ingredient toIngredient(Category category) {
        return new Ingredient(null, category, name, price, containerSize, pledge, pledgeContainer, minStock);
    }

    /**
     * Copies these values onto an existing ingredient.
     */
    public void applyTo(Ingredient ingredient, Category category) {
        ingredient.setName(name);
        ingredient.setCategory(category);
        ingredient.setPrice(price);
        ingredient.setContainerSize(containerSize);
        ingredient.setPledge(pledge);
        ingredient.setPledgeContainer(pledgeContainer);
        ingredient.setMinStock(minStock);
    }
}
