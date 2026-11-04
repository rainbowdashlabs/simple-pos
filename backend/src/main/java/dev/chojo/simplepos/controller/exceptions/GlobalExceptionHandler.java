package dev.chojo.simplepos.controller.exceptions;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Renders every exception escaping a controller, and every exception the security filters hand to the
 * {@code HandlerExceptionResolver}, as a {@link ProblemDetail} response.
 * <p>
 * Spring MVC exceptions (missing parameters, unreadable bodies, validation) and {@code ErrorResponseException}s such
 * as {@link NotFoundException} keep their own status through {@link ResponseEntityExceptionHandler}. Security, JWT and
 * database constraint failures are mapped to 401, 403 and 409. Anything else becomes a 500.
 * <p>
 * Every response is logged once together with the request line (method and path, never the query, which may carry
 * credentials): client errors as a one-line warning, server errors as an error with the full stack trace.
 * Authentication failures caused by infrastructure ({@link AuthenticationServiceException}) count as server errors.
 * 401 responses carry {@code WWW-Authenticate: Bearer}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> handleDisabled(DisabledException e, WebRequest request) {
        return respond(e, HttpStatus.FORBIDDEN, "Account is disabled. Ask an administrator to enable it.", request);
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<Object> handleAuthenticationService(AuthenticationServiceException e, WebRequest request) {
        return handleUnexpected(e.getCause() instanceof Exception cause ? cause : e, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException e, WebRequest request) {
        return respond(e, HttpStatus.UNAUTHORIZED, e.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException e, WebRequest request) {
        return respond(e, HttpStatus.FORBIDDEN, e.getMessage(), request);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Object> handleJwt(JwtException e, WebRequest request) {
        return respond(e, HttpStatus.UNAUTHORIZED, "Invalid or expired token", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException e, WebRequest request) {
        return respond(e, HttpStatus.CONFLICT, NestedExceptionUtils.getMostSpecificCause(e).getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception e, WebRequest request) {
        ResponseStatus annotated = AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class);
        if (annotated != null) {
            String reason = annotated.reason().isEmpty() ? e.getMessage() : annotated.reason();
            return respond(e, annotated.code(), reason, request);
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        problem.setTitle(e.getClass().getSimpleName());
        return handleExceptionInternal(e, problem, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        if (isCommitted(request)) {
            log.warn("{} failed after the response was committed: {}", requestLine(request), e.toString());
            return null;
        }
        logFailure(e, status, request);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.addAll(headers);
        boolean unauthorized = status.value() == HttpStatus.UNAUTHORIZED.value();
        if (unauthorized && !responseHeaders.containsHeader(HttpHeaders.WWW_AUTHENTICATE)) {
            responseHeaders.set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        }
        return super.handleExceptionInternal(e, body, responseHeaders, status, request);
    }

    private ResponseEntity<Object> respond(Exception e, HttpStatus status, String detail, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        return handleExceptionInternal(e, problem, new HttpHeaders(), status, request);
    }

    private static boolean isCommitted(WebRequest request) {
        return request instanceof ServletWebRequest servlet
                && servlet.getResponse() != null
                && servlet.getResponse().isCommitted();
    }

    private static void logFailure(Exception e, HttpStatusCode status, WebRequest request) {
        String line = requestLine(request);
        if (status.is5xxServerError()) {
            log.error("{} failed with {} {}: {}", line, status.value(), e.getClass().getName(), e.getMessage(), e);
        } else {
            log.warn("{} failed with {} {}: {}", line, status.value(), e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private static String requestLine(WebRequest request) {
        if (!(request instanceof ServletWebRequest servlet)) return request.getDescription(false);
        HttpServletRequest req = servlet.getRequest();
        return req.getMethod() + " " + req.getRequestURI();
    }
}
