package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.repository.IngredientRepository;
import org.springframework.stereotype.Service;

@Service
public class IngredientService {
    private final IngredientRepository repository;

    public IngredientService(IngredientRepository repository) {
        this.repository = repository;
    }

    public Ingredient create(Ingredient ingredient) {
        return repository.save(ingredient);
    }
}
