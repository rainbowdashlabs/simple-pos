package dev.chojo.simplepos.configuration.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {
    private static final String KEY = "k".repeat(JwtService.MIN_KEY_BYTES);
    private static final UserDetails USER = User.withUsername("user").password("x").build();

    @Test
    void shouldRefuseMissingSecretKey() {
        assertThrows(IllegalStateException.class, () -> withKey("").requireSecretKey());
    }

    @Test
    void shouldRefuseShortSecretKey() {
        assertThrows(IllegalStateException.class, () -> withKey("too-short").requireSecretKey());
    }

    @Test
    void shouldAcceptLongEnoughSecretKey() {
        assertDoesNotThrow(() -> withKey("x".repeat(JwtService.MIN_KEY_BYTES)).requireSecretKey());
    }

    @Test
    void tokensCarryAndRequireTheOwnIssuerAndAudience() {
        JwtService service = configured("simple-pos", "simple-pos");
        String token = service.generateToken(USER);

        assertEquals("simple-pos", service.extractClaim(token, Claims::getIssuer));
        assertTrue(service.extractClaim(token, Claims::getAudience).contains("simple-pos"));
        assertThrows(JwtException.class, () -> configured("other", "simple-pos").extractUsername(token));
        assertThrows(JwtException.class, () -> configured("simple-pos", "other").extractUsername(token));
    }

    @Test
    void tokensWithoutIssuerAndAudienceAreStillAccepted() {
        JwtService service = configured("simple-pos", "simple-pos");
        String legacy = Jwts.builder()
                .subject("user")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertEquals("user", service.extractUsername(legacy));
    }

    private static JwtService configured(String issuer, String audience) {
        JwtService service = withKey(KEY);
        ReflectionTestUtils.setField(service, "issuer", issuer);
        ReflectionTestUtils.setField(service, "audience", audience);
        ReflectionTestUtils.setField(service, "jwtExpiration", 60_000L);
        return service;
    }

    private static JwtService withKey(String key) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", key);
        return service;
    }
}
