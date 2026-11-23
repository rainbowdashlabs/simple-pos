package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.VatRate;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * What a client may set on an ingredient. Its id comes from the path, and whether it is active only changes through
 * the toggle endpoint. Without a VAT rate the ingredient uses the default rate.
 */
public record IngredientRequest(@NotBlank @Size(max = 100) String name,
                                @NotNull Integer categoryId,
                                @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 4) BigDecimal price,
                                @NotNull @PositiveOrZero Integer containerSize,
                                @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal pledge,
                                @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal pledgeContainer,
                                @NotNull @PositiveOrZero Integer minStock,
                                Integer vatRateId) {

    /**
     * A new, active ingredient with these values.
     */
    public Ingredient toIngredient(Category category, VatRate vatRate) {
        Ingredient ingredient = new Ingredient(null, category, name, price, containerSize, pledge, pledgeContainer, minStock);
        ingredient.setVatRate(vatRate);
        return ingredient;
    }

    /**
     * Copies these values onto an existing ingredient.
     */
    public void applyTo(Ingredient ingredient, Category category, VatRate vatRate) {
        ingredient.setName(name);
        ingredient.setCategory(category);
        ingredient.setPrice(price);
        ingredient.setContainerSize(containerSize);
        ingredient.setPledge(pledge);
        ingredient.setPledgeContainer(pledgeContainer);
        ingredient.setMinStock(minStock);
        ingredient.setVatRate(vatRate);
    }
}
