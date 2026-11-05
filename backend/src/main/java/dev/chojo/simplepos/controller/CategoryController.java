package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.controller.validation.PageNumber;
import dev.chojo.simplepos.controller.validation.PageSize;
import dev.chojo.simplepos.entity.dto.CategoryRequest;
import dev.chojo.simplepos.entity.response.CategoryResponse;
import dev.chojo.simplepos.entity.response.PageResponse;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.of(categoryService.create(category)));
    }

    @GetMapping
    PageResponse<CategoryResponse> all(@RequestParam(defaultValue = "100") @PageSize int limit,
                                       @RequestParam(defaultValue = "0") @PageNumber int page) {
        return PageResponse.of(categoryRepository.findAll(PageRequest.of(page, limit, Sort.by("name", "id"))), CategoryResponse::of);
    }

    @GetMapping("/{id}")
    ResponseEntity<CategoryResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(CategoryResponse.of(categoryRepository.require(id)));
    }

    @PatchMapping("/{id}")
    ResponseEntity<CategoryResponse> update(@PathVariable int id, @Valid @RequestBody CategoryRequest category) {
        return ResponseEntity.ok(CategoryResponse.of(categoryService.update(id, category)));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable int id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
