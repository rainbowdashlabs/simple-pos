package dev.chojo.simplepos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

/**
 * Units of one ingredient that a purchase used: taken from one storage batch, or sold without stock when there was
 * none (no batch). Carries what those units cost, so profit does not depend on later changes to batches or prices.
 */
@Entity
public class PurchasePart {
    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Purchase purchase;
    @ManyToOne(fetch = FetchType.LAZY)
    private Storage storage;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Ingredient ingredient;
    @Column(nullable = false)
    private int amount;
    @Column(precision = 12, scale = Money.SCALE, nullable = false)
    private BigDecimal cost;

    public PurchasePart(Purchase purchase, Storage storage, Ingredient ingredient, int amount, BigDecimal cost) {
        this.purchase = purchase;
        this.storage = storage;
        this.ingredient = ingredient;
        this.amount = amount;
        this.cost = Money.of(cost);
    }

    public PurchasePart() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    /**
     * The batch the units came from, or null for units sold without stock.
     */
    public Storage getStorage() {
        return storage;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getAmount() {
        return amount;
    }

    public BigDecimal getCost() {
        return cost;
    }
}
