package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.response.Listing;
import dev.chojo.simplepos.repository.IngredientRepository;
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

@RestController
@RequestMapping("/api/ingredient")
public class IngredientController {
    private final IngredientRepository ingredientRepository;

    public IngredientController(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Ingredient> create(@RequestBody Ingredient ingredient) {
        return ResponseEntity.accepted().body(ingredientRepository.save(ingredient));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Listing<Ingredient>> all() {
        return ResponseEntity.ok(Listing.map(ingredientRepository.findAll(), Ingredient::getCategory));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Ingredient> update(@RequestBody Ingredient ingredient) {
        return ResponseEntity.accepted().body(ingredientRepository.save(ingredient));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Void> delete(@RequestBody Ingredient ingredient) {
        // TODO check that ingredient is unused
        ingredientRepository.deleteById(ingredient.getId());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Ingredient> get(@PathVariable int id) {
        return ingredientRepository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
