package dev.chojo.simplepos.service;

import dev.chojo.simplepos.controller.exceptions.ConflictException;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.dto.IngredientRequest;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.RecipeEntryRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngredientService {
    private final IngredientRepository repository;
    private final RecipeEntryRepository recipeEntryRepository;
    private final StorageRepository storageRepository;
    private final CategoryRepository categoryRepository;

    public IngredientService(IngredientRepository repository, RecipeEntryRepository recipeEntryRepository,
                             StorageRepository storageRepository, CategoryRepository categoryRepository) {
        this.repository = repository;
        this.recipeEntryRepository = recipeEntryRepository;
        this.storageRepository = storageRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Deletes an ingredient nothing refers to. One that is part of a recipe or has stock history is kept, because
     * deleting it would take the recipes and the cost of past sales with it; deactivate it instead.
     *
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if the ingredient does not exist
     * @throws ConflictException if a recipe or a storage batch refers to it
     */
    @Transactional
    public void delete(int id) {
        Ingredient ingredient = repository.require(id);
        if (recipeEntryRepository.existsByIngredient_IdAndRecipeIsNotNull(id) || storageRepository.existsByIngredient_Id(id)) {
            throw new ConflictException("%s is used in recipes or storage. Deactivate it instead.".formatted(ingredient.getName()));
        }
        repository.delete(ingredient);
    }

    public Ingredient create(Ingredient ingredient) {
        return repository.save(ingredient);
    }

    /**
     * Creates an active ingredient.
     *
     * @throws dev.chojo.simplepos.controller.exceptions.BadRequestException if the category does not exist
     */
    @Transactional
    public Ingredient create(IngredientRequest request) {
        return repository.save(request.toIngredient(categoryRepository.requireReferenced(request.categoryId())));
    }

    /**
     * Changes an ingredient's values. Whether it is active is not changed here.
     *
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if the ingredient does not exist
     * @throws dev.chojo.simplepos.controller.exceptions.BadRequestException if the category does not exist
     */
    @Transactional
    public Ingredient update(int id, IngredientRequest request) {
        Ingredient ingredient = repository.require(id);
        request.applyTo(ingredient, categoryRepository.requireReferenced(request.categoryId()));
        return repository.save(ingredient);
    }
}
