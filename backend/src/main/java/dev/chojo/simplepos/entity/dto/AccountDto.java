package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Money;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountDto {
    private Integer id;
    private  String name;
    private  Instant created;
    private  BigDecimal balance;

    public AccountDto(Integer id, String name, Instant created, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.balance = Money.of(balance);
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = Money.of(balance);
    }
}
