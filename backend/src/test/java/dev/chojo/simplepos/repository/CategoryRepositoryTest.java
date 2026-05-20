package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryRepositoryTest {
    @Autowired
    CategoryRepository categoryRepository;

    @Test
    void shouldSaveCategory() {
        Category category = categoryRepository.save(new Category(null, "Drinks"));
        assertNotNull(category.getId());
        assertEquals("Drinks", category.getName());
    }

    @Test
    void shouldFindById() {
        Category saved = categoryRepository.save(new Category(null, "Food"));
        Optional<Category> found = categoryRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Food", found.get().getName());
    }

    @Test
    void shouldReturnEmptyForMissingId() {
        Optional<Category> found = categoryRepository.findById(999999);
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindAll() {
        categoryRepository.save(new Category(null, "Alpha"));
        categoryRepository.save(new Category(null, "Beta"));
        categoryRepository.save(new Category(null, "Gamma"));

        List<Category> all = categoryRepository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    void shouldFindAllSortedByName() {
        categoryRepository.save(new Category(null, "Zebra"));
        categoryRepository.save(new Category(null, "Apple"));
        categoryRepository.save(new Category(null, "Mango"));

        List<Category> sorted = categoryRepository.findAll(Sort.by("name").ascending());
        assertEquals("Apple", sorted.get(0).getName());
        assertEquals("Mango", sorted.get(1).getName());
        assertEquals("Zebra", sorted.get(2).getName());
    }

    @Test
    void shouldDeleteById() {
        Category saved = categoryRepository.save(new Category(null, "ToDelete"));
        assertTrue(categoryRepository.existsById(saved.getId()));

        categoryRepository.deleteById(saved.getId());
        assertFalse(categoryRepository.existsById(saved.getId()));
    }

    @Test
    void shouldUpdateCategory() {
        Category saved = categoryRepository.save(new Category(null, "OldName"));
        saved.setName("NewName");
        categoryRepository.save(saved);

        Category updated = categoryRepository.findById(saved.getId()).orElseThrow();
        assertEquals("NewName", updated.getName());
    }

    @Test
    void shouldCountCategories() {
        categoryRepository.save(new Category(null, "One"));
        categoryRepository.save(new Category(null, "Two"));

        assertEquals(2, categoryRepository.count());
    }
}
