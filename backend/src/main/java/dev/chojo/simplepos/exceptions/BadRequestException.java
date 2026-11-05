package dev.chojo.simplepos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

/**
 * Signals a request that is malformed, e.g. because a required value is missing. Rendered as 400 with an
 * {@link ErrorCode}.
 */
public class BadRequestException extends ErrorResponseException {
    public BadRequestException(ErrorCode code, Object... args) {
        super(HttpStatus.BAD_REQUEST, code.problem(HttpStatus.BAD_REQUEST, args), null);
    }
}
