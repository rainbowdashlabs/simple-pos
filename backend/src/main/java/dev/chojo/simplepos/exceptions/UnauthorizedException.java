package dev.chojo.simplepos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Signals that the caller could not be authenticated, e.g. because of an unknown user or an invalid token. Rendered
 * as 401.
 */
public class UnauthorizedException extends ErrorResponseException {
    public UnauthorizedException(String detail) {
        super(HttpStatus.UNAUTHORIZED, ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, detail), null);
    }
}
