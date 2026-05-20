package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Ingredient;

public record StockProjectionDto(Ingredient ingredient, int currentStock, double dailyConsumption,
                                 Integer daysRemaining) {
}
