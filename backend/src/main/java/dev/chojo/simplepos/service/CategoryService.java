package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public Category create(String name) {
        return repository.saveAndFlush(new Category(null, name));
    }
}
