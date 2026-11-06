package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.RateLimiter;
import dev.chojo.simplepos.configuration.security.RefreshCookie;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.ChangePasswordRequest;
import dev.chojo.simplepos.entity.dto.UserPreferencesDto;
import dev.chojo.simplepos.entity.response.UserResponse;
import dev.chojo.simplepos.entity.security.SessionResponse;
import dev.chojo.simplepos.service.AuthenticationService;
import dev.chojo.simplepos.service.PreferencesService;
import dev.chojo.simplepos.service.UserAdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController()
@RequestMapping("/api/user")
public class UserController {

    private final PreferencesService preferencesService;
    private final AuthenticationService authenticationService;
    private final RateLimiter rateLimiter;
    private final RefreshCookie refreshCookie;

    public UserController(PreferencesService preferencesService,
                          AuthenticationService authenticationService, RateLimiter rateLimiter,
                          RefreshCookie refreshCookie) {
        this.preferencesService = preferencesService;
        this.authenticationService = authenticationService;
        this.rateLimiter = rateLimiter;
        this.refreshCookie = refreshCookie;
    }

    /**
     * Changes the caller's own password. The current password is checked like a login, so guessing it is rate limited
     * the same way. Other sessions of the user end; the answer starts the caller's new session like a login.
     */
    @PutMapping("/me/password")
    public ResponseEntity<SessionResponse> changePassword(@Valid @RequestBody ChangePasswordRequest body,
                                                          HttpServletRequest request, @CurrentUser User user) {
        if (body.current().getBytes(StandardCharsets.UTF_8).length > UserAdminService.MAX_PASSWORD_BYTES) {
            throw AuthenticationService.wrongCurrentPassword();
        }
        RateLimiter.Attempt attempt = rateLimiter.beginLogin(user.getUsername(), request.getRemoteAddr());
        authenticationService.requireCurrentPassword(user, body.current());
        attempt.succeeded();
        return refreshCookie.respond(authenticationService.changePassword(user, body.password()), user.getUsername());
    }

    @GetMapping("me")
    public UserResponse me(@CurrentUser User user) {
        return UserResponse.of(user);
    }

    @GetMapping("/preferences")
    public UserPreferencesDto getPreferences(@CurrentUser User user) {
        return preferencesService.get(user);
    }

    @PutMapping("/preferences")
    public UserPreferencesDto updatePreferences(@Valid @RequestBody UserPreferencesDto dto,
                                                @CurrentUser User user) {
        return preferencesService.save(user, dto);
    }
}
