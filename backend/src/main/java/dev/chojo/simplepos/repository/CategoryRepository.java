package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    /**
     * Finds a category.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no category with this id exists
     */
    default Category require(Integer id) {
        return findById(requireId(id)).orElseThrow(() -> new NotFoundException("Category", id));
    }

    private Integer requireId(Integer id) {
        if (id == null) throw new BadRequestException("Category id is missing");
        return id;
    }
}
