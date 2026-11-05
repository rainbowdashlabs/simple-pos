package dev.chojo.simplepos.entity.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A purchase in an account's history; {@code soldBy} is the username of the cashier, or null for purchases booked
 * before the cashier was recorded.
 */
public record PurchaseDto(int id, int productId, Instant purchased, int amount, BigDecimal price, String soldBy) {
}
