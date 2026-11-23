package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * One line of an invoice. What it needs depends on its kind:
 * <ul>
 *     <li>an {@link Kind#ITEM} names the ingredient, the quantity, whether it counts containers or pieces, and the
 *     price per unit; it may ask for its VAT rate to become the ingredient's rate from now on,</li>
 *     <li>a {@link Kind#PLEDGE} has a quantity and a price per unit, negative for empties given back,</li>
 *     <li>a {@link Kind#DISCOUNT} has an amount and the index of the item in {@code positions} it reduces, counted
 *     from 0; error messages count positions from 1, as an invoice does.</li>
 * </ul>
 * A VAT rate given on the line overrides the ingredient's rate, or for pledge the default rate.
 */
public record InvoicePosition(@NotNull Kind kind,
                              Integer ingredientId,
                              @Size(max = 200) String label,
                              @Min(1) @Max(1_000_000) Integer quantity,
                              Unit unit,
                              @DecimalMin("-99999999") @DecimalMax("99999999") BigDecimal price,
                              @DecimalMin("0") @DecimalMax("9999999999") BigDecimal amount,
                              @Min(0) Integer discountOf,
                              Integer vatRateId,
                              Boolean updateIngredientRate) {
    /**
     * Whether the line's VAT rate is to become its ingredient's rate; not asking for it means no.
     */
    public boolean updatesIngredientRate() {
        return Boolean.TRUE.equals(updateIngredientRate);
    }

    public enum Kind {
        ITEM, PLEDGE, DISCOUNT
    }

    public enum Unit {
        CONTAINER, PIECE
    }
}
