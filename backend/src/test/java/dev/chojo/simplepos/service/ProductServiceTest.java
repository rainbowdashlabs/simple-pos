package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest {
    @Autowired
    ProductService productService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    IngredientService ingredientService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    EntityManager entityManager;

    private Category category;
    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        category = categoryService.create("ProductTestCat");
        ingredient = ingredientService.create(new Ingredient(null, category, "TestIngredient", 0.5, 12, 0, 0, 0));
    }

    @Test
    void createProduct() {
        RecipeEntry recipeEntry = new RecipeEntry(null, ingredient, 1);
        Recipe recipe = new Recipe(null, null, List.of(recipeEntry));
        Product product = new Product(null, category, "Beer", 2.5, true, recipe);

        product = productService.create(product);

        assertNotNull(product.getId());
        assertNotNull(product.getRecipe());
        assertNotNull(product.getRecipe().getId());
    }

    @Test
    void createProductWithMultipleRecipeEntries() {
        Ingredient second = ingredientService.create(new Ingredient(null, category, "Syrup", 0.3, 1, 0, 0, 0));
        RecipeEntry entry1 = new RecipeEntry(null, ingredient, 2);
        RecipeEntry entry2 = new RecipeEntry(null, second, 1);
        Recipe recipe = new Recipe(null, null, List.of(entry1, entry2));
        Product product = new Product(null, category, "Mixed Drink", 3.0, true, recipe);

        product = productService.create(product);

        assertNotNull(product.getId());
        assertNotNull(product.getRecipe().getId());
    }

    @Test
    void createProductPreservesFields() {
        RecipeEntry recipeEntry = new RecipeEntry(null, ingredient, 1);
        Recipe recipe = new Recipe(null, null, List.of(recipeEntry));
        Product product = productService.create(new Product(null, category, "Soda", 1.5, true, recipe));

        Optional<Product> found = productRepository.findById(product.getId());
        assertTrue(found.isPresent());
        assertEquals("Soda", found.get().getName());
        assertEquals(1.5, found.get().getPrice(), 0.001);
        assertTrue(found.get().isActive());
        assertEquals(category.getId(), found.get().getCategory().getId());
    }

    @Test
    void updateProduct() {
        RecipeEntry recipeEntry = new RecipeEntry(null, ingredient, 1);
        Recipe recipe = new Recipe(null, null, List.of(recipeEntry));
        Product product = productService.create(new Product(null, category, "OldName", 1.0, true, recipe));

        product.setName("NewName");
        product.setPrice(2.0);
        Product updated = productService.update(product);

        assertEquals("NewName", updated.getName());
        assertEquals(2.0, updated.getPrice(), 0.001);
    }

    @Test
    void updateProductDeactivate() {
        RecipeEntry recipeEntry = new RecipeEntry(null, ingredient, 1);
        Recipe recipe = new Recipe(null, null, List.of(recipeEntry));
        Product product = productService.create(new Product(null, category, "Active", 1.0, true, recipe));

        product.setActive(false);
        Product updated = productService.update(product);

        assertFalse(updated.isActive());
    }

    @Test
    void createProductWithNewIngredientPreservesCategory() {
        // Simulates the frontend ProductCreate flow: new product + new ingredient, same category
        Ingredient newIngredient = new Ingredient(null, category, "NewIngr", 0.8, 24, 0.25, 3.0, 10);
        RecipeEntry entry = new RecipeEntry(null, newIngredient, 1);
        Recipe recipe = new Recipe(null, null, List.of(entry));
        Product product = new Product(null, category, "NewProduct", 2.0, true, recipe);

        product = productService.create(product);

        assertNotNull(product.getId());
        assertNotNull(product.getCategory());
        assertEquals(category.getId(), product.getCategory().getId());
        RecipeEntry savedEntry = product.getRecipe().getEntries().get(0);
        assertNotNull(savedEntry.getIngredient().getId());
        assertNotNull(savedEntry.getIngredient().getCategory());
        assertEquals(category.getId(), savedEntry.getIngredient().getCategory().getId());
    }

    @Test
    void createProductWithDetachedCategoryPreservesCategory() {
        // Simulates JSON deserialization: category is a detached object (not managed by Hibernate)
        entityManager.flush();
        entityManager.clear();

        // Create detached Category references (as Jackson would from JSON)
        Category detachedCategory = new Category(category.getId(), category.getName());
        Ingredient newIngredient = new Ingredient(null, detachedCategory, "DetachedIngr", 0.8, 24, 0, 0, 0);
        RecipeEntry entry = new RecipeEntry(null, newIngredient, 1);
        Recipe recipe = new Recipe(null, null, List.of(entry));
        Product product = new Product(null, detachedCategory, "DetachedProduct", 2.0, true, recipe);

        product = productService.create(product);

        // Re-fetch to verify DB state
        entityManager.flush();
        entityManager.clear();
        Product found = productRepository.findById(product.getId()).orElseThrow();
        assertNotNull(found.getCategory(), "Product category should not be null");
        assertEquals(category.getId(), found.getCategory().getId());

        RecipeEntry savedEntry = found.getRecipe().getEntries().get(0);
        assertNotNull(savedEntry.getIngredient().getCategory(), "Ingredient category should not be null");
        assertEquals(category.getId(), savedEntry.getIngredient().getCategory().getId());
    }

    @Test
    void createProductWithEmptyRecipe() {
        Recipe recipe = new Recipe(null, null, Collections.emptyList());
        Product product = new Product(null, category, "NoIngredients", 1.0, true, recipe);

        product = productService.create(product);
        assertNotNull(product.getId());
        assertNotNull(product.getRecipe().getId());
        assertTrue(product.getRecipe().getEntries().isEmpty());
    }
}
