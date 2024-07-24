package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Category;
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

@SpringBootTest
class CategoryServiceTest {
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
    void createCategory() {
        Category test = categoryService.create("Test");
        Assertions.assertNotNull(test.getId());
    }

    @Test
    void createMultipleCategories() {
        Category first = categoryService.create("Test");
        Category second = categoryService.create("Test");
        Assertions.assertNotNull(first.getId());
        Assertions.assertNotNull(second.getId());
        Assertions.assertNotEquals(first.getId(), second.getId());
    }
}
