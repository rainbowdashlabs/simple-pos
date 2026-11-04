package dev.chojo.simplepos.configuration.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {
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

    private static JwtService withKey(String key) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", key);
        return service;
    }
}
