package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.controller.exceptions.ConflictException;
import dev.chojo.simplepos.controller.exceptions.UnauthorizedException;
import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.UserDto;
import dev.chojo.simplepos.repository.TokenRepository;
import dev.chojo.simplepos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthenticationServiceTest {
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void shouldSignupUser() {
        User user = authenticationService.signup(new UserDto("newuser", "password123"));

        assertNotNull(user.getId());
        assertEquals("newuser", user.getUsername());
        assertTrue(passwordEncoder.matches("password123", user.getPassword()));
    }

    @Test
    void signedUpUserIsPersisted() {
        authenticationService.signup(new UserDto("persisted", "pass"));

        Optional<User> found = userRepository.findByUsername("persisted");
        assertTrue(found.isPresent());
    }

    @Test
    void signedUpUserIsDisabledByDefault() {
        User user = authenticationService.signup(new UserDto("newbie", "pass"));
        assertFalse(user.isEnabled());
    }

    @Test
    void shouldAuthenticateValidUser() {
        User user = authenticationService.signup(new UserDto("authuser", "secret"));
        user.setEnabled(true);
        userRepository.save(user);

        User authenticated = authenticationService.authenticate("authuser", "secret");
        assertNotNull(authenticated);
        assertEquals("authuser", authenticated.getUsername());
    }

    @Test
    void shouldRejectInvalidPassword() {
        User user = authenticationService.signup(new UserDto("authuser2", "correct"));
        user.setEnabled(true);
        userRepository.save(user);

        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate("authuser2", "wrong");
        });
    }

    @Test
    void shouldSaveRefreshToken() {
        User user = authenticationService.signup(new UserDto("tokenuser", "pass"));

        authenticationService.saveRefreshToken(new Token("refresh_token_123", user));

        Optional<Token> found = tokenRepository.findByUserAndToken(user, "refresh_token_123");
        assertTrue(found.isPresent());
    }

    @Test
    void shouldInvalidateRefreshTokenOnlyOnce() {
        User user = authenticationService.signup(new UserDto("invalidateuser", "pass"));
        authenticationService.saveRefreshToken(new Token("to_invalidate", user));

        assertTrue(authenticationService.invalidateRefreshToken(user, "to_invalidate"));
        assertFalse(authenticationService.invalidateRefreshToken(user, "to_invalidate"));
        assertTrue(tokenRepository.findByUserAndToken(user, "to_invalidate").isEmpty());
    }

    @Test
    void shouldNotInvalidateUnknownRefreshToken() {
        User user = authenticationService.signup(new UserDto("notokenuser", "pass"));

        assertFalse(authenticationService.invalidateRefreshToken(user, "nonexistent"));
    }

    @Test
    void shouldRejectDuplicateSignup() {
        authenticationService.signup(new UserDto("duplicate", "pass"));

        assertThrows(ConflictException.class, () -> authenticationService.signup(new UserDto("duplicate", "other")));
    }

    @Test
    void shouldFindByUsername() {
        authenticationService.signup(new UserDto("findable", "pass"));

        User found = authenticationService.findByUsername("findable");
        assertEquals("findable", found.getUsername());
    }

    @Test
    void shouldThrowWhenFindingNonExistentUser() {
        assertThrows(UnauthorizedException.class, () -> authenticationService.findByUsername("ghost"));
    }
}
