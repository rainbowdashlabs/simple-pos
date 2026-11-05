package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Category;

public interface CategoryRepository extends LookupRepository<Category> {
    @Override
    default String entityName() {
        return "Category";
    }
}
