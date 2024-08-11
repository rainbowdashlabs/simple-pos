package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Product;

public record CartPositionDto(Product product, int amount) {
}
