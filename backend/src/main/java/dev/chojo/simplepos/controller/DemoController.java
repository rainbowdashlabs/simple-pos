package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.RateLimiter;
import dev.chojo.simplepos.configuration.security.RefreshCookie;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.security.SessionResponse;
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
    private final RefreshCookie refreshCookie;

    public DemoController(AuthenticationService authenticationService, RateLimiter rateLimiter, RefreshCookie refreshCookie) {
        this.authenticationService = authenticationService;
        this.rateLimiter = rateLimiter;
        this.refreshCookie = refreshCookie;
    }

    @GetMapping
    public ResponseEntity<Map<String, Boolean>> status() {
        return ResponseEntity.ok(Map.of("demo", true));
    }

    @PostMapping("/login")
    public ResponseEntity<SessionResponse> demoLogin(HttpServletRequest request) {
        rateLimiter.checkRequest("demo-login", request.getRemoteAddr());
        User user = authenticationService.findByUsername("demo");
        return refreshCookie.respond(authenticationService.issueTokens(user), user.getUsername());
    }
}
