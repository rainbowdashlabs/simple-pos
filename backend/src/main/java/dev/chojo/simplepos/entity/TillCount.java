package dev.chojo.simplepos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * A count of the cash in the till, with the total the till should have held at that moment and the pieces counted
 * per denomination, keyed by the denomination's value in cents.
 */
@Entity
public class TillCount {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(precision = 12, scale = Money.SCALE, nullable = false)
    private BigDecimal counted;

    @Column(precision = 12, scale = Money.SCALE, nullable = false)
    private BigDecimal expected;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<Integer, Integer> denominations;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User countedBy;

    @Column(nullable = false)
    private Instant created = Instant.now();

    public TillCount() {
    }

    public TillCount(BigDecimal counted, BigDecimal expected, Map<Integer, Integer> denominations, User countedBy) {
        this.counted = Money.of(counted);
        this.expected = Money.of(expected);
        this.denominations = denominations;
        this.countedBy = countedBy;
    }

    public Integer getId() {
        return id;
    }

    public BigDecimal getCounted() {
        return counted;
    }

    public BigDecimal getExpected() {
        return expected;
    }

    public BigDecimal getDifference() {
        return counted.subtract(expected);
    }

    public Map<Integer, Integer> getDenominations() {
        return denominations;
    }

    public User getCountedBy() {
        return countedBy;
    }

    public Instant getCreated() {
        return created;
    }
}
