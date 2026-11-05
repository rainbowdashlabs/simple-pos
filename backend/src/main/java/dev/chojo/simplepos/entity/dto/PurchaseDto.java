package dev.chojo.simplepos.entity.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PurchaseDto(int id, int productId, Instant purchased, int amount, BigDecimal price) {
}
