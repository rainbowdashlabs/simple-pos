package dev.chojo.simplepos.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Signals that an entity requested by id does not exist or was deleted. Rendered as 404 with the entity name and id
 * as {@link ProblemDetail} properties.
 */
public class NotFoundException extends ErrorResponseException {
    public NotFoundException(String entity, Object id) {
        super(HttpStatus.NOT_FOUND, problem(entity, id), null);
    }

    private static ProblemDetail problem(String entity, Object id) {
        String detail = "%s %s not found".formatted(entity, id);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail);
        problem.setProperty("entity", entity);
        problem.setProperty("id", id);
        return problem;
    }
}
