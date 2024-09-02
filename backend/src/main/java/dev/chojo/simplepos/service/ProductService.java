package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.RecipeEntryRepository;
import dev.chojo.simplepos.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeEntryRepository recipeEntryRepository;

    public ProductService(ProductRepository productRepository, IngredientRepository ingredientRepository, RecipeRepository recipeRepository, RecipeEntryRepository recipeEntryRepository) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
        this.recipeEntryRepository = recipeEntryRepository;
    }

    public Product create(Product product) {
        Recipe recipe = product.getRecipe();
        // save without recipe first
        product.setRecipe(null);
        product = productRepository.save(product);
        recipe.setProduct(product);
        List<RecipeEntry> entries = new ArrayList<>();
        recipe.setEntries(Collections.emptyList());
        recipe = recipeRepository.save(recipe);
        // Create ingredients if they are new
        for (RecipeEntry entry : recipe.getEntries()) {
            entry.setRecipe(recipe);
            if (entry.getIngredient().getId() == null) {
                entry.setIngredient(ingredientRepository.save(entry.getIngredient()));
            }
            entries.add(recipeEntryRepository.save(entry));
        }
        recipe.setEntries(entries);
        product.setRecipe(recipe);
        return productRepository.save(product);
    }

    public Product update(Product product) {
        product.getRecipe().setProduct(product);
        for (RecipeEntry entry : product.getRecipe().getEntries()) {
            entry.setRecipe(product.getRecipe());
        }
        List<RecipeEntry> entries = product.getRecipe().getEntries().stream().map(recipeEntryRepository::save).toList();
        product.getRecipe().setEntries(entries);
        productRepository.save(product);
        return productRepository.findById(product.getId()).get();
    }
}
