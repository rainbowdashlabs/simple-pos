package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Ingredient;

import java.time.Instant;

public record InboundStorage(Ingredient ingredient, Instant purchased, Double price, Double pledge, Integer amount) {
}
