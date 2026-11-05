package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.exceptions.BadRequestException;
import dev.chojo.simplepos.exceptions.ErrorCode;
import dev.chojo.simplepos.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Finding an entity by an id from a request, with the answers every repository gives: a missing id is a bad request,
 * an unknown id is not found when it is the resource asked for, and a bad request when the request only refers to it.
 *
 * @param <T> the entity
 */
@NoRepositoryBean
public interface LookupRepository<T> extends JpaRepository<T, Integer> {
    /**
     * The entity's name as error messages show it.
     */
    String entityName();

    /**
     * Finds an entity.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException   if no entity with this id exists
     */
    default T require(Integer id) {
        return findById(requireId(id)).orElseThrow(() -> new NotFoundException(entityName(), id));
    }

    /**
     * Finds an entity that a request refers to by id. A missing one is the request's fault, so it is reported as a
     * bad request rather than as a missing resource.
     *
     * @throws BadRequestException if the id is missing or no entity with this id exists
     */
    default T requireReferenced(Integer id) {
        return findById(requireId(id))
                .orElseThrow(() -> new BadRequestException(ErrorCode.REFERENCE_UNKNOWN, entityName(), id));
    }

    /**
     * The id, if the request gave one.
     *
     * @throws BadRequestException if the id is missing
     */
    default Integer requireId(Integer id) {
        if (id == null) throw new BadRequestException(ErrorCode.ID_MISSING, entityName());
        return id;
    }
}
