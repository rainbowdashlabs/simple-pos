package dev.chojo.simplepos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

/**
 * How a supplier names something on an invoice, remembered from the last booking: the printed text or GTIN, and for
 * an item which ingredient it is, whether it counts containers or pieces, and the price last paid. A GTIN identifies
 * the alias on its own; without one the text does, until a line with the same text brings a GTIN. The price is kept
 * as printed, so whether it was net is kept with it. Texts are kept {@link #normalize normalized}, so spacing and case
 * do not tell two aliases apart.
 */
@Entity
public class InvoiceAlias {
    public enum Kind {
        ITEM, PLEDGE
    }

    @Id
    @GeneratedValue
    private Integer id;

    @Column(length = 200)
    private String text;

    @Column(length = 20)
    private String gtin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Kind kind;

    @ManyToOne
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private CountUnit unit;

    @Column(precision = 12, scale = Money.UNIT_SCALE)
    private BigDecimal lastPrice;

    @Column(nullable = false)
    private boolean lastPriceNet;

    @Column(nullable = false)
    private Instant lastUsed;

    public InvoiceAlias() {
    }

    public InvoiceAlias(String text, String gtin) {
        this.text = normalize(text);
        this.gtin = gtin == null || gtin.isBlank() ? null : gtin.strip();
    }

    /**
     * The text as aliases keep and search it: without surrounding spaces, inner runs of spaces as one, in upper case;
     * null for none or only spaces.
     */
    public static String normalize(String text) {
        if (text == null) return null;
        String normalized = text.strip().replaceAll("(?U)\\s+", " ").toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Remembers what an invoice line said this time.
     */
    public void use(Kind kind, Ingredient ingredient, CountUnit unit, BigDecimal price, boolean net, Instant when) {
        this.kind = kind;
        this.ingredient = ingredient;
        this.unit = unit;
        this.lastPrice = Money.unit(price);
        this.lastPriceNet = net;
        this.lastUsed = when;
    }

    /**
     * Gives an alias known only by its text the GTIN a line brought, so the article stays one alias.
     */
    public void assignGtin(String gtin) {
        if (this.gtin == null) this.gtin = gtin;
    }

    /**
     * Takes the text a line printed this time, when the alias is found by its GTIN.
     */
    public void retext(String text) {
        String normalized = normalize(text);
        if (normalized != null) this.text = normalized;
    }

    public Integer getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getGtin() {
        return gtin;
    }

    public Kind getKind() {
        return kind;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public CountUnit getUnit() {
        return unit;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public boolean isLastPriceNet() {
        return lastPriceNet;
    }

    public Instant getLastUsed() {
        return lastUsed;
    }
}
