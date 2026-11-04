package dev.chojo.simplepos.service;

import dev.chojo.simplepos.controller.exceptions.ConflictException;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.dto.CategoryRequest;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository repository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;

    public CategoryService(CategoryRepository repository, ProductRepository productRepository,
                           IngredientRepository ingredientRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public Category create(String name) {
        return repository.saveAndFlush(new Category(null, name));
    }

    public Category create(CategoryRequest request) {
        return create(request.name());
    }

    /**
     * Renames a category.
     *
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if the category does not exist
     */
    @Transactional
    public Category update(int id, CategoryRequest request) {
        Category category = repository.require(id);
        category.setName(request.name());
        return repository.save(category);
    }

    /**
     * Deletes a category no product or ingredient uses any more. Deleted products still pointing at it are detached
     * from it first.
     *
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if the category does not exist
     * @throws ConflictException if a product that is not deleted, or an ingredient, is in the category
     */
    @Transactional
    public void delete(int id) {
        Category category = repository.require(id);
        long products = productRepository.countByCategory_IdAndDeletedFalse(id);
        long ingredients = ingredientRepository.countByCategory_Id(id);
        if (products > 0 || ingredients > 0) throw inUse(category.getName(), products, ingredients);
        productRepository.detachDeletedFromCategory(id);
        repository.deleteById(id);
        try {
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("%s was just given a product or ingredient. Move it to another category first."
                    .formatted(category.getName()));
        }
    }

    private static ConflictException inUse(String name, long products, long ingredients) {
        return new ConflictException("%s is still used by %d product(s) and %d ingredient(s), inactive ones included. Move them to another category first."
                .formatted(name, products, ingredients));
    }
}
