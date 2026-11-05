package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Money;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountDto {
    private Integer id;
    private  String name;
    private  Instant created;
    private  BigDecimal balance;
    private Instant lastPurchase;

    public AccountDto(Integer id, String name, Instant created, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.balance = Money.of(balance);
    }

    /**
     * An account with the time of its latest purchase, or null if it never bought anything.
     */
    public AccountDto(Integer id, String name, Instant created, BigDecimal balance, Instant lastPurchase) {
        this(id, name, created, balance);
        this.lastPurchase = lastPurchase;
    }

    public AccountDto() {
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getLastPurchase() {
        return lastPurchase;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = Money.of(balance);
    }
}
