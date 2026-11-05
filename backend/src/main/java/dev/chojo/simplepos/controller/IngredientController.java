package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.controller.validation.PageNumber;
import dev.chojo.simplepos.controller.validation.PageSize;
import dev.chojo.simplepos.entity.dto.IngredientRequest;
import dev.chojo.simplepos.entity.response.IngredientResponse;
import dev.chojo.simplepos.entity.response.PageResponse;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.service.IngredientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    ResponseEntity<IngredientResponse> create(@Valid @RequestBody IngredientRequest ingredient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(IngredientResponse.of(ingredientService.create(ingredient)));
    }

    @GetMapping
    PageResponse<IngredientResponse> all(@RequestParam(required = false) Integer categoryId,
                                         @RequestParam(defaultValue = "100") @PageSize int limit,
                                         @RequestParam(defaultValue = "0") @PageNumber int page) {
        return PageResponse.of(ingredientRepository.findPage(categoryId, PageRequest.of(page, limit)), IngredientResponse::of);
    }

    @PatchMapping("/{id}")
    ResponseEntity<IngredientResponse> update(@PathVariable int id, @Valid @RequestBody IngredientRequest ingredient) {
        return ResponseEntity.ok(IngredientResponse.of(ingredientService.update(id, ingredient)));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable int id) {
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    ResponseEntity<IngredientResponse> get(@PathVariable int id) {
        return ResponseEntity.ok(IngredientResponse.of(ingredientRepository.require(id)));
    }

    @PatchMapping("/{id}/active")
    ResponseEntity<IngredientResponse> toggleActive(@PathVariable int id) {
        return ResponseEntity.ok(IngredientResponse.of(ingredientService.toggleActive(id)));
    }
}
