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

import java.time.Duration;
import java.time.Instant;
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

        authenticationService.saveRefreshToken("refresh_token_123", user, Duration.ofDays(1));

        assertTrue(tokenRepository.findByToken("refresh_token_123").isEmpty());
        Optional<Token> found = tokenRepository.findByUserAndToken(user, AuthenticationService.fingerprint("refresh_token_123"));
        assertTrue(found.isPresent());
        Instant expected = Instant.now().plus(Duration.ofDays(1));
        assertTrue(Duration.between(found.get().getExpires(), expected).abs().toSeconds() < 60);
    }

    @Test
    void shouldInvalidateRefreshTokenOnlyOnce() {
        User user = createUser("invalidateuser", "pass");
        authenticationService.saveRefreshToken("to_invalidate", user, Duration.ofDays(1));

        assertTrue(authenticationService.invalidateRefreshToken(user, "to_invalidate"));
        assertFalse(authenticationService.invalidateRefreshToken(user, "to_invalidate"));
        assertTrue(tokenRepository.findByUserAndToken(user, AuthenticationService.fingerprint("to_invalidate")).isEmpty());
    }

    @Test
    void shouldForgetATokenOnLogout() {
        User user = createUser("logoutuser", "pass");
        authenticationService.saveRefreshToken("to_forget", user, Duration.ofDays(1));

        assertTrue(authenticationService.logout("to_forget"));
        assertFalse(authenticationService.logout("to_forget"));
        assertFalse(authenticationService.invalidateRefreshToken(user, "to_forget"));
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
