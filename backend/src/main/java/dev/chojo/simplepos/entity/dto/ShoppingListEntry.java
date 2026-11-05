package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Ingredient;

import java.math.BigDecimal;

/**
 * What to buy of one ingredient: {@code amount} units, which is {@code containers} full containers when the
 * ingredient comes in containers (0 otherwise), at an estimated cost by its current price.
 */
public record ShoppingListEntry(Ingredient ingredient, int stock, double dailyUsage, int amount, int containers,
                                BigDecimal estimatedCost) {
}
