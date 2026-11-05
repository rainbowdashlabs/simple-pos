package dev.chojo.simplepos.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money is kept as {@link BigDecimal} with two decimal places, rounded half up, matching the {@code numeric(12, 2)}
 * columns it is stored in. Unit prices keep four, because they can be a share of a total that does not divide into
 * cents. Amounts given as {@code double} literals are converted through their decimal string, so
 * {@code 0.1} becomes exactly {@code 0.10}.
 */
public final class Money {
    public static final int SCALE = 2;
    /**
     * Decimal places of a unit price, which can come from dividing a delivery's total by its amount.
     */
    public static final int UNIT_SCALE = 4;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE);

    private Money() {
    }

    public static BigDecimal of(double amount) {
        return of(BigDecimal.valueOf(amount));
    }

    /**
     * The amount with two decimal places, or null for null.
     */
    public static BigDecimal of(BigDecimal amount) {
        return amount == null ? null : amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * A unit price with four decimal places, or null for null.
     */
    public static BigDecimal unit(BigDecimal amount) {
        return amount == null ? null : amount.setScale(UNIT_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal unit(double amount) {
        return unit(BigDecimal.valueOf(amount));
    }

    /**
     * The share of {@code total} that falls on one of {@code count} units, as a unit price.
     */
    public static BigDecimal perUnit(BigDecimal total, int count) {
        return total.divide(BigDecimal.valueOf(count), UNIT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * The number of digits before the decimal point, used to refuse amounts a money column cannot hold.
     */
    public static int integerDigits(BigDecimal amount) {
        return Math.max(0, amount.precision() - amount.scale());
    }

    /**
     * A sum read from a query, as money; null, which a sum over no rows gives, counts as zero.
     */
    public static BigDecimal orZero(BigDecimal amount) {
        return amount == null ? ZERO : of(amount);
    }
}
