package dev.chojo.simplepos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

@Entity
public class Cash {
    @Id
    @GeneratedValue
    private int id;
    @ManyToOne
    // TODO serialise a sparse representation of the user instead of the full user
    private User user;

    private Instant created = Instant.now();
    private double amount;
    private CashType type;
    private String note;

    public Cash() {
    }

    public Cash(User user, double amount, CashType type, String note) {
        this.user = user;
        this.amount = amount;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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
