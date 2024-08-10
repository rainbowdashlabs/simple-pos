package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Category> create(@RequestBody Category category) {
        return ResponseEntity.accepted().body(categoryRepository.save(category));
    }

    @GetMapping("/")
    ResponseEntity<List<Category>> all() {
        return ResponseEntity.ok(categoryRepository.findAll(Sort.by("name").ascending()));
    }

    @GetMapping("/{id}")
    ResponseEntity<Category> getById(@PathVariable Integer id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Category> update(@RequestBody Category category) {
        return ResponseEntity.accepted().body(categoryRepository.save(category));
    }

    @DeleteMapping("/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Void> delete(@RequestBody Category category) {
        // TODO check that no entity is associated
        categoryRepository.delete(category);
        return ResponseEntity.accepted().build();
    }
}
