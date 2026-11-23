package dev.chojo.simplepos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A VAT rate in percent, such as the standard and the reduced rate. Exactly one active rate is the default, which
 * applies wherever no other rate is chosen. Rates are never deleted, only deactivated, so everything pointing at one
 * keeps doing so.
 */
@Entity
public class VatRate {
    /**
     * Decimal places of a rate in percent.
     */
    public static final int SCALE = 2;

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(precision = 5, scale = SCALE, nullable = false)
    private BigDecimal rate;

    @Column(name = "is_default", nullable = false)
    private boolean defaultRate;

    @Column(nullable = false)
    private boolean active = true;

    public VatRate() {
    }

    public VatRate(Integer id, String name, BigDecimal rate, boolean defaultRate) {
        this.id = id;
        this.name = name;
        this.rate = scaled(rate);
        this.defaultRate = defaultRate;
    }

    public VatRate(Integer id, String name, double rate, boolean defaultRate) {
        this(id, name, BigDecimal.valueOf(rate), defaultRate);
    }

    private static BigDecimal scaled(BigDecimal rate) {
        return rate == null ? null : rate.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = scaled(rate);
    }

    public boolean isDefaultRate() {
        return defaultRate;
    }

    public void setDefaultRate(boolean defaultRate) {
        this.defaultRate = defaultRate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
