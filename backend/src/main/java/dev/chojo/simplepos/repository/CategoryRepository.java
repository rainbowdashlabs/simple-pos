package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    /**
     * Finds a category.
     *
     * @throws NotFoundException if no category with this id exists
     */
    default Category require(int id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Category", id));
    }
}
