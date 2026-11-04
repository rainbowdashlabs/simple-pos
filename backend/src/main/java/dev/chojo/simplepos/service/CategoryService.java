package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.dto.CategoryRequest;
import dev.chojo.simplepos.repository.CategoryRepository;
import jakarta.transaction.Transactional;
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

    public Category create(CategoryRequest request) {
        return create(request.name());
    }

    /**
     * Renames a category.
     *
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if the category does not exist
     */
    @Transactional
    public Category update(int id, CategoryRequest request) {
        Category category = repository.require(id);
        category.setName(request.name());
        return repository.save(category);
    }
}
