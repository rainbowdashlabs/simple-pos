package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * What an administrator may set on a VAT rate: its name and the rate in percent. Which rate is the default and
 * whether a rate is active change through their own endpoints.
 */
public record VatRateRequest(@NotBlank @Size(max = 100) String name,
                             @NotNull @PositiveOrZero @DecimalMax(value = "100", inclusive = false)
                             @Digits(integer = 2, fraction = 2) BigDecimal rate) {
}
