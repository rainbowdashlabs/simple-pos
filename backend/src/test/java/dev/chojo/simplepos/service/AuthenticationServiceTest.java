package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.exceptions.UnauthorizedException;
import dev.chojo.simplepos.repository.TokenRepository;
import dev.chojo.simplepos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
    void shouldRedeemARefreshTokenOnlyOnce() {
        User user = createUser("invalidateuser", "pass");
        authenticationService.saveRefreshToken("to_invalidate", user, Duration.ofDays(1));

        assertEquals(user.getId(), authenticationService.redeemRefreshToken("to_invalidate").getId());
        assertThrows(UnauthorizedException.class, () -> authenticationService.redeemRefreshToken("to_invalidate"));
        assertTrue(tokenRepository.findByUserAndToken(user, AuthenticationService.fingerprint("to_invalidate")).isEmpty());
    }

    @Test
    void shouldRefuseAnExpiredRefreshToken() {
        User user = createUser("expireduser", "pass");
        authenticationService.saveRefreshToken("expired", user, Duration.ofSeconds(-1));

        assertThrows(UnauthorizedException.class, () -> authenticationService.redeemRefreshToken("expired"));
    }

    @Test
    void shouldKeepTheRefreshTokenOfADisabledUser() {
        User user = createUser("disabledrefresh", "pass");
        authenticationService.saveRefreshToken("kept", user, Duration.ofDays(1));
        user.setEnabled(false);
        userRepository.saveAndFlush(user);

        assertThrows(DisabledException.class, () -> authenticationService.redeemRefreshToken("kept"));
        assertTrue(tokenRepository.findByUserAndToken(user, AuthenticationService.fingerprint("kept")).isPresent());
    }

    @Test
    void shouldIssueRandomRefreshTokens() {
        User user = createUser("randomuser", "pass");

        String first = authenticationService.issueTokens(user).refresh().token();
        String second = authenticationService.issueTokens(user).refresh().token();

        assertNotEquals(first, second);
        assertEquals(43, first.length());
        assertEquals(user.getId(), authenticationService.redeemRefreshToken(first).getId());
    }

    @Test
    void shouldForgetATokenOnLogout() {
        User user = createUser("logoutuser", "pass");
        authenticationService.saveRefreshToken("to_forget", user, Duration.ofDays(1));

        assertTrue(authenticationService.logout("to_forget"));
        assertFalse(authenticationService.logout("to_forget"));
        assertThrows(UnauthorizedException.class, () -> authenticationService.redeemRefreshToken("to_forget"));
    }

    @Test
    void shouldNotRedeemUnknownRefreshToken() {
        assertThrows(UnauthorizedException.class, () -> authenticationService.redeemRefreshToken("nonexistent"));
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
