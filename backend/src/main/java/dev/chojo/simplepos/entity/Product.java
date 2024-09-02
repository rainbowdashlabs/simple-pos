package dev.chojo.simplepos.entity;

import dev.chojo.simplepos.entity.dto.LazyProduct;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;

@Entity
public class Product implements Comparable<Product> {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @NotNull
    private String name;

    @NotNull
    private double price;

    private boolean active = true;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "id")
    private Recipe recipe;

    public Product(Integer id, Category category, String name, double price, boolean active, Recipe recipe) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = price;
        this.active = active;
        this.recipe = recipe;
    }

    public Product() {
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
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

    @NotNull
    public double getPrice() {
        return price;
    }

    public void setPrice(@NotNull double price) {
        this.price = price;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LazyProduct asLazyProduct() {
        return new LazyProduct(id, category, name, price, active);
    }

    @Override
    public int compareTo(Product o) {
        return name.compareTo(o.name);
    }
}
