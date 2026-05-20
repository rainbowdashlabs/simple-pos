package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IngredientRepositoryTest {
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(new Category(null, "Beverages"));
    }

    @Test
    void shouldSaveIngredient() {
        Ingredient ingredient = ingredientRepository.save(
                new Ingredient(null, category, "Cola", 1.5, 24, 0.25, 3.0, 10));
        assertNotNull(ingredient.getId());
    }

    @Test
    void shouldFindById() {
        Ingredient saved = ingredientRepository.save(
                new Ingredient(null, category, "Fanta", 1.2, 12, 0.15, 2.0, 5));
        Optional<Ingredient> found = ingredientRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Fanta", found.get().getName());
    }

    @Test
    void shouldFindAll() {
        ingredientRepository.save(new Ingredient(null, category, "A", 1.0, 1, 0, 0, 0));
        ingredientRepository.save(new Ingredient(null, category, "B", 2.0, 1, 0, 0, 0));

        List<Ingredient> all = ingredientRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void shouldUpdatePriceById() {
        Ingredient saved = ingredientRepository.save(
                new Ingredient(null, category, "Sprite", 1.0, 6, 0, 0, 0));

        int updated = ingredientRepository.updateByIdIs(saved.getId(), 2.5);
        assertEquals(1, updated);

        ingredientRepository.flush();
        Ingredient found = ingredientRepository.findById(saved.getId()).orElseThrow();
        assertEquals(2.5, found.getPrice(), 0.001);
    }

    @Test
    void shouldPersistAllFields() {
        Ingredient saved = ingredientRepository.save(
                new Ingredient(null, category, "Beer", 0.8, 20, 0.08, 1.6, 15));
        Ingredient found = ingredientRepository.findById(saved.getId()).orElseThrow();

        assertEquals("Beer", found.getName());
        assertEquals(0.8, found.getPrice(), 0.001);
        assertEquals(20, found.getContainerSize());
        assertEquals(0.08, found.getPledge(), 0.001);
        assertEquals(1.6, found.getPledgeContainer(), 0.001);
        assertEquals(15, found.getMinStock());
        assertEquals(category.getId(), found.getCategory().getId());
    }

    @Test
    void shouldDeleteIngredient() {
        Ingredient saved = ingredientRepository.save(
                new Ingredient(null, category, "Deletable", 1.0, 1, 0, 0, 0));
        ingredientRepository.deleteById(saved.getId());
        assertTrue(ingredientRepository.findById(saved.getId()).isEmpty());
    }
}
