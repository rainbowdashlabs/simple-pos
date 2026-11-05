package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Purchases to cancel together.
 */
public record PurchaseCancelRequest(@NotEmpty @Size(max = 100) List<@NotNull Integer> ids) {
}
