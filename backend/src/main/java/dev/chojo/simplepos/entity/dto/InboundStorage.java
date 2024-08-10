package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Ingredient;

import java.time.Instant;

public record InboundStorage(Ingredient ingredient, Instant purchased, double price, double pledge, int amount) {
}
