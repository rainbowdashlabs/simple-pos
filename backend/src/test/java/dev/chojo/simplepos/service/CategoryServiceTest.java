package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceTest {
    @Autowired
    CategoryService categoryService;
    @Autowired
    CategoryRepository categoryRepository;

    @Test
    void createCategory() {
        Category test = categoryService.create("Test");
        assertNotNull(test.getId());
    }

    @Test
    void createMultipleCategories() {
        Category first = categoryService.create("CatA");
        Category second = categoryService.create("CatB");
        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void createdCategoryIsPersisted() {
        Category created = categoryService.create("Persisted");
        Optional<Category> found = categoryRepository.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Persisted", found.get().getName());
    }

    @Test
    void createCategoryWithSameName() {
        Category first = categoryService.create("Duplicate");
        Category second = categoryService.create("Duplicate");
        assertNotEquals(first.getId(), second.getId());
        assertEquals(first.getName(), second.getName());
    }
}
