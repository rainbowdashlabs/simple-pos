package dev.chojo.simplepos.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.chojo.simplepos.entity.dto.LazyProduct;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

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
    @Column(precision = 12, scale = Money.SCALE, nullable = false)
    private BigDecimal price;

    private boolean active = true;

    @Column(columnDefinition = "boolean default false")
    private boolean deleted = false;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Recipe recipe;

    /**
     * The same with the amounts as {@code double}, for test fixtures and demo data.
     */
    public Product(Integer id, Category category, String name, double price, boolean active, Recipe recipe) {
        this(id, category, name, Money.of(price), active, recipe);
    }

    public Product(Integer id, Category category, String name, BigDecimal price, boolean active, Recipe recipe) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = Money.of(price);
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

    /**
     * What one unit of the product costs at the ingredients' current prices; 0 without a recipe. Sent with the
     * product so the interface shows the same margin everywhere; the profit in the statistics uses what the sold
     * units actually cost instead.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal getUnitCost() {
        if (recipe == null || recipe.getEntries() == null) return Money.ZERO;
        return Money.of(recipe.getEntries().stream()
                .map(entry -> entry.getIngredient().getPrice().multiply(BigDecimal.valueOf(entry.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
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
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(@NotNull BigDecimal price) {
        this.price = Money.of(price);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LazyProduct asLazyProduct() {
        return new LazyProduct(id, category, name, price, active);
    }

    @Override
    public int compareTo(Product o) {
        return name.compareTo(o.name);
    }
}
