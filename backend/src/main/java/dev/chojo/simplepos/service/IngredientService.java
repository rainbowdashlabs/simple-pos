package dev.chojo.simplepos.service;

import dev.chojo.simplepos.controller.exceptions.ConflictException;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.RecipeEntryRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class IngredientService {
    private final IngredientRepository repository;
    private final RecipeEntryRepository recipeEntryRepository;
    private final StorageRepository storageRepository;

    public IngredientService(IngredientRepository repository, RecipeEntryRepository recipeEntryRepository,
                             StorageRepository storageRepository) {
        this.repository = repository;
        this.recipeEntryRepository = recipeEntryRepository;
        this.storageRepository = storageRepository;
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
        if (recipeEntryRepository.existsByIngredient_Id(id) || storageRepository.existsByIngredient_Id(id)) {
            throw new ConflictException("%s is used in recipes or storage. Deactivate it instead.".formatted(ingredient.getName()));
        }
        repository.delete(ingredient);
    }

    public Ingredient create(Ingredient ingredient) {
        return repository.save(ingredient);
    }
}
