package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeEntryRepository recipeEntryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, IngredientRepository ingredientRepository, RecipeRepository recipeRepository, RecipeEntryRepository recipeEntryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
        this.recipeEntryRepository = recipeEntryRepository;
    }

    public Product create(Product product) {
        // Resolve category from DB to avoid detached entity issues
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            product.setCategory(categoryRepository.findById(product.getCategory().getId()).orElse(product.getCategory()));
        }

        Recipe recipe = product.getRecipe();
        // save without recipe first
        product.setRecipe(null);
        product = productRepository.save(product);
        recipe.setProduct(product);
        List<RecipeEntry> originalEntries = new ArrayList<>(recipe.getEntries());
        recipe.setEntries(Collections.emptyList());
        recipe = recipeRepository.save(recipe);
        // Create ingredients if they are new
        List<RecipeEntry> savedEntries = new ArrayList<>();
        for (RecipeEntry entry : originalEntries) {
            entry.setRecipe(recipe);
            if (entry.getIngredient().getId() == null) {
                // Resolve ingredient category from DB
                if (entry.getIngredient().getCategory() != null && entry.getIngredient().getCategory().getId() != null) {
                    entry.getIngredient().setCategory(
                            categoryRepository.findById(entry.getIngredient().getCategory().getId())
                                    .orElse(entry.getIngredient().getCategory()));
                }
                entry.setIngredient(ingredientRepository.save(entry.getIngredient()));
            }
            savedEntries.add(recipeEntryRepository.save(entry));
        }
        recipe.setEntries(savedEntries);
        product.setRecipe(recipe);
        return productRepository.save(product);
    }

    public Product update(Product product) {
        product.getRecipe().setProduct(product);
        for (RecipeEntry entry : product.getRecipe().getEntries()) {
            entry.setRecipe(product.getRecipe());
        }
        List<RecipeEntry> entries = new ArrayList<>(product.getRecipe().getEntries().stream().map(recipeEntryRepository::save).toList());
        product.getRecipe().setEntries(entries);
        productRepository.save(product);
        return productRepository.findById(product.getId()).get();
    }
}
