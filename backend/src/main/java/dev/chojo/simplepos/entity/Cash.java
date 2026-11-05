package dev.chojo.simplepos.entity;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Cash {
    @Id
    @GeneratedValue
    private int id;
    @ManyToOne
    @JsonIncludeProperties({"id", "username"})
    private User user;

    private Instant created = Instant.now();
    @Column(precision = 12, scale = Money.SCALE, nullable = false)
    private BigDecimal amount;
    private CashType type;
    private String note;

    public Cash() {
    }

    public Cash(User user, double amount, CashType type, String note) {
        this(user, Money.of(amount), type, note);
    }

    public Cash(User user, BigDecimal amount, CashType type, String note) {
        this.user = user;
        this.amount = Money.of(amount);
        this.type = type;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = Money.of(amount);
    }

    public CashType getType() {
        return type;
    }

    public void setType(CashType type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
