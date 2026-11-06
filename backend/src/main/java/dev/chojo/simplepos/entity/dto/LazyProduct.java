package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.response.CategoryResponse;

import java.math.BigDecimal;

/**
 * A product without its recipe, as the purchase history lists it.
 */
public record LazyProduct(Integer id, CategoryResponse category, String name, BigDecimal price, boolean active) {
    public static LazyProduct of(Product product) {
        return new LazyProduct(product.getId(), CategoryResponse.of(product.getCategory()), product.getName(),
                product.getPrice(), product.isActive());
    }
}
