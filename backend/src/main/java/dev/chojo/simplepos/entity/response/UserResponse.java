package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;

public record UserResponse(Integer id, String username, Role role) {
    public static UserResponse of(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
