package dev.chojo.simplepos.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
public class Ingredient implements Comparable<Ingredient> {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Category category;

    @NotNull
    private String name;
    @NotNull
    @Column(precision = 12, scale = Money.UNIT_SCALE, nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private int containerSize;
    @Column(precision = 12, scale = Money.SCALE, nullable = false)
    private BigDecimal pledge;
    @Column(precision = 12, scale = Money.SCALE, nullable = false)
    private BigDecimal pledgeContainer;
    @Column(nullable = false)
    private int minStock;

    @Column(columnDefinition = "boolean default true")
    private boolean active = true;

    public Ingredient(Integer id, Category category, String name, double price, int containerSize, double pledge, double pledgeContainer, int minStock) {
        this(id, category, name, BigDecimal.valueOf(price), containerSize, Money.of(pledge), Money.of(pledgeContainer), minStock);
    }

    public Ingredient(Integer id, Category category, String name, BigDecimal price, int containerSize, BigDecimal pledge, BigDecimal pledgeContainer, int minStock) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = Money.unit(price);
        this.containerSize = containerSize;
        this.pledge = Money.of(pledge);
        this.pledgeContainer = Money.of(pledgeContainer);
        this.minStock = minStock;
    }

    public Ingredient() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public @NotNull BigDecimal getPrice() {
        return price;
    }

    public void setPrice(@NotNull BigDecimal price) {
        this.price = Money.unit(price);
    }

    public int getContainerSize() {
        return containerSize;
    }

    public void setContainerSize(int containerSize) {
        this.containerSize = containerSize;
    }

    public BigDecimal getPledge() {
        return pledge;
    }

    public void setPledge(BigDecimal deposit) {
        this.pledge = Money.of(deposit);
    }

    public BigDecimal getPledgeContainer() {
        return pledgeContainer;
    }

    public void setPledgeContainer(BigDecimal depositContainer) {
        this.pledgeContainer = Money.of(depositContainer);
    }

    public int getMinStock() {
        return minStock;
    }

    public void setMinStock(int minStock) {
        this.minStock = minStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int compareTo(Ingredient o) {
        return name.compareTo(o.name);
    }
}
