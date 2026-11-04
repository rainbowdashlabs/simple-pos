package dev.chojo.simplepos.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * What a client may set on a product. Its id comes from the path; whether it is deleted or active is never taken
 * from a request. A new product is active, and the state only changes through the toggle endpoint.
 */
public record ProductRequest(@NotBlank @Size(max = 100) String name,
                             @NotNull Integer categoryId,
                             @NotNull @PositiveOrZero Double price,
                             @NotNull List<@NotNull @Valid RecipeEntryRequest> recipe) {
}
