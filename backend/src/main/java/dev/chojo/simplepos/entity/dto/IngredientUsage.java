package dev.chojo.simplepos.entity.dto;

import java.time.Instant;

/**
 * How many units of an ingredient sales used in a period, and when a sale first used it at all.
 */
public record IngredientUsage(Integer ingredientId, Long taken, Instant firstTaken) {
}
