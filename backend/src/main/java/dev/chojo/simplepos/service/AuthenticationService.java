package dev.chojo.simplepos.service;

import dev.chojo.simplepos.configuration.security.LoginTokens;
import dev.chojo.simplepos.configuration.security.Sha256;
import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.security.TokenDto;
import dev.chojo.simplepos.entity.security.TokenResponse;
import dev.chojo.simplepos.exceptions.BadRequestException;
import dev.chojo.simplepos.exceptions.ErrorCode;
import dev.chojo.simplepos.exceptions.UnauthorizedException;
import dev.chojo.simplepos.repository.TokenRepository;
import dev.chojo.simplepos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthenticationService {
    private static final int REFRESH_TOKEN_BYTES = 32;
    private static final String UNUSABLE_REFRESH_TOKEN = "Unknown or already used token";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    private final AuthenticationManager authenticationManager;
    private final LoginTokens loginTokens;
    private final PasswordEncoder passwordEncoder;
    private final long refreshLifetime;

    public AuthenticationService(
            UserRepository userRepository, TokenRepository tokenRepository,
            AuthenticationManager authenticationManager, LoginTokens loginTokens, PasswordEncoder passwordEncoder,
            @Value("${security.jwt.refresh-expiration-time}") long refreshLifetime
    ) {
        this.loginTokens = loginTokens;
        this.passwordEncoder = passwordEncoder;
        this.refreshLifetime = refreshLifetime;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    /**
     * Issues a new login and refresh token for the user and remembers the refresh token. The refresh token is a
     * random value with no meaning of its own: only its stored fingerprint makes it usable.
     */
    public TokenResponse issueTokens(User user) {
        String refresh = newRefreshToken();
        saveRefreshToken(refresh, user, Duration.ofMillis(refreshLifetime));
        return new TokenResponse(
                new TokenDto(loginTokens.issue(user), loginTokens.lifetime().toMillis()),
                new TokenDto(refresh, refreshLifetime));
    }

    private static String newRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Trades a refresh token for a new login and refresh token in one transaction, so the old token is only used up
     * when the new one is stored.
     *
     * @throws UnauthorizedException if the token is unknown, expired or already used
     * @throws DisabledException     if the user is disabled
     */
    @Transactional
    public TokenResponse refresh(String token) {
        return issueTokens(redeemRefreshToken(token));
    }

    /**
     * Uses up a refresh token. Of two concurrent calls with the same token only one succeeds. The token of a disabled
     * user is not used up.
     *
     * @return the user the token belongs to
     * @throws UnauthorizedException if the token is unknown, expired or already used
     * @throws DisabledException     if the user is disabled
     */
    @Transactional
    public User redeemRefreshToken(String token) {
        String fingerprint = fingerprint(token);
        User user = tokenRepository.findUserOfUsableToken(fingerprint, Instant.now())
                .orElseThrow(() -> new UnauthorizedException(UNUSABLE_REFRESH_TOKEN));
        if (!user.isEnabled()) throw new DisabledException("Account is disabled");
        if (tokenRepository.deleteByFingerprint(fingerprint) != 1) throw new UnauthorizedException(UNUSABLE_REFRESH_TOKEN);
        return user;
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
        BadRequestException error = new BadRequestException(ErrorCode.CURRENT_PASSWORD_WRONG);
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
        return Sha256.hex(token);
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
}
