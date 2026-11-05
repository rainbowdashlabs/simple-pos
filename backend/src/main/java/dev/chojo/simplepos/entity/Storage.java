package dev.chojo.simplepos.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Storage {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JsonIgnore
    private Ingredient ingredient;

    private Instant purchased;
    @Column(precision = 12, scale = Money.SCALE, nullable = false)
    private BigDecimal cost;
    private int amount;
    private int sold;

    public Storage() {
    }

    /**
     * A batch bought at {@code price} per unit; what was paid for it is that price times the amount, in cents.
     */
    public Storage(Integer id, Ingredient ingredient, Instant purchased, double price, int amount, int sold) {
        this(id, ingredient, purchased, BigDecimal.valueOf(price), amount, sold);
    }

    /**
     * A batch bought at {@code price} per unit; what was paid for it is that price times the amount, in cents.
     */
    public Storage(Integer id, Ingredient ingredient, Instant purchased, BigDecimal price, int amount, int sold) {
        this.id = id;
        this.ingredient = ingredient;
        this.purchased = purchased;
        this.cost = Money.of(price.multiply(BigDecimal.valueOf(amount)));
        this.amount = amount;
        this.sold = sold;
    }

    /**
     * A batch of {@code amount} units bought for {@code cost} in total.
     */
    public static Storage withCost(Ingredient ingredient, Instant purchased, BigDecimal cost, int amount) {
        Storage storage = new Storage(null, ingredient, purchased, BigDecimal.ZERO, amount, 0);
        storage.cost = Money.of(cost);
        return storage;
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

    /**
     * What was paid for the whole batch.
     */
    public BigDecimal getCost() {
        return cost;
    }

    /**
     * The cost of one unit of the batch: its cost divided by its amount.
     */
    public BigDecimal getPrice() {
        return amount > 0 ? Money.perUnit(cost, amount) : Money.unit(cost);
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

    public void take(){
        sold += 1;
    }
}
