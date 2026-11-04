package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.controller.exceptions.ConflictException;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.response.Listing;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.ProductRepository;
import jakarta.persistence.EntityManager;
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
class CategoryServiceTest {
    @Autowired
    CategoryService categoryService;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductService productService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    EntityManager entityManager;

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

    @Test
    void deletingAnUnusedCategoryRemovesIt() {
        Category category = categoryService.create("Unused");

        categoryService.delete(category.getId());

        assertFalse(categoryRepository.existsById(category.getId()));
    }

    @Test
    void deletingACategoryInUseIsAConflict() {
        Category category = categoryService.create("InUse");
        Ingredient ingredient = ingredientRepository.save(new Ingredient(null, category, "Used", 1.0, 0, 0, 0, 0));
        productService.create(new Product(null, category, "Sold", 2.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 1)))));

        ConflictException conflict = assertThrows(ConflictException.class, () -> categoryService.delete(category.getId()));
        assertTrue(conflict.getBody().getDetail().contains("1 product(s) and 1 ingredient(s)"));
        assertTrue(categoryRepository.existsById(category.getId()));
    }

    @Test
    void deletedProductsDoNotKeepACategory() {
        Category category = categoryService.create("OnlyDeleted");
        Category other = categoryService.create("Other");
        Ingredient ingredient = ingredientRepository.save(new Ingredient(null, other, "Elsewhere", 1.0, 0, 0, 0, 0));
        Product product = productService.create(new Product(null, category, "Retired", 2.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 1)))));
        product.setDeleted(true);
        productRepository.save(product);
        entityManager.flush();

        categoryService.delete(category.getId());
        entityManager.flush();
        entityManager.clear();

        assertFalse(categoryRepository.existsById(category.getId()));
        assertNull(productRepository.findById(product.getId()).orElseThrow().getCategory());
    }

    @Test
    void entriesWithoutCategoryAreListedLast() {
        Category category = categoryService.create("Listed");
        Ingredient ingredient = ingredientRepository.save(new Ingredient(null, category, "Grouped", 1.0, 0, 0, 0, 0));
        Ingredient loose = ingredientRepository.save(new Ingredient(null, null, "Loose", 1.0, 0, 0, 0, 0));

        Listing<Ingredient> listing = Listing.map(List.of(loose, ingredient), Ingredient::getCategory);

        assertEquals(2, listing.categories().size());
        assertSame(Category.UNCATEGORISED, listing.categories().get(1).category());
        assertEquals(List.of(loose), listing.categories().get(1).entries());
    }
}
