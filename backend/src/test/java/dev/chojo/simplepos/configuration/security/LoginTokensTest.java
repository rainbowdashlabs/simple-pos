package dev.chojo.simplepos.configuration.security;

import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginTokensTest {
    private static final String KEY = "k".repeat(LoginTokens.MIN_KEY_BYTES);
    private static final User USER = new User(1, "user", "x", true, Role.STAFF);

    @Test
    void shouldRefuseAMissingOrShortSecretKey() {
        assertThrows(IllegalStateException.class, () -> configured(""));
        assertThrows(IllegalStateException.class, () -> configured("too-short"));
        assertDoesNotThrow(() -> configured("x".repeat(LoginTokens.MIN_KEY_BYTES)));
    }

    @Test
    void shouldAcceptItsOwnLoginTokens() {
        LoginTokens tokens = configured(KEY);

        assertEquals("user", tokens.decoder().decode(tokens.issue(USER)).getSubject());
    }

    @Test
    void shouldRejectTokensOfAnotherIssuerAudienceOrKey() {
        LoginTokens tokens = configured(KEY);

        assertRejected(tokens, new LoginTokens(KEY, "other", "simple-pos", 60_000).issue(USER));
        assertRejected(tokens, new LoginTokens(KEY, "simple-pos", "other", 60_000).issue(USER));
        assertRejected(tokens, configured("o".repeat(LoginTokens.MIN_KEY_BYTES)).issue(USER));
        assertRejected(tokens, "garbage");
    }

    @Test
    void shouldRejectExpiredTokensWithoutGrace() {
        assertRejected(configured(KEY), signed(claims -> claims.expiresAt(Instant.now().minusSeconds(5))));
    }

    @Test
    void shouldRejectTokensWithoutExpiry() {
        assertRejected(configured(KEY), signed(claims -> claims.claims(all -> all.remove("exp"))));
    }

    @Test
    void shouldRejectOtherAlgorithmsWithTheSameKey() {
        String longKey = "l".repeat(64);

        assertRejected(configured(longKey), TestTokens.signed(longKey, MacAlgorithm.HS512, claims -> {
        }));
    }

    @Test
    void shouldRejectTokensThatAreNotLoginTokens() {
        LoginTokens tokens = configured(KEY);

        assertRejected(tokens, signed(claims -> claims.claim(LoginTokens.TYPE_CLAIM, "refresh")));
        assertRejected(tokens, signed(claims -> claims.claim(LoginTokens.TYPE_CLAIM, 1)));
        assertRejected(tokens, signed(claims -> claims.claims(all -> all.remove(LoginTokens.TYPE_CLAIM))));
    }

    @Test
    void shouldRejectTokensWithoutIssuerAndAudience() {
        assertRejected(configured(KEY), signed(claims -> claims.claims(all -> {
            all.remove("iss");
            all.remove("aud");
        })));
    }

    private static void assertRejected(LoginTokens tokens, String token) {
        assertThrows(JwtException.class, () -> tokens.decoder().decode(token));
    }

    private static LoginTokens configured(String key) {
        return new LoginTokens(key, "simple-pos", "simple-pos", 60_000);
    }

    private static String signed(Consumer<JwtClaimsSet.Builder> change) {
        return TestTokens.signed(KEY, MacAlgorithm.HS256, change);
    }
}
