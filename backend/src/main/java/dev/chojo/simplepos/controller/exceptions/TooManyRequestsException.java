package dev.chojo.simplepos.controller.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.time.Duration;

/**
 * Signals that a client tried too often in too short a time. Rendered as 429 with a {@code Retry-After} header and
 * the same number of seconds as the {@code retryAfter} property.
 */
public class TooManyRequestsException extends ErrorResponseException {
    public TooManyRequestsException(String detail, Duration retryAfter) {
        super(HttpStatus.TOO_MANY_REQUESTS, problem(detail, retryAfter), null);
        getHeaders().set(HttpHeaders.RETRY_AFTER, String.valueOf(seconds(retryAfter)));
    }

    private static ProblemDetail problem(String detail, Duration retryAfter) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, detail);
        problem.setProperty("retryAfter", seconds(retryAfter));
        return problem;
    }

    private static long seconds(Duration duration) {
        return Math.max(1, (duration.toMillis() + 999) / 1000);
    }
}
