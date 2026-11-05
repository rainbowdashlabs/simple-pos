package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A new user. Length and content of the name and password are checked by the user service, which trims the name and
 * counts the password in bytes.
 */
public record CreateUserRequest(@NotBlank String username, @NotNull String password, @NotNull Role role) {
}
