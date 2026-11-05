package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Role;
import jakarta.validation.constraints.AssertTrue;

/**
 * Changes to a user. Fields left null stay as they are.
 */
public record UpdateUserRequest(Role role, Boolean enabled) {
    /**
     * At least one field is given.
     */
    @AssertTrue(message = "{simplepos.validation.something_to_change}")
    public boolean isChanging() {
        return role != null || enabled != null;
    }
}
