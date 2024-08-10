package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Category;

public record LazyProduct(Integer id, Category category, String name, double price, boolean active) {
}
