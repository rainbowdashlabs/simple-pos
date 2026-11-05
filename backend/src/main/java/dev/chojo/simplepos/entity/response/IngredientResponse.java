package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.Ingredient;

import java.math.BigDecimal;

public record IngredientResponse(Integer id, CategoryResponse category, String name, BigDecimal price,
                                 int containerSize, BigDecimal pledge, BigDecimal pledgeContainer, int minStock,
                                 boolean active) {
    public static IngredientResponse of(Ingredient ingredient) {
        return new IngredientResponse(ingredient.getId(), CategoryResponse.of(ingredient.getCategory()),
                ingredient.getName(), ingredient.getPrice(), ingredient.getContainerSize(), ingredient.getPledge(),
                ingredient.getPledgeContainer(), ingredient.getMinStock(), ingredient.isActive());
    }
}
