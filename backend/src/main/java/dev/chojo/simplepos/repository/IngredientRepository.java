package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Ingredient;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
    /**
     * Finds an ingredient.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no ingredient with this id exists
     */
    default Ingredient require(Integer id) {
        return findById(requireId(id)).orElseThrow(() -> new NotFoundException("Ingredient", id));
    }

    /**
     * Finds an ingredient and locks its row until the surrounding transaction ends. Without a surrounding
     * transaction the lock only lasts for this single statement.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no ingredient with this id exists
     */
    default Ingredient requireLocked(Integer id) {
        return findByIdForUpdate(requireId(id)).orElseThrow(() -> new NotFoundException("Ingredient", id));
    }

    /**
     * Locks the rows of all given ingredients in ascending id order. Every caller that locks more than one
     * ingredient goes through here, so two transactions never wait on each other's locks in opposite order.
     *
     * @throws BadRequestException if one of the ids is missing
     * @throws NotFoundException if one of the ingredients does not exist
     */
    default void requireLockedInOrder(Collection<Integer> ids) {
        ids.stream().map(this::requireId).distinct().sorted().forEach(this::requireLocked);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Ingredient i WHERE i.id = ?1")
    Optional<Ingredient> findByIdForUpdate(int id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Ingredient SET price = ?2 where id = ?1")
    int updateByIdIs(int id, double price);

    @Query("SELECT Ingredient ")
    List<Ingredient> findBy(double price);

    private Integer requireId(Integer id) {
        if (id == null) throw new BadRequestException("Ingredient id is missing");
        return id;
    }
}
