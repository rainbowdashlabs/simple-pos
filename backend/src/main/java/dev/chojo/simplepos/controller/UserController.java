package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.UserPreferences;
import dev.chojo.simplepos.entity.dto.UserPreferencesDto;
import dev.chojo.simplepos.repository.UserPreferencesRepository;
import dev.chojo.simplepos.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserPreferencesRepository preferencesRepository;

    public UserController(UserService userService, UserPreferencesRepository preferencesRepository) {
        this.userService = userService;
        this.preferencesRepository = preferencesRepository;
    }

    @GetMapping("me")
    public User me() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String admin() {
        return "U admin UwU";
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
