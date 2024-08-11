package dev.chojo.simplepos.entity.dto;

import java.time.Instant;

public class AccountDto {
    private Integer id;
    private  String name;
    private  Instant created;
    private  double balance;

    public AccountDto(int id, String name, Instant created, double balance) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.balance = balance;
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

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
