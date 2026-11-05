package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.NotNull;

/**
 * A password an administrator sets for a user; its length is checked by the user service.
 */
public record PasswordRequest(@NotNull String password) {
}
