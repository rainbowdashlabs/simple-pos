package dev.chojo.simplepos.configuration.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * Hands exceptions raised in the security filter chain to the MVC {@link HandlerExceptionResolver}, so they are
 * rendered and logged like controller exceptions. Usable as entry point, access denied handler and authentication
 * failure handler.
 * <p>
 * Should no resolver handle the exception, the response still gets a status matching the exception instead of an
 * empty 200.
 */
@Component
public class SecurityErrorResolver {
    private final HandlerExceptionResolver handlerExceptionResolver;

    public SecurityErrorResolver(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    public void resolve(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException {
        if (handlerExceptionResolver.resolveException(request, response, null, exception) != null) return;
        if (response.isCommitted()) return;
        response.sendError(fallbackStatus(exception).value());
    }

    private static HttpStatus fallbackStatus(Exception exception) {
        if (exception instanceof AuthenticationException || exception instanceof JwtException) return HttpStatus.UNAUTHORIZED;
        if (exception instanceof AccessDeniedException) return HttpStatus.FORBIDDEN;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
