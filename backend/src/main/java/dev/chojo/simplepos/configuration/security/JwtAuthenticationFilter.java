package dev.chojo.simplepos.configuration.security;

import dev.chojo.simplepos.service.UserService;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserService userService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService, HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String jwt = authorization.substring(7);
            String username = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (username != null && authentication == null) {
                UserDetails userDetails = this.userService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails, Map.of("type", "login"))) {
                    var authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    log.info("token invalid");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return;
                    //      response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token invalid");
                }
            }

            filterChain.doFilter(request, response);
        } catch (SignatureException e) {
            filterChain.doFilter(request, response);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getOutputStream().print("Token invalid");
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);

        }
    }
}
