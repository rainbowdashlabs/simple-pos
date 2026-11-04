package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;

/**
 * A user as administrators see it.
 */
public record AdminUserDto(Integer id, String username, Role role, boolean enabled) {
    public static AdminUserDto of(User user) {
        return new AdminUserDto(user.getId(), user.getUsername(), user.getRole(), user.isEnabled());
    }
}
