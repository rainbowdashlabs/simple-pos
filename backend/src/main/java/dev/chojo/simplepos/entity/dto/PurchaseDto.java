package dev.chojo.simplepos.entity.dto;

import java.time.Instant;

public record PurchaseDto(int id, int productId, Instant purchased, int amount, double price) {
}
