package dev.chojo.simplepos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Signals a request that is malformed, e.g. because a required value is missing. Rendered as 400.
 */
public class BadRequestException extends ErrorResponseException {
    public BadRequestException(String detail) {
        super(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail), null);
    }
}
