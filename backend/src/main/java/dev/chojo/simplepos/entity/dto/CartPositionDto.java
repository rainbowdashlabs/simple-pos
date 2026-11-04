package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Product;

public record CartPositionDto(Product product, int amount) {
    /**
     * The id of the product to buy, or null if the request names none.
     */
    public Integer productId() {
        return product == null ? null : product.getId();
    }
}
