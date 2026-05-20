package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.service.ProductService;
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

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductRepositoryTest {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    ProductService productService;

    private Category category;
    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(new Category(null, "ProdRepoCat"));
        ingredient = ingredientRepository.save(
                new Ingredient(null, category, "ProdRepoIngredient", 0.5, 12, 0, 0, 0));
    }

    @Test
    void shouldSaveAndFindProduct() {
        Recipe recipe = new Recipe(null, null, Collections.emptyList());
        Product product = productService.create(new Product(null, category, "TestProduct", 2.0, true, recipe));

        Optional<Product> found = productRepository.findById(product.getId());
        assertTrue(found.isPresent());
        assertEquals("TestProduct", found.get().getName());
        assertEquals(2.0, found.get().getPrice(), 0.001);
    }

    @Test
    void shouldFindAllProducts() {
        Recipe r1 = new Recipe(null, null, Collections.emptyList());
        Recipe r2 = new Recipe(null, null, Collections.emptyList());
        productService.create(new Product(null, category, "Product1", 1.0, true, r1));
        productService.create(new Product(null, category, "Product2", 2.0, true, r2));

        List<Product> all = productRepository.findAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void shouldCheckExistsById() {
        Recipe recipe = new Recipe(null, null, Collections.emptyList());
        Product product = productService.create(new Product(null, category, "ExistsTest", 1.0, true, recipe));
        assertTrue(productRepository.existsById(product.getId()));
        assertFalse(productRepository.existsById(999999));
    }

    @Test
    void shouldDeleteProduct() {
        Recipe recipe = new Recipe(null, null, Collections.emptyList());
        Product product = productService.create(new Product(null, category, "Deletable", 1.0, true, recipe));
        productRepository.deleteById(product.getId());
        assertFalse(productRepository.existsById(product.getId()));
    }

    @Test
    void shouldLoadProductWithRecipe() {
        RecipeEntry entry = new RecipeEntry(null, ingredient, 2);
        Recipe recipe = new Recipe(null, null, List.of(entry));
        Product product = productService.create(new Product(null, category, "WithRecipe", 3.0, true, recipe));

        Product found = productRepository.findById(product.getId()).orElseThrow();
        assertNotNull(found.getRecipe());
    }

    @Test
    void shouldConvertToLazyProduct() {
        Recipe recipe = new Recipe(null, null, Collections.emptyList());
        Product product = productService.create(new Product(null, category, "LazyTest", 2.5, true, recipe));

        var lazy = product.asLazyProduct();
        assertEquals(product.getId(), lazy.id());
        assertEquals("LazyTest", lazy.name());
        assertEquals(2.5, lazy.price(), 0.001);
        assertTrue(lazy.active());
    }
}
