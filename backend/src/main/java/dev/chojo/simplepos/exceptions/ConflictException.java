package dev.chojo.simplepos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

/**
 * Signals that a request contradicts the current state, e.g. a value that has to be unique already exists. Rendered
 * as 409 with an {@link ErrorCode}.
 */
public class ConflictException extends ErrorResponseException {
    public ConflictException(ErrorCode code, Object... args) {
        super(HttpStatus.CONFLICT, code.problem(HttpStatus.CONFLICT, args), null);
    }
}
