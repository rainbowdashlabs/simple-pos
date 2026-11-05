package dev.chojo.simplepos.service;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.dto.ProductRequest;
import dev.chojo.simplepos.entity.dto.RecipeEntryRequest;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.RecipeEntryRepository;
import dev.chojo.simplepos.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Creates a product from a request, with a recipe of existing ingredients and of new ones created with it.
     *
     * @throws BadRequestException if the category or an ingredient does not exist, or a recipe entry names neither or
     *                             both an existing and a new ingredient
     */
    @Transactional
    public Product create(ProductRequest request) {
        Category category = categoryRepository.requireReferenced(request.categoryId());
        List<RecipeEntry> entries = request.recipe().stream().map(this::toEntry).toList();
        return create(new Product(null, category, request.name(), request.price(), true,
                new Recipe(null, null, new ArrayList<>(entries))));
    }

    /**
     * Changes an existing product and replaces its recipe, in one transaction. Only the fields of the request are
     * taken; whether the product is deleted or active never changes here, and the changes always go to the recipe
     * stored for the product. A product without a stored recipe gets one.
     *
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if the product does not exist or is deleted
     * @throws BadRequestException if the category or an ingredient does not exist, or a recipe entry names neither or
     *                             both an existing and a new ingredient
     */
    @Transactional
    public Product update(int id, ProductRequest request) {
        Product stored = productRepository.requireNotDeleted(id);
        stored.setName(request.name());
        stored.setCategory(categoryRepository.requireReferenced(request.categoryId()));
        stored.setPrice(request.price());

        Recipe recipe = stored.getRecipe() != null
                ? stored.getRecipe()
                : recipeRepository.save(new Recipe(null, stored, new ArrayList<>()));
        stored.setRecipe(recipe);
        List<RecipeEntry> replaced = new ArrayList<>(recipe.getEntries());
        List<RecipeEntry> entries = new ArrayList<>();
        for (RecipeEntryRequest entryRequest : request.recipe()) {
            RecipeEntry entry = toEntry(entryRequest);
            if (entry.getIngredient().getId() == null) {
                entry.setIngredient(ingredientRepository.save(entry.getIngredient()));
            }
            entry.setRecipe(recipe);
            entries.add(entry);
        }
        recipe.getEntries().clear();
        recipeEntryRepository.deleteAll(replaced);
        recipe.getEntries().addAll(recipeEntryRepository.saveAll(entries));
        return productRepository.save(stored);
    }

    private RecipeEntry toEntry(RecipeEntryRequest request) {
        if ((request.ingredientId() == null) == (request.ingredient() == null)) {
            throw new BadRequestException("Each recipe entry needs either an existing ingredient or a new one");
        }
        Ingredient ingredient = request.ingredientId() != null
                ? ingredientRepository.requireReferenced(request.ingredientId())
                : request.ingredient().toIngredient(categoryRepository.requireReferenced(request.ingredient().categoryId()));
        return new RecipeEntry(null, ingredient, request.amount());
    }
}
