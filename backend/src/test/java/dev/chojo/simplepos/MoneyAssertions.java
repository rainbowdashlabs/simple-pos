package dev.chojo.simplepos;

import dev.chojo.simplepos.entity.Money;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Compares money by value, so {@code 2.5} and {@code 2.50} are the same amount, and nothing is rounded before
 * comparing.
 */
public final class MoneyAssertions {
    private MoneyAssertions() {
    }

    public static BigDecimal money(double amount) {
        return Money.of(amount);
    }

    public static void assertMoney(double expected, BigDecimal actual) {
        assertMoney(Money.of(expected), actual);
    }

    public static void assertMoney(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    /**
     * Compares a unit price by value with all four of its decimal places.
     */
    public static void assertUnitPrice(double expected, BigDecimal actual) {
        assertMoney(Money.unit(expected), actual);
    }
}
