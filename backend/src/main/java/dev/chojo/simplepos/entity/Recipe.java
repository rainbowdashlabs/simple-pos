package dev.chojo.simplepos.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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

    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL)
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
