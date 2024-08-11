package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;

    public ProductService(ProductRepository productRepository, IngredientRepository ingredientRepository, RecipeRepository recipeRepository) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
    }

    public Product create(Product product) {
        Recipe recipe = product.getRecipe();
        // save without recipe first
        product.setRecipe(null);
        product = productRepository.save(product);
        recipe.setProduct(product);
        List<RecipeEntry> entries = recipe.getEntries();
        recipe.setEntries(Collections.emptyList());
        recipe = recipeRepository.save(recipe);
        for (RecipeEntry entry : entries) {
            entry.setRecipe(recipe);
            if (entry.getIngredient().getId() == null) {
                entry.setIngredient(ingredientRepository.save(entry.getIngredient()));
            }
        }
        recipe.setEntries(entries);
        product.setRecipe(recipe);
        return productRepository.save(product);
    }
}
