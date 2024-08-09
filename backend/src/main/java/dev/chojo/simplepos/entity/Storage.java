package dev.chojo.simplepos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

@Entity
public class Storage {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Ingredient ingredient;

    private Instant purchased;
    private double price;
    private int amount;
    private int sold;

    public Storage() {
    }

    public Storage(Integer id, Ingredient ingredient, Instant purchased, double price, int amount, int sold) {
        this.id = id;
        this.ingredient = ingredient;
        this.purchased = purchased;
        this.price = price;
        this.amount = amount;
        this.sold = sold;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Instant getPurchased() {
        return purchased;
    }

    public void setPurchased(Instant purchased) {
        this.purchased = purchased;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }
}
