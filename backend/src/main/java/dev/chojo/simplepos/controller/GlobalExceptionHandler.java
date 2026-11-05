package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.LoginTokenAuthentication;
import dev.chojo.simplepos.exceptions.ErrorCode;
import dev.chojo.simplepos.exceptions.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Renders every exception escaping a controller, and every exception the security filters hand to the
 * {@code HandlerExceptionResolver}, as a {@link ProblemDetail} response.
 * <p>
 * Spring MVC exceptions (missing parameters, unreadable bodies, validation) and {@code ErrorResponseException}s such
 * as {@link NotFoundException} keep their own status through {@link ResponseEntityExceptionHandler}. Security, JWT and
 * database constraint and lock failures are mapped to 401, 403 and 409. Anything else becomes a 500.
 * <p>
 * Every response is logged once together with the request line (method and path, never the query, which may carry
 * credentials): client errors as a one-line warning, server errors as an error with the full stack trace.
 * Authentication failures caused by infrastructure ({@link AuthenticationServiceException}) count as server errors.
 * 401 responses carry {@code WWW-Authenticate: Bearer}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * ProblemDetail property set on the 403 of a disabled account, so clients can tell it from other 403s.
     */
    public static final String ACCOUNT_DISABLED = "accountDisabled";
    private static final Logger log = getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> handleDisabled(DisabledException e, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                "Account is disabled. Ask an administrator to enable it.");
        problem.setProperty(ACCOUNT_DISABLED, true);
        return handleExceptionInternal(e, problem, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
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

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<Object> handleRejectedToken(OAuth2AuthenticationException e, WebRequest request) {
        return respond(e, HttpStatus.UNAUTHORIZED, LoginTokenAuthentication.INVALID_TOKEN, request);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Object> handleLockFailure(PessimisticLockingFailureException e, WebRequest request) {
        return handleExceptionInternal(e, ErrorCode.CONCURRENT_CHANGE.problem(HttpStatus.CONFLICT), new HttpHeaders(),
                HttpStatus.CONFLICT, request);
    }

    /**
     * Answers a write the database refused, such as a duplicate, with a generic conflict. The database's own message
     * names tables and constraints, so it is only logged with the failure, never sent to the client.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException e, WebRequest request) {
        return handleExceptionInternal(e, ErrorCode.DATA_CONFLICT.problem(HttpStatus.CONFLICT), new HttpHeaders(),
                HttpStatus.CONFLICT, request);
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

    /**
     * Answers a request body that failed validation with 400. The detail lists each rejected field with its reason,
     * and {@code errors} carries the same as {@code field}/{@code message} pairs.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(),
                        "message", Objects.requireNonNullElse(error.getDefaultMessage(), "is invalid")))
                .toList();
        return invalid(e, errors, headers, request);
    }

    private ResponseEntity<Object> invalid(Exception e, List<Map<String, String>> errors, HttpHeaders headers,
                                           WebRequest request) {
        String detail = errors.isEmpty()
                ? "The request is invalid."
                : errors.stream().map(error -> error.get("field") + ": " + error.get("message")).collect(Collectors.joining("; "));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setProperty("code", ErrorCode.INVALID_REQUEST.value());
        problem.setProperty("errors", errors);
        return handleExceptionInternal(e, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Answers request parameters that failed validation, such as a page size out of range, with 400 listing each
     * rejected parameter, like {@link #handleMethodArgumentNotValid}.
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException e,
                                                                            HttpHeaders headers, HttpStatusCode status,
                                                                            WebRequest request) {
        List<Map<String, String>> errors = e.getParameterValidationResults().stream()
                .flatMap(result -> {
                    String parameter = Objects.requireNonNullElse(
                            result.getMethodParameter().getParameterName(), "parameter");
                    if (result instanceof ParameterErrors elementErrors && elementErrors.getContainerIndex() != null) {
                        String element = "%s[%d]".formatted(parameter, elementErrors.getContainerIndex());
                        return elementErrors.getAllErrors().stream().map(error -> Map.of(
                                "field", error instanceof FieldError field ? element + "." + field.getField() : element,
                                "message", Objects.requireNonNullElse(error.getDefaultMessage(), "is invalid")));
                    }
                    return result.getResolvableErrors().stream().map(error -> Map.of(
                            "field", parameter,
                            "message", Objects.requireNonNullElse(error.getDefaultMessage(), "is invalid")));
                })
                .toList();
        return invalid(e, errors, headers, request);
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
