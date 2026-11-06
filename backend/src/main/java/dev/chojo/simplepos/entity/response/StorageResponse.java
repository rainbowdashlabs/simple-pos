package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.Storage;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One delivered batch of an ingredient, with what it cost in total and per unit, and how much of it is sold.
 */
public record StorageResponse(Integer id, Integer ingredientId, Instant purchased, BigDecimal cost, BigDecimal price,
                              int amount, int sold) {
    public static StorageResponse of(Storage storage) {
        return new StorageResponse(storage.getId(), storage.getIngredientId(), storage.getPurchased(),
                storage.getCost(), storage.getPrice(), storage.getAmount(), storage.getSold());
    }
}
