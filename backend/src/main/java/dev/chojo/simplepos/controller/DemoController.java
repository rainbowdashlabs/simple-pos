package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.DemoDataSeeder;
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

    /**
     * Signs in as the demo user, who works the till like staff.
     */
    @PostMapping("/login")
    public ResponseEntity<SessionResponse> demoLogin(HttpServletRequest request) {
        return logIn(DemoDataSeeder.DEMO_USER, request);
    }

    /**
     * Signs in as the demo administrator, to try what only administrators can do.
     */
    @PostMapping("/login/admin")
    public ResponseEntity<SessionResponse> demoAdminLogin(HttpServletRequest request) {
        return logIn(DemoDataSeeder.DEMO_ADMIN, request);
    }

    private ResponseEntity<SessionResponse> logIn(String username, HttpServletRequest request) {
        rateLimiter.checkRequest("demo-login", request.getRemoteAddr());
        User user = authenticationService.findByUsername(username);
        return refreshCookie.respond(authenticationService.issueTokens(user), user.getUsername());
    }
}
