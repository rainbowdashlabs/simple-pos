package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.dto.AdminUserDto;
import dev.chojo.simplepos.entity.dto.CreateUserRequest;
import dev.chojo.simplepos.entity.dto.PasswordRequest;
import dev.chojo.simplepos.entity.dto.UpdateUserRequest;
import dev.chojo.simplepos.service.UserAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User management for administrators. Access is restricted to ADMIN by the security configuration.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserAdminService userAdminService;

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    List<AdminUserDto> list() {
        return userAdminService.list().stream().map(AdminUserDto::of).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdminUserDto create(@RequestBody CreateUserRequest request) {
        return AdminUserDto.of(userAdminService.create(request));
    }

    @PatchMapping("/{id}")
    AdminUserDto update(@PathVariable int id, @RequestBody UpdateUserRequest request) {
        return AdminUserDto.of(userAdminService.update(id, request));
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void setPassword(@PathVariable int id, @RequestBody PasswordRequest request) {
        userAdminService.setPassword(id, request.password());
    }
}
