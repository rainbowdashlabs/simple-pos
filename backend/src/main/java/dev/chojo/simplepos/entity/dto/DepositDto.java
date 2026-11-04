package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Money paid into an account. Only positive amounts; an account's balance only goes down through purchases and
 * closing it.
 */
public record DepositDto(@NotNull @Positive Double amount) {
}
