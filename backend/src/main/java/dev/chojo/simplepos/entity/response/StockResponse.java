package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.dto.StorageSummary;

public record StockResponse(IngredientResponse ingredient, int stock) {
    public static StockResponse of(StorageSummary summary) {
        return new StockResponse(IngredientResponse.of(summary.getIngredient()), summary.getStock());
    }
}
