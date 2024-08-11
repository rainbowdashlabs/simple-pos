package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.Category;

import java.util.List;

public record CategoryGroup<T>(Category category, List<T> entries) {
}
