package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.controller.exceptions.UnauthorizedException;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
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
    void shouldAuthenticateValidUser() {
        User user = createUser("authuser", "secret");

        User authenticated = authenticationService.authenticate("authuser", "secret");
        assertNotNull(authenticated);
        assertEquals("authuser", authenticated.getUsername());
    }

    @Test
    void shouldRejectInvalidPassword() {
        User user = createUser("authuser2", "correct");

        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate("authuser2", "wrong");
        });
    }

    @Test
    void shouldSaveRefreshToken() {
        User user = createUser("tokenuser", "pass");

        authenticationService.saveRefreshToken(new Token("refresh_token_123", user));

        Optional<Token> found = tokenRepository.findByUserAndToken(user, "refresh_token_123");
        assertTrue(found.isPresent());
    }

    @Test
    void shouldInvalidateRefreshTokenOnlyOnce() {
        User user = createUser("invalidateuser", "pass");
        authenticationService.saveRefreshToken(new Token("to_invalidate", user));

        assertTrue(authenticationService.invalidateRefreshToken(user, "to_invalidate"));
        assertFalse(authenticationService.invalidateRefreshToken(user, "to_invalidate"));
        assertTrue(tokenRepository.findByUserAndToken(user, "to_invalidate").isEmpty());
    }

    @Test
    void shouldNotInvalidateUnknownRefreshToken() {
        User user = createUser("notokenuser", "pass");

        assertFalse(authenticationService.invalidateRefreshToken(user, "nonexistent"));
    }

    @Test
    void shouldFindByUsername() {
        createUser("findable", "pass");

        User found = authenticationService.findByUsername("findable");
        assertEquals("findable", found.getUsername());
    }

    @Test
    void shouldThrowWhenFindingNonExistentUser() {
        assertThrows(UnauthorizedException.class, () -> authenticationService.findByUsername("ghost"));
    }

    private User createUser(String username, String password) {
        return userRepository.save(new User(null, username, passwordEncoder.encode(password), true, Role.STAFF));
    }
}
