package dev.chojo.simplepos.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Signals that a request contradicts the current state, e.g. a value that has to be unique already exists. Rendered
 * as 409.
 */
public class ConflictException extends ErrorResponseException {
    public ConflictException(String detail) {
        super(HttpStatus.CONFLICT, ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail), null);
    }
}
