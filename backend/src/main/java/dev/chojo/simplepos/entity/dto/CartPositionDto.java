package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartPositionDto(@NotNull Integer productId, @Min(1) @Max(1_000) int amount) {
}
