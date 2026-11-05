package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.Category;

public record CategoryResponse(Integer id, String name) {
    /**
     * The category as the API shows it, or null for none.
     */
    public static CategoryResponse of(Category category) {
        return category == null ? null : new CategoryResponse(category.getId(), category.getName());
    }
}
