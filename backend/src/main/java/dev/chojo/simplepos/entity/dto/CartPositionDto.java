package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Product;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartPositionDto(@NotNull Product product, @Min(1) @Max(1_000) int amount) {
    /**
     * The id of the product to buy, or null if the request names none.
     */
    public Integer productId() {
        return product == null ? null : product.getId();
    }
}
