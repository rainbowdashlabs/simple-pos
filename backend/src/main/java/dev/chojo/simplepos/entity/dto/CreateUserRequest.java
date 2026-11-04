package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Role;

public record CreateUserRequest(String username, String password, Role role) {
}
