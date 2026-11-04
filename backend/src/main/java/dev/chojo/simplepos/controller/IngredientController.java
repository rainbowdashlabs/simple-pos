package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.dto.IngredientRequest;
import dev.chojo.simplepos.entity.response.Listing;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.service.IngredientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ingredient")
public class IngredientController {
    private final IngredientRepository ingredientRepository;
    private final IngredientService ingredientService;

    public IngredientController(IngredientRepository ingredientRepository, IngredientService ingredientService) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientService = ingredientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Ingredient> create(@Valid @RequestBody IngredientRequest ingredient) {
        return ResponseEntity.accepted().body(ingredientService.create(ingredient));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Listing<Ingredient>> all() {
        return ResponseEntity.ok(Listing.map(ingredientRepository.findAll(), Ingredient::getCategory));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Ingredient> update(@PathVariable int id, @Valid @RequestBody IngredientRequest ingredient) {
        return ResponseEntity.accepted().body(ingredientService.update(id, ingredient));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Void> delete(@PathVariable int id) {
        ingredientService.delete(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Ingredient> get(@PathVariable int id) {
        return ResponseEntity.ok(ingredientRepository.require(id));
    }

    @PatchMapping("/{id}/active")
    ResponseEntity<Ingredient> toggleActive(@PathVariable int id) {
        Ingredient ingredient = ingredientRepository.require(id);
        ingredient.setActive(!ingredient.isActive());
        return ResponseEntity.ok(ingredientRepository.save(ingredient));
    }
}
