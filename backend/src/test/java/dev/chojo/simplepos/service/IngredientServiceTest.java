package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IngredientServiceTest {
    @Autowired
    IngredientService service;

    @Autowired
    CategoryService categoryService;

    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));


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
    void createIngredient() {
        Category test = categoryService.create("test");
        Ingredient ingredient = service.create(new Ingredient(null, test, "Test", 0d, 0, 0, 0, 0));
        Assertions.assertNotNull(ingredient.getId());
    }
}
