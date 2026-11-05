package dev.chojo.simplepos.service;

import dev.chojo.simplepos.configuration.security.JwtService;
import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.controller.exceptions.UnauthorizedException;
import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.security.TokenDto;
import dev.chojo.simplepos.entity.security.TokenResponse;
import dev.chojo.simplepos.repository.TokenRepository;
import dev.chojo.simplepos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final long refreshLifetime;

    public AuthenticationService(
            UserRepository userRepository, TokenRepository tokenRepository,
            AuthenticationManager authenticationManager, JwtService jwtService, PasswordEncoder passwordEncoder,
            @Value("${security.jwt.refresh-expiration-time}") long refreshLifetime
    ) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshLifetime = refreshLifetime;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    /**
     * Issues a new login and refresh token for the user and remembers the refresh token.
     */
    public TokenResponse issueTokens(User user) {
        TokenResponse tokens = new TokenResponse(
                new TokenDto(jwtService.generateToken(JwtService.LOGIN_CLAIM, user, jwtService.getExpirationTime()), jwtService.getExpirationTime()),
                new TokenDto(jwtService.generateToken(JwtService.REFRESH_CLAIM, user, refreshLifetime), refreshLifetime));
        saveRefreshToken(tokens.refresh().token(), user, Duration.ofMillis(refreshLifetime));
        return tokens;
    }

    /**
     * Checks the password a user gave as their current one.
     *
     * @throws BadRequestException if it is not the user's password
     */
    public void requireCurrentPassword(User user, String current) {
        User stored = findByUsername(user.getUsername());
        if (current == null || !passwordEncoder.matches(current, stored.getPassword())) {
            throw wrongCurrentPassword();
        }
    }

    /**
     * The answer to a wrong current password: 400 with the property {@code field} set to {@code current}, so the
     * form can show it at the field.
     */
    public static BadRequestException wrongCurrentPassword() {
        BadRequestException error = new BadRequestException("The current password is wrong");
        error.getBody().setProperty("field", "current");
        return error;
    }

    /**
     * Sets a user's own new password and ends every session of the user, then starts a new one for the caller, so
     * other devices are logged out at their next refresh while this one stays logged in.
     *
     * @return the tokens of the new session
     * @throws BadRequestException if the new password is invalid
     */
    @Transactional
    public TokenResponse changePassword(User user, String password) {
        UserAdminService.requirePassword(password);
        User stored = findByUsername(user.getUsername());
        stored.setPassword(passwordEncoder.encode(password));
        User saved = userRepository.save(stored);
        tokenRepository.deleteAllByUser(saved);
        return issueTokens(saved);
    }

    /**
     * Remembers a refresh token as usable until {@code lifetime} from now. Only its fingerprint is stored.
     */
    public void saveRefreshToken(String token, User user, Duration lifetime) {
        tokenRepository.save(new Token(fingerprint(token), user, Instant.now().plus(lifetime)));
    }

    /**
     * Forgets a refresh token, ending the session it belongs to at its next refresh.
     *
     * @return whether the token was known
     */
    @Transactional
    public boolean logout(String token) {
        return tokenRepository.deleteByFingerprint(fingerprint(token)) > 0;
    }

    /**
     * What is stored for a refresh token: its SHA-256, hex encoded.
     */
    public static String fingerprint(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available", e);
        }
    }

    /**
     * Loads the user with this name.
     *
     * @throws UnauthorizedException if no user with this name exists
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UnauthorizedException("Unknown user"));
    }

    public User authenticate(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        return findByUsername(username);
    }

    @Transactional
    public boolean invalidateRefreshToken(User user, String token) {
        return tokenRepository.deleteByUserAndToken(user, fingerprint(token)) == 1;
    }

}
