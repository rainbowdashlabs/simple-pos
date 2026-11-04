package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.JwtService;
import dev.chojo.simplepos.configuration.security.RateLimiter;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.security.TokenDto;
import dev.chojo.simplepos.entity.security.TokenResponse;
import dev.chojo.simplepos.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true")
public class DemoController {
    private static final Map<String, Object> LOGIN_CLAIM = Map.of("type", "login");
    private static final Map<String, Object> REFRESH_CLAIM = Map.of("type", "refresh");

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final RateLimiter rateLimiter;
    @Value("${security.jwt.refresh-expiration-time}")
    private long jwtRefreshExpiration;

    public DemoController(AuthenticationService authenticationService, JwtService jwtService, RateLimiter rateLimiter) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping
    public ResponseEntity<Map<String, Boolean>> status() {
        return ResponseEntity.ok(Map.of("demo", true));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> demoLogin(HttpServletRequest request) {
        rateLimiter.checkRequest("demo-login", request.getRemoteAddr());
        User user = authenticationService.findByUsername("demo");
        TokenResponse tokenResponse = new TokenResponse(
                new TokenDto(jwtService.generateToken(LOGIN_CLAIM, user, jwtService.getExpirationTime()), jwtService.getExpirationTime()),
                new TokenDto(jwtService.generateToken(REFRESH_CLAIM, user, jwtRefreshExpiration), jwtRefreshExpiration)
        );
        authenticationService.saveRefreshToken(tokenResponse.refresh().token(), user, Duration.ofMillis(jwtRefreshExpiration));
        return ResponseEntity.ok(tokenResponse);
    }
}
