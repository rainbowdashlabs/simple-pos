package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.repository.IngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IngredientServiceTest {
    @Autowired
    IngredientService service;
    @Autowired
    CategoryService categoryService;
    @Autowired
    IngredientRepository ingredientRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = categoryService.create("IngredientTestCat");
    }

    @Test
    void createIngredient() {
        Ingredient ingredient = service.create(new Ingredient(null, category, "Cola", 1.5, 24, 0.25, 3.0, 10));
        assertNotNull(ingredient.getId());
    }

    @Test
    void createdIngredientRetainsFields() {
        Ingredient ingredient = service.create(new Ingredient(null, category, "Fanta", 1.2, 12, 0.15, 2.0, 5));

        Ingredient found = ingredientRepository.findById(ingredient.getId()).orElseThrow();
        assertEquals("Fanta", found.getName());
        assertEquals(1.2, found.getPrice(), 0.001);
        assertEquals(12, found.getContainerSize());
        assertEquals(0.15, found.getPledge(), 0.001);
        assertEquals(2.0, found.getPledgeContainer(), 0.001);
        assertEquals(5, found.getMinStock());
    }

    @Test
    void createMultipleIngredients() {
        Ingredient first = service.create(new Ingredient(null, category, "Sprite", 1.0, 6, 0, 0, 0));
        Ingredient second = service.create(new Ingredient(null, category, "Water", 0.5, 6, 0, 0, 0));
        assertNotEquals(first.getId(), second.getId());
    }
}
