package dev.chojo.simplepos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
public class Storage {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
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

    /**
     * The id of the ingredient this batch belongs to, read without loading the ingredient.
     */
    public Integer getIngredientId() {
        return ingredient == null ? null : ingredient.getId();
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

    /**
     * Takes up to {@code wanted} units that are still in the batch.
     *
     * @return what the taken units cost: the share of the batch cost up to the units sold now, minus the share up to
     * the units sold before, so the costs of all units of a sold-out batch add up to exactly its cost
     */
    public StockTaken take(int wanted) {
        int units = Math.min(wanted, amount - sold);
        BigDecimal taken = costUpTo(sold + units).subtract(costUpTo(sold));
        sold += units;
        return new StockTaken(units, taken);
    }

    private BigDecimal costUpTo(int units) {
        if (amount <= 0) return Money.ZERO;
        return cost.multiply(BigDecimal.valueOf(units)).divide(BigDecimal.valueOf(amount), Money.SCALE, RoundingMode.HALF_UP);
    }

    /**
     * How many units {@link #take(int)} took and what they cost.
     */
    public record StockTaken(int units, BigDecimal cost) {
    }
}
