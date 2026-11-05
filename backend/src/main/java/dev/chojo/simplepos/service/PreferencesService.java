package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.UserPreferences;
import dev.chojo.simplepos.entity.dto.UserPreferencesDto;
import dev.chojo.simplepos.repository.UserPreferencesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A user's interface preferences. A user who never saved any gets the defaults, without anything being stored.
 */
@Service
public class PreferencesService {
    private final UserPreferencesRepository preferencesRepository;

    public PreferencesService(UserPreferencesRepository preferencesRepository) {
        this.preferencesRepository = preferencesRepository;
    }

    @Transactional(readOnly = true)
    public UserPreferencesDto get(User user) {
        UserPreferences prefs = preferencesRepository.findByUser(user).orElseGet(() -> new UserPreferences(user));
        return new UserPreferencesDto(prefs.getLocale(), prefs.getColorTheme(), prefs.getDarkMode());
    }

    @Transactional
    public UserPreferencesDto save(User user, UserPreferencesDto dto) {
        UserPreferences prefs = preferencesRepository.findByUser(user).orElseGet(() -> new UserPreferences(user));
        prefs.setLocale(dto.locale());
        prefs.setColorTheme(dto.colorTheme());
        prefs.setDarkMode(dto.darkMode());
        preferencesRepository.save(prefs);
        return dto;
    }
}
