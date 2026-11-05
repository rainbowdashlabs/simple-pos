package dev.chojo.simplepos.entity.dto;

/**
 * A user's own password change: the password in use now and the new one.
 */
public record ChangePasswordRequest(String current, String password) {
}
