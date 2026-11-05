package dev.chojo.simplepos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Calls the handler directly for failures that no endpoint can trigger on purpose, such as an unexpected exception or
 * a lock failure, so no extra controller and Spring context are needed.
 */
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final ServletWebRequest request = new ServletWebRequest(
            new MockHttpServletRequest("GET", "/api/test"), new MockHttpServletResponse());

    @Test
    void shouldRenderUnexpectedExceptionAsInternalServerError() {
        ResponseEntity<Object> response = handler.handleUnexpected(new IllegalStateException("boom"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problem = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("IllegalStateException", problem.getTitle());
        assertEquals("boom", problem.getDetail());
    }

    @Test
    void shouldRenderLockFailureAsConflict() {
        ResponseEntity<Object> response = handler.handleLockFailure(new CannotAcquireLockException("deadlock"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("The data was changed concurrently. Please retry.", problem.getDetail());
    }

    @Test
    void shouldRenderConstraintViolationAsConflictWithoutTheDatabaseMessage() {
        var violation = new DataIntegrityViolationException("could not execute statement", new RuntimeException("duplicate key"));

        ResponseEntity<Object> response = handler.handleDataIntegrity(violation, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals("data_conflict", problem.getProperties().get("code"));
        assertFalse(problem.getDetail().contains("duplicate key"));
    }
}
