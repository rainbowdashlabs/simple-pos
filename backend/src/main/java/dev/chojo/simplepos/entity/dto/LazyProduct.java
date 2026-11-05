package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Category;

import java.math.BigDecimal;

public record LazyProduct(Integer id, Category category, String name, BigDecimal price, boolean active) {
}
