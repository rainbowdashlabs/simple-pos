package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;

    public ProductService(ProductRepository productRepository, IngredientRepository ingredientRepository) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public Product create(Product product) {
        for (RecipeEntry entry : product.getRecipe().getEntries()) {
            if (entry.getIngredient().getId() == null) {
                entry.setIngredient(ingredientRepository.save(entry.getIngredient()));
            }
        }
        return productRepository.save(product);
    }
}
