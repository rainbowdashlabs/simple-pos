package dev.chojo.simplepos.service;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.controller.exceptions.ConflictException;
import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CreateUserRequest;
import dev.chojo.simplepos.entity.dto.UpdateUserRequest;
import dev.chojo.simplepos.repository.TokenRepository;
import dev.chojo.simplepos.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Creates and changes user accounts on behalf of an administrator.
 * <p>
 * There is always at least one enabled ADMIN: a change that would disable or demote the last one is refused. The
 * enabled admins are locked while such a change is checked, so two concurrent changes cannot each remove "another"
 * admin. Disabling a user or setting a new password deletes the user's refresh tokens, so those sessions end at their
 * next refresh at the latest.
 */
@Service
public class UserAdminService {
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_BYTES = 72;
    public static final int MAX_USERNAME_LENGTH = 64;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> list() {
        return userRepository.findAll(Sort.by("username"));
    }

    /**
     * Creates an enabled user.
     *
     * @throws BadRequestException if the username, password or role is missing or invalid
     * @throws ConflictException   if the username is taken, ignoring case
     */
    @Transactional
    public User create(CreateUserRequest request) {
        String username = requireUsername(request.username());
        requirePassword(request.password());
        if (request.role() == null) throw new BadRequestException("Role is missing");
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username %s is already taken".formatted(username));
        }
        return userRepository.save(new User(null, username, passwordEncoder.encode(request.password()), true, request.role()));
    }

    /**
     * Changes the role and/or the enabled state of a user.
     *
     * @throws BadRequestException if the request changes nothing
     * @throws NotFoundException   if the user does not exist
     * @throws ConflictException   if the change would leave no enabled admin
     */
    @Transactional
    public User update(int id, UpdateUserRequest request) {
        if (request.role() == null && request.enabled() == null) throw new BadRequestException("Nothing to change");
        List<User> enabledAdmins = userRepository.lockEnabledByRole(Role.ADMIN);
        User user = require(id);
        Role role = request.role() == null ? user.getRole() : request.role();
        boolean enabled = request.enabled() == null ? user.isEnabled() : request.enabled();
        boolean removesAdmin = user.isEnabled() && user.getRole() == Role.ADMIN && (role != Role.ADMIN || !enabled);
        if (removesAdmin && enabledAdmins.size() <= 1) {
            throw new ConflictException("At least one enabled administrator must remain");
        }
        user.setRole(role);
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        if (!enabled) tokenRepository.deleteAllByUser(saved);
        return saved;
    }

    /**
     * Sets a new password and ends the user's sessions at their next refresh.
     *
     * @throws NotFoundException   if the user does not exist
     * @throws BadRequestException if the password is invalid
     */
    @Transactional
    public User setPassword(int id, String password) {
        requirePassword(password);
        User user = require(id);
        user.setPassword(passwordEncoder.encode(password));
        User saved = userRepository.save(user);
        tokenRepository.deleteAllByUser(saved);
        return saved;
    }

    private User require(int id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    }

    private static String requireUsername(String username) {
        String trimmed = username == null ? "" : username.strip();
        if (trimmed.isEmpty()) throw new BadRequestException("Username is missing");
        if (trimmed.chars().anyMatch(Character::isISOControl)) {
            throw new BadRequestException("Username must not contain control characters");
        }
        if (trimmed.length() > MAX_USERNAME_LENGTH) {
            throw new BadRequestException("Username must be at most %d characters".formatted(MAX_USERNAME_LENGTH));
        }
        return trimmed;
    }

    private static void requirePassword(String password) {
        if (password == null || password.isBlank() || password.length() < MIN_PASSWORD_LENGTH) {
            throw new BadRequestException("Password must be at least %d characters".formatted(MIN_PASSWORD_LENGTH));
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new BadRequestException("Password must be at most %d bytes".formatted(MAX_PASSWORD_BYTES));
        }
    }
}
