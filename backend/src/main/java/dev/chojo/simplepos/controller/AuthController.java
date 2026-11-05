package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.RateLimiter;
import dev.chojo.simplepos.configuration.security.RefreshCookie;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.security.SessionResponse;
import dev.chojo.simplepos.entity.security.TokenResponse;
import dev.chojo.simplepos.exceptions.UnauthorizedException;
import dev.chojo.simplepos.service.AuthenticationService;
import dev.chojo.simplepos.service.UserAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationService authenticationService;
    private final RateLimiter rateLimiter;
    private final RefreshCookie refreshCookie;

    public AuthController(AuthenticationService authenticationService, RateLimiter rateLimiter, RefreshCookie refreshCookie) {
        this.authenticationService = authenticationService;
        this.rateLimiter = rateLimiter;
        this.refreshCookie = refreshCookie;
    }

    /**
     * Logs in. Every attempt is counted by the {@link RateLimiter} before the password is checked and taken back
     * once the password matched — also for a disabled account, whose password was right. Credentials longer than any
     * that can be stored are rejected as wrong before anything is counted or hashed.
     */
    @PostMapping("/login")
    ResponseEntity<SessionResponse> login(@RequestParam String username, @RequestParam String password,
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

        TokenResponse tokens = authenticationService.issueTokens(authenticatedUser);
        return refreshCookie.respond(tokens, authenticatedUser.getUsername());
    }

    /**
     * Ends the session of the refresh cookie and makes the browser forget it. Answers the same whether or not the
     * token was known, so it says nothing about which tokens exist.
     */
    @PostMapping("/logout")
    ResponseEntity<Void> logout(
            @CookieValue(name = RefreshCookie.NAME, required = false) String token,
            @RequestHeader(name = RefreshCookie.CSRF_HEADER, required = false) String requestedWith,
            HttpServletRequest request) {
        RefreshCookie.requireOwnFrontend(requestedWith);
        rateLimiter.checkRequest("logout", request.getRemoteAddr());
        if (token != null) authenticationService.logout(token);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, refreshCookie.cleared()).build();
    }

    /**
     * Trades the refresh cookie for a new login token and a new refresh cookie. A refresh token can be used only
     * once; one that is refused is also removed from the browser, so new tabs do not send it again.
     */
    @PostMapping("/refresh")
    ResponseEntity<SessionResponse> refresh(
            @CookieValue(name = RefreshCookie.NAME, required = false) String token,
            @RequestHeader(name = RefreshCookie.CSRF_HEADER, required = false) String requestedWith,
            HttpServletRequest request) {
        RefreshCookie.requireOwnFrontend(requestedWith);
        rateLimiter.checkRequest("refresh", request.getRemoteAddr());
        if (token == null) throw new UnauthorizedException("No session");
        AuthenticationService.Session session;
        try {
            session = authenticationService.refresh(token);
        } catch (UnauthorizedException e) {
            e.getHeaders().add(HttpHeaders.SET_COOKIE, refreshCookie.cleared());
            throw e;
        }
        log.info("Valid token. Created new login and refresh token");
        return refreshCookie.respond(session.tokens(), session.user().getUsername());
    }
}
