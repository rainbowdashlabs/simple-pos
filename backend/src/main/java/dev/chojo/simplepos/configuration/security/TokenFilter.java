package dev.chojo.simplepos.configuration.security;

import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class TokenFilter extends OncePerRequestFilter {

    private static final Logger log = getLogger(TokenFilter.class);
    private final UserService userService;

    public TokenFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("authorization");
        if (authorization == null) {
            throw new BadRequestException();
        }

        Optional<Token> token = userService.getToken(authorization);
        token.orElseThrow(() -> new AuthenticationException("Invalid token"));
        if (token.get().getValidUntil().isBefore(Instant.now())) {
            throw new AuthenticationException("Token expired");
        }
        log.debug("Authorization granted");
    }
}
