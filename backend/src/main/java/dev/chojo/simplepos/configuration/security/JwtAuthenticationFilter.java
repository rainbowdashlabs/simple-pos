package dev.chojo.simplepos.configuration.security;

import dev.chojo.simplepos.controller.exceptions.UnauthorizedException;
import dev.chojo.simplepos.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Authenticates requests that carry a {@code Bearer} login token. Requests without one pass through unchanged; a
 * token that is invalid, expired or belongs to a disabled user ends the request with the error response of the
 * {@link SecurityErrorResolver}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String INVALID_TOKEN = "Invalid or expired token";

    private final JwtService jwtService;
    private final UserService userService;
    private final SecurityErrorResolver securityErrorResolver;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService, SecurityErrorResolver securityErrorResolver) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.securityErrorResolver = securityErrorResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            authenticate(authorization.substring(7), request);
        } catch (Exception e) {
            securityErrorResolver.resolve(request, response, e);
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Puts the user of a valid login token into the security context.
     *
     * @throws UnauthorizedException if the token belongs to no user or is not a valid login token
     * @throws DisabledException     if the user is disabled
     * @throws io.jsonwebtoken.JwtException if the token is malformed, expired or wrongly signed
     */
    private void authenticate(String jwt, HttpServletRequest request) {
        if (jwt.isBlank()) throw new UnauthorizedException(INVALID_TOKEN);
        String username = jwtService.extractUsername(jwt);
        if (username == null) throw new UnauthorizedException(INVALID_TOKEN);
        if (SecurityContextHolder.getContext().getAuthentication() != null) return;

        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(jwt, userDetails, Map.of("type", "login"))) {
            throw new UnauthorizedException(INVALID_TOKEN);
        }
        if (!userDetails.isEnabled()) throw new DisabledException("Account is disabled");

        var authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
