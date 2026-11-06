package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.response.IngredientResponse;

public record StockProjectionDto(IngredientResponse ingredient, int currentStock, double dailyConsumption,
                                 Integer daysRemaining) {
}
