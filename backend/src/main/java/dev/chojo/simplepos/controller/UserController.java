package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.RateLimiter;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.UserPreferences;
import dev.chojo.simplepos.entity.dto.ChangePasswordRequest;
import dev.chojo.simplepos.entity.dto.UserPreferencesDto;
import dev.chojo.simplepos.entity.security.TokenResponse;
import dev.chojo.simplepos.repository.UserPreferencesRepository;
import dev.chojo.simplepos.service.AuthenticationService;
import dev.chojo.simplepos.service.UserAdminService;
import dev.chojo.simplepos.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController()
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserPreferencesRepository preferencesRepository;
    private final AuthenticationService authenticationService;
    private final RateLimiter rateLimiter;

    public UserController(UserService userService, UserPreferencesRepository preferencesRepository,
                          AuthenticationService authenticationService, RateLimiter rateLimiter) {
        this.userService = userService;
        this.preferencesRepository = preferencesRepository;
        this.authenticationService = authenticationService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Changes the caller's own password. The current password is checked like a login, so guessing it is rate limited
     * the same way. Other sessions of the user end; the answer carries the tokens of the caller's new session.
     */
    @PutMapping("/me/password")
    public TokenResponse changePassword(@RequestBody ChangePasswordRequest body, HttpServletRequest request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (body.current() != null && body.current().getBytes(StandardCharsets.UTF_8).length > UserAdminService.MAX_PASSWORD_BYTES) {
            throw AuthenticationService.wrongCurrentPassword();
        }
        RateLimiter.Attempt attempt = rateLimiter.beginLogin(user.getUsername(), request.getRemoteAddr());
        authenticationService.requireCurrentPassword(user, body.current());
        attempt.succeeded();
        return authenticationService.changePassword(user, body.password());
    }

    @GetMapping("me")
    public User me() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/preferences")
    public ResponseEntity<UserPreferencesDto> getPreferences() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserPreferences prefs = preferencesRepository.findByUser(user)
                .orElseGet(() -> preferencesRepository.save(new UserPreferences(user)));
        return ResponseEntity.ok(new UserPreferencesDto(prefs.getLocale(), prefs.getColorTheme(), prefs.getDarkMode()));
    }

    @PutMapping("/preferences")
    public ResponseEntity<UserPreferencesDto> updatePreferences(@RequestBody UserPreferencesDto dto) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserPreferences prefs = preferencesRepository.findByUser(user)
                .orElseGet(() -> new UserPreferences(user));
        prefs.setLocale(dto.locale());
        prefs.setColorTheme(dto.colorTheme());
        prefs.setDarkMode(dto.darkMode());
        preferencesRepository.save(prefs);
        return ResponseEntity.ok(dto);
    }
}
