package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Role;

/**
 * Changes to a user. Fields left null stay as they are.
 */
public record UpdateUserRequest(Role role, Boolean enabled) {
}
