package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.RateLimiter;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.security.TokenResponse;
import dev.chojo.simplepos.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true")
public class DemoController {

    private final AuthenticationService authenticationService;
    private final RateLimiter rateLimiter;

    public DemoController(AuthenticationService authenticationService, RateLimiter rateLimiter) {
        this.authenticationService = authenticationService;
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
        TokenResponse tokenResponse = authenticationService.issueTokens(user);
        return ResponseEntity.ok(tokenResponse);
    }
}
