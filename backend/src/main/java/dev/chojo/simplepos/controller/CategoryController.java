package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.dto.CategoryRequest;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.service.CategoryService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    public CategoryController(CategoryRepository categoryRepository, CategoryService categoryService) {
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    @PostMapping
    ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(category));
    }

    @GetMapping
    ResponseEntity<List<Category>> all() {
        return ResponseEntity.ok(categoryRepository.findAll(Sort.by("name").ascending()));
    }

    @GetMapping("/{id}")
    ResponseEntity<Category> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryRepository.require(id));
    }

    @PatchMapping("/{id}")
    ResponseEntity<Category> update(@PathVariable int id, @Valid @RequestBody CategoryRequest category) {
        return ResponseEntity.ok(categoryService.update(id, category));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable int id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
