package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * A till count as the pieces counted per denomination, keyed by the denomination's value in cents.
 */
public record TillCountRequest(@NotEmpty @Size(max = 50) Map<@NotNull @Positive @Max(100_000) Integer,
        @NotNull @PositiveOrZero @Max(100_000) Integer> denominations) {
}
