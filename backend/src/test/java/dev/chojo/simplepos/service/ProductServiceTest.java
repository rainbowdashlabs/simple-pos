package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@SpringBootTest
class ProductServiceTest {
    @Autowired
    ProductService products;
    @Autowired
    CategoryService categories;
    @Autowired
    IngredientService ingredients;

    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
    @Autowired
    private IngredientService ingredientService;


    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
    }

    @AfterEach
    void afterEach() {
    }

    @AfterAll
    static void afterAll() {
        postgresContainer.stop();
    }

    @Test
    void createProduct() {
        Category cat = categories.create("Test");
        Ingredient ingredient = ingredients.create(new Ingredient(null, cat, "Test", 0d, 0, 0, 0, 0));
        RecipeEntry recipeEntry = new RecipeEntry(null, ingredient, 1);
        Recipe recipe = new Recipe(null, null, List.of(recipeEntry));
        Product product = new Product(null, cat, "Test", 1.0, true, recipe);
        product = products.create(product);
        Assertions.assertNotNull(product.getId());
        Assertions.assertNotNull(product.getRecipe().getId());
        for (RecipeEntry entry : product.getRecipe().getEntries()) {
            Assertions.assertNotNull(entry.getId());
        }
    }

    @Test
    void createMultipleCategories() {
    }
}
