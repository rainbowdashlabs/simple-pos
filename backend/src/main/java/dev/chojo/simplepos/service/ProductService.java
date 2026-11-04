package dev.chojo.simplepos.service;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.RecipeEntryRepository;
import dev.chojo.simplepos.repository.RecipeRepository;
import jakarta.transaction.Transactional;
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

    /**
     * Creates a product together with its recipe, and the ingredients of the recipe that do not exist yet, in one
     * transaction. The recipe is linked to the product through {@code recipe.product_id}. Ids the request carries for
     * the recipe or its entries are ignored, so a new product never takes over another product's recipe.
     *
     * @throws BadRequestException if the recipe is missing
     */
    @Transactional
    public Product create(Product product) {
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            product.setCategory(categoryRepository.findById(product.getCategory().getId()).orElse(product.getCategory()));
        }

        Recipe recipe = product.getRecipe();
        if (recipe == null || recipe.getEntries() == null) {
            throw new BadRequestException("Recipe is missing");
        }
        recipe.setId(null);
        recipe.getEntries().forEach(entry -> entry.setId(null));
        product.setRecipe(null);
        product = productRepository.save(product);
        recipe.setProduct(product);
        List<RecipeEntry> originalEntries = new ArrayList<>(recipe.getEntries());
        recipe.setEntries(Collections.emptyList());
        recipe = recipeRepository.save(recipe);
        List<RecipeEntry> savedEntries = new ArrayList<>();
        for (RecipeEntry entry : originalEntries) {
            entry.setRecipe(recipe);
            if (entry.getIngredient().getId() == null) {
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

    /**
     * Saves the changes to an existing product and its recipe, in one transaction. The changes always go to the
     * recipe stored for the product, whatever recipe id the request carries; a product without a stored recipe gets
     * one.
     *
     * @throws BadRequestException if the id or the recipe is missing
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if the product does not exist
     */
    @Transactional
    public Product update(Product product) {
        Product stored = productRepository.require(product.getId());
        if (product.getRecipe() == null || product.getRecipe().getEntries() == null) {
            throw new BadRequestException("Recipe is missing");
        }
        Recipe target = stored.getRecipe() != null
                ? stored.getRecipe()
                : recipeRepository.save(new Recipe(null, stored, new ArrayList<>()));
        product.getRecipe().setId(target.getId());
        product.getRecipe().setProduct(product);
        for (RecipeEntry entry : product.getRecipe().getEntries()) {
            entry.setRecipe(product.getRecipe());
        }
        List<RecipeEntry> entries = new ArrayList<>(product.getRecipe().getEntries().stream().map(recipeEntryRepository::save).toList());
        product.getRecipe().setEntries(entries);
        productRepository.save(product);
        return productRepository.require(product.getId());
    }
}
