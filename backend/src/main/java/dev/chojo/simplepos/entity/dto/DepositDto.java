package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Money paid into an account. Only positive amounts; an account's balance only goes down through purchases and
 * closing it.
 */
public record DepositDto(@NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount) {
}
