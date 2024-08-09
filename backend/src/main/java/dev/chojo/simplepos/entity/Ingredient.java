package dev.chojo.simplepos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;

@Entity
public class Ingredient {
    @Id
    @GeneratedValue
    private Integer id;

    @OneToOne
    private Category category;

    @NotNull
    private String name;
    @NotNull
    private Double price;
    @Column(nullable = false)
    private int containerSize;
    @Column( nullable = false)
    private double deposit;
    @Column(nullable = false)
    private double depositContainer;
    @Column(nullable = false)
    private int minStock;

    public Ingredient(Integer id, Category category, String name, Double price, int containerSize, double deposit, double depositContainer, int minStock) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = price;
        this.containerSize = containerSize;
        this.deposit = deposit;
        this.depositContainer = depositContainer;
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

    public @NotNull Double getPrice() {
        return price;
    }

    public void setPrice(@NotNull Double price) {
        this.price = price;
    }

    public int getContainerSize() {
        return containerSize;
    }

    public void setContainerSize(int containerSize) {
        this.containerSize = containerSize;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public double getDepositContainer() {
        return depositContainer;
    }

    public void setDepositContainer(double depositContainer) {
        this.depositContainer = depositContainer;
    }

    public int getMinStock() {
        return minStock;
    }

    public void setMinStock(int minStock) {
        this.minStock = minStock;
    }
}
