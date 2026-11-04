package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.JwtService;
import dev.chojo.simplepos.configuration.security.RateLimiter;
import dev.chojo.simplepos.controller.exceptions.UnauthorizedException;
import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.security.TokenDto;
import dev.chojo.simplepos.entity.security.TokenResponse;
import dev.chojo.simplepos.service.AuthenticationService;
import dev.chojo.simplepos.service.UserAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Map<String, Object> LOGIN_CLAIM = Map.of("type", "login");
    private static final Map<String, Object> REFRESH_CLAIM = Map.of("type", "refresh");
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final RateLimiter rateLimiter;
    @Value("${security.jwt.refresh-expiration-time}")
    private long jwtRefreshExpiration;

    public AuthController(AuthenticationService authenticationService, JwtService jwtService, RateLimiter rateLimiter) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Logs in. Every attempt is counted by the {@link RateLimiter} before the password is checked and taken back
     * once the password matched — also for a disabled account, whose password was right. Credentials longer than any
     * that can be stored are rejected as wrong before anything is counted or hashed.
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<TokenResponse> login(@RequestParam String username, @RequestParam String password,
                                        HttpServletRequest request) {
        if (username.length() > UserAdminService.MAX_USERNAME_LENGTH
                || password.getBytes(StandardCharsets.UTF_8).length > UserAdminService.MAX_PASSWORD_BYTES) {
            throw new BadCredentialsException("Bad credentials");
        }
        RateLimiter.Attempt attempt = rateLimiter.beginLogin(username, request.getRemoteAddr());
        User authenticatedUser;
        try {
            authenticatedUser = authenticationService.authenticate(username, password);
        } catch (DisabledException e) {
            attempt.succeeded();
            throw e;
        }
        attempt.succeeded();

        TokenResponse tokenResponse = new TokenResponse(
                new TokenDto(jwtService.generateToken(LOGIN_CLAIM, authenticatedUser, jwtService.getExpirationTime()), jwtService.getExpirationTime()),
                new TokenDto(jwtService.generateToken(REFRESH_CLAIM, authenticatedUser, jwtRefreshExpiration), jwtRefreshExpiration)
        );
        authenticationService.saveRefreshToken(new Token(tokenResponse.refresh().token(), authenticatedUser));

        return ResponseEntity.accepted().body(tokenResponse);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<TokenResponse> refresh(@RequestParam String token, HttpServletRequest request) {
        rateLimiter.checkRequest("refresh", request.getRemoteAddr());
        String username = jwtService.extractUsername(token);
        User user = authenticationService.findByUsername(username);
        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }
        if (!jwtService.isTokenValid(token, user, REFRESH_CLAIM)) {
            throw new UnauthorizedException("Invalid token");
        }

        if (!authenticationService.invalidateRefreshToken(user, token)) {
            throw new UnauthorizedException("Unknown or already used token");
        }
        log.info("Valid token. Creating new login and refresh token");

        TokenResponse tokenResponse = new TokenResponse(
                new TokenDto(jwtService.generateToken(LOGIN_CLAIM, user, jwtService.getExpirationTime()), jwtService.getExpirationTime()),
                new TokenDto(jwtService.generateToken(REFRESH_CLAIM, user, jwtRefreshExpiration), jwtRefreshExpiration)
        );
        authenticationService.saveRefreshToken(new Token(tokenResponse.refresh().token(), user));
        return ResponseEntity.accepted().body(tokenResponse);
    }
}
