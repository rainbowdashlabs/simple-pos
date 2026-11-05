package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.dto.IngredientRequest;
import dev.chojo.simplepos.entity.dto.ProductRequest;
import dev.chojo.simplepos.entity.dto.RecipeEntryRequest;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.RecipeRepository;
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

import static dev.chojo.simplepos.MoneyAssertions.assertMoney;
import static dev.chojo.simplepos.MoneyAssertions.money;
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
    RecipeRepository recipeRepository;
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
        assertMoney(1.5, found.get().getPrice());
        assertTrue(found.get().isActive());
        assertEquals(category.getId(), found.get().getCategory().getId());
    }

    @Test
    void updateProduct() {
        RecipeEntry recipeEntry = new RecipeEntry(null, ingredient, 1);
        Recipe recipe = new Recipe(null, null, List.of(recipeEntry));
        Product product = productService.create(new Product(null, category, "OldName", 1.0, true, recipe));

        Product updated = productService.update(product.getId(), request("NewName", 2.0, entry(1)));

        assertEquals("NewName", updated.getName());
        assertMoney(2.0, updated.getPrice());
    }

    @Test
    void updateKeepsTheActiveState() {
        RecipeEntry recipeEntry = new RecipeEntry(null, ingredient, 1);
        Recipe recipe = new Recipe(null, null, List.of(recipeEntry));
        Product product = productService.create(new Product(null, category, "Inactive", 1.0, true, recipe));
        product.setActive(false);
        productRepository.save(product);

        Product updated = productService.update(product.getId(), request("Inactive", 1.5, entry(1)));

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

    @Test
    void loadsItsOwnRecipeWhenRecipeAndProductIdsDiffer() {
        Product other = productService.create(new Product(null, category, "Other", 1.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 3)))));
        Product product;
        do {
            recipeRepository.save(new Recipe(null, null, Collections.emptyList()));
            product = productService.create(new Product(null, category, "Shifted", 2.0, true,
                    new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 1)))));
        } while (product.getId().equals(product.getRecipe().getId()));
        entityManager.flush();
        entityManager.clear();

        Product loaded = productRepository.findById(product.getId()).orElseThrow();

        assertNotEquals(loaded.getId(), loaded.getRecipe().getId());
        assertEquals(1, loaded.getRecipe().getEntries().size());
        assertEquals(1, loaded.getRecipe().getEntries().get(0).getAmount());
        assertEquals(3, productRepository.findById(other.getId()).orElseThrow().getRecipe().getEntries().get(0).getAmount());
    }

    @Test
    void updateReplacesTheEntriesOfTheStoredRecipe() {
        Product other = productService.create(new Product(null, category, "Bystander", 1.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 3)))));
        Product product = productService.create(new Product(null, category, "Target", 2.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 1)))));
        Integer recipeId = product.getRecipe().getId();
        Integer otherRecipeId = other.getRecipe().getId();
        entityManager.flush();
        entityManager.clear();

        productService.update(product.getId(), request("Target", 2.0, entry(2)));
        entityManager.flush();
        entityManager.clear();

        Recipe recipe = productRepository.findById(product.getId()).orElseThrow().getRecipe();
        assertEquals(recipeId, recipe.getId());
        assertEquals(1, recipe.getEntries().size());
        assertEquals(2, recipe.getEntries().get(0).getAmount());
        Recipe untouched = productRepository.findById(other.getId()).orElseThrow().getRecipe();
        assertEquals(otherRecipeId, untouched.getId());
        assertEquals(1, untouched.getEntries().size());
        assertEquals(3, untouched.getEntries().get(0).getAmount());
    }

    @Test
    void createIgnoresARecipeIdOfAnotherProduct() {
        Product other = productService.create(new Product(null, category, "Owner", 1.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 3)))));
        Integer otherRecipeId = other.getRecipe().getId();
        entityManager.flush();
        entityManager.clear();

        Product created = productService.create(new Product(null, category, "Intruder", 2.0, true,
                new Recipe(otherRecipeId, null, List.of(new RecipeEntry(null, ingredient, 1)))));
        entityManager.flush();
        entityManager.clear();

        assertNotEquals(otherRecipeId, created.getRecipe().getId());
        Recipe untouched = productRepository.findById(other.getId()).orElseThrow().getRecipe();
        assertEquals(otherRecipeId, untouched.getId());
        assertEquals(3, untouched.getEntries().get(0).getAmount());
    }

    @Test
    void createWithoutRecipeIsRejected() {
        assertThrows(BadRequestException.class,
                () -> productService.create(new Product(null, category, "Bare", 1.0, true, null)));
    }

    @Test
    void updateGivesAProductWithoutRecipeOne() {
        Product product = productService.create(new Product(null, category, "NoRecipe", 2.0, true,
                new Recipe(null, null, Collections.emptyList())));
        entityManager.flush();
        entityManager.clear();
        entityManager.createNativeQuery("DELETE FROM recipe WHERE product_id = ?1")
                .setParameter(1, product.getId())
                .executeUpdate();
        assertNull(productRepository.findById(product.getId()).orElseThrow().getRecipe());

        productService.update(product.getId(), request("NoRecipe", 2.0, entry(1)));
        entityManager.flush();
        entityManager.clear();

        Product loaded = productRepository.findById(product.getId()).orElseThrow();
        assertNotNull(loaded.getRecipe());
        assertEquals(1, loaded.getRecipe().getEntries().size());
    }

    @Test
    void updateLeavesNoOrphanedRecipeEntries() {
        Product product = productService.create(new Product(null, category, "Orphans", 2.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 1)))));
        entityManager.flush();
        entityManager.clear();

        productService.update(product.getId(), request("Orphans", 2.0, entry(4)));
        entityManager.flush();

        Number orphans = (Number) entityManager.createNativeQuery("SELECT count(*) FROM recipe_entry WHERE recipe_id IS NULL")
                .getSingleResult();
        assertEquals(0, orphans.intValue());
    }

    @Test
    void updateDoesNotReviveADeletedProduct() {
        Product product = productService.create(new Product(null, category, "Gone", 2.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 1)))));
        product.setDeleted(true);
        productRepository.save(product);
        entityManager.flush();
        entityManager.clear();

        assertThrows(NotFoundException.class, () -> productService.update(product.getId(), request("Gone", 2.0, entry(1))));
        assertTrue(productRepository.findById(product.getId()).orElseThrow().isDeleted());
    }

    @Test
    void createFromRequestWithNewIngredient() {
        Product created = productService.create(new ProductRequest("Lemonade", category.getId(), money(2.0),
                List.of(new RecipeEntryRequest(null, new IngredientRequest("Lemon", category.getId(), money(0.4), 6, money(0.0), money(0.0), 2), 1))));

        assertTrue(created.isActive());
        assertEquals("Lemon", created.getRecipe().getEntries().get(0).getIngredient().getName());
        assertNotNull(created.getRecipe().getEntries().get(0).getIngredient().getId());
    }

    @Test
    void recipeEntryNeedsExactlyOneIngredient() {
        assertThrows(BadRequestException.class, () -> productService.create(new ProductRequest("Both", category.getId(), money(1.0),
                List.of(new RecipeEntryRequest(ingredient.getId(), new IngredientRequest("X", category.getId(), money(0.1), 0, money(0.0), money(0.0), 0), 1)))));
        assertThrows(BadRequestException.class, () -> productService.create(new ProductRequest("Neither", category.getId(), money(1.0),
                List.of(new RecipeEntryRequest(null, null, 1)))));
    }

    @Test
    void unknownCategoryOrIngredientIsABadRequest() {
        assertThrows(BadRequestException.class, () -> productService.create(new ProductRequest("NoCat", 999_999, money(1.0), List.of(entry(1)))));
        assertThrows(BadRequestException.class, () -> productService.create(new ProductRequest("NoIngr", category.getId(), money(1.0),
                List.of(new RecipeEntryRequest(999_999, null, 1)))));
    }

    private ProductRequest request(String name, double price, RecipeEntryRequest... entries) {
        return new ProductRequest(name, category.getId(), money(price), List.of(entries));
    }

    private RecipeEntryRequest entry(int amount) {
        return new RecipeEntryRequest(ingredient.getId(), null, amount);
    }
}
