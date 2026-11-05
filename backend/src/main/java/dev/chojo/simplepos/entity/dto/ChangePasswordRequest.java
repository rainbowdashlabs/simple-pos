package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.NotNull;

/**
 * A user's own password change: the password in use now and the new one.
 */
public record ChangePasswordRequest(@NotNull String current, @NotNull String password) {
}
