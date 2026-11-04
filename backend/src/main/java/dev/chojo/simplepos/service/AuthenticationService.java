package dev.chojo.simplepos.service;

import dev.chojo.simplepos.controller.exceptions.UnauthorizedException;
import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.TokenRepository;
import dev.chojo.simplepos.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

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

    public AuthenticationService(
            UserRepository userRepository, TokenRepository tokenRepository,
            AuthenticationManager authenticationManager
    ) {
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
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
