package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.exceptions.ConflictException;
import dev.chojo.simplepos.exceptions.NotFoundException;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static dev.chojo.simplepos.MoneyAssertions.assertMoney;
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
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    ProductService productService;

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
        assertMoney(1.2, found.getPrice());
        assertEquals(12, found.getContainerSize());
        assertMoney(0.15, found.getPledge());
        assertMoney(2.0, found.getPledgeContainer());
        assertEquals(5, found.getMinStock());
    }

    @Test
    void createMultipleIngredients() {
        Ingredient first = service.create(new Ingredient(null, category, "Sprite", 1.0, 6, 0, 0, 0));
        Ingredient second = service.create(new Ingredient(null, category, "Water", 0.5, 6, 0, 0, 0));
        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void deletesAnUnusedIngredient() {
        Ingredient ingredient = service.create(new Ingredient(null, category, "Unused", 1.0, 1, 0, 0, 0));

        service.delete(ingredient.getId());

        assertTrue(ingredientRepository.findById(ingredient.getId()).isEmpty());
    }

    @Test
    void refusesToDeleteAnIngredientWithStock() {
        Ingredient ingredient = service.create(new Ingredient(null, category, "Stocked", 1.0, 1, 0, 0, 0));
        storageRepository.save(new Storage(null, ingredient, Instant.now(), 1.0, 5, 0));

        assertThrows(ConflictException.class, () -> service.delete(ingredient.getId()));
        assertTrue(ingredientRepository.findById(ingredient.getId()).isPresent());
    }

    @Test
    void refusesToDeleteAnUnknownIngredient() {
        assertThrows(NotFoundException.class, () -> service.delete(999999));
    }

    @Test
    void refusesToDeleteAnIngredientInARecipe() {
        Ingredient ingredient = service.create(new Ingredient(null, category, "InRecipe", 1.0, 1, 0, 0, 0));
        productService.create(new Product(null, category, "WithRecipe", 2.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 1)))));

        assertThrows(ConflictException.class, () -> service.delete(ingredient.getId()));
    }
}
