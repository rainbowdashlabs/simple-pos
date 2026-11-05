package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.RateLimiter;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.security.TokenResponse;
import dev.chojo.simplepos.service.AuthenticationService;
import dev.chojo.simplepos.service.UserAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationService authenticationService;
    private final RateLimiter rateLimiter;

    public AuthController(AuthenticationService authenticationService, RateLimiter rateLimiter) {
        this.authenticationService = authenticationService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Logs in. Every attempt is counted by the {@link RateLimiter} before the password is checked and taken back
     * once the password matched — also for a disabled account, whose password was right. Credentials longer than any
     * that can be stored are rejected as wrong before anything is counted or hashed.
     */
    @PostMapping("/login")
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

        TokenResponse tokenResponse = authenticationService.issueTokens(authenticatedUser);

        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * Ends the session a refresh token belongs to. Answers the same whether or not the token was known, so it says
     * nothing about which tokens exist.
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@RequestParam String token, HttpServletRequest request) {
        rateLimiter.checkRequest("logout", request.getRemoteAddr());
        authenticationService.logout(token);
    }

    /**
     * Trades a refresh token for a new login and refresh token. The refresh token can be used only once.
     */
    @PostMapping("/refresh")
    ResponseEntity<TokenResponse> refresh(@RequestParam String token, HttpServletRequest request) {
        rateLimiter.checkRequest("refresh", request.getRemoteAddr());
        TokenResponse tokens = authenticationService.refresh(token);
        log.info("Valid token. Created new login and refresh token");
        return ResponseEntity.ok(tokens);
    }
}
