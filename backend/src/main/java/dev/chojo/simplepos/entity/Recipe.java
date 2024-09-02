package dev.chojo.simplepos.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.util.List;

@Entity
public class Recipe {
    @Id
    @GeneratedValue
    private Integer id;

    @OneToOne
    @JsonIgnore
    private Product product;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "recipe_id")
    private List<RecipeEntry> entries;

    public Recipe(Integer id, Product product, List<RecipeEntry> entries) {
        this.id = id;
        this.product = product;
        this.entries = entries;
    }

    public Recipe() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<RecipeEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<RecipeEntry> ingredients) {
        this.entries = ingredients;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
