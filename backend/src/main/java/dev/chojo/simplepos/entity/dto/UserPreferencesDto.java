package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * A user's interface preferences, limited to the languages, colour themes and modes the frontend offers.
 */
public record UserPreferencesDto(@NotNull @Pattern(regexp = "de|en") String locale,
                                 @NotNull @Pattern(regexp = "blue|red|green|pink") String colorTheme,
                                 @NotNull @Pattern(regexp = "dark|light") String darkMode) {
}
