package dev.chojo.simplepos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Balance {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Account account;

    private Instant added;

    @Column(precision = 12, scale = Money.SCALE, nullable = false)
    private BigDecimal amount;

    public Balance() {
    }

    public Balance(Integer id, Account account, Instant added, double amount) {
        this(id, account, added, Money.of(amount));
    }

    public Balance(Integer id, Account account, Instant added, BigDecimal amount) {
        this.id = id;
        this.account = account;
        this.added = added;
        this.amount = Money.of(amount);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Instant getAdded() {
        return added;
    }

    public void setAdded(Instant added) {
        this.added = added;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = Money.of(amount);
    }
}
