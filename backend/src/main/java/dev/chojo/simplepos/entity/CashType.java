package dev.chojo.simplepos.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Locale;

/**
 * Why money went into or out of the register. Stored and sent as its lower-case name.
 */
public enum CashType {
    DEPOSIT, PURCHASE, PLEDGE, BALANCE, OTHER, CORRECTION, SALE;

    @JsonValue
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * The type with this lower-case name.
     *
     * @throws IllegalArgumentException if there is none, which a request body reports as 400
     */
    @JsonCreator
    public static CashType of(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown cash type " + value));
    }

    /**
     * Stores the type as its lower-case name, which is what the column held as free text before. A stored value that
     * is not a known type reads as {@link #OTHER}, so an old free-text entry cannot break the cash listing.
     */
    @Converter(autoApply = true)
    public static class StoredAs implements AttributeConverter<CashType, String> {
        @Override
        public String convertToDatabaseColumn(CashType type) {
            return type == null ? null : type.value();
        }

        @Override
        public CashType convertToEntityAttribute(String value) {
            if (value == null) return null;
            return Arrays.stream(values()).filter(type -> type.value().equals(value)).findFirst().orElse(OTHER);
        }
    }
}
