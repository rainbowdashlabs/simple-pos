package dev.chojo.simplepos.configuration.security;

import dev.chojo.simplepos.exceptions.TooManyRequestsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateLimiterTest {
    private final MutableClock clock = new MutableClock();
    private final RateLimiter limiter = new RateLimiter(clock);

    @Test
    void shouldRefuseAfterTooManyFailuresFromOneAddress() {
        fail("alice", "10.0.0.1", RateLimiter.LOGIN_ATTEMPTS);

        TooManyRequestsException refused = assertThrows(TooManyRequestsException.class,
                () -> limiter.beginLogin("alice", "10.0.0.1"));
        assertEquals(String.valueOf(RateLimiter.LOGIN_WINDOW.toSeconds()),
                refused.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
        assertDoesNotThrow(() -> limiter.beginLogin("alice", "10.0.0.2"));
        assertDoesNotThrow(() -> limiter.beginLogin("bob", "10.0.0.1"));
    }

    @Test
    void shouldIgnoreCaseInTheUsername() {
        fail("Alice", "10.0.0.1", RateLimiter.LOGIN_ATTEMPTS);

        assertThrows(TooManyRequestsException.class, () -> limiter.beginLogin("alice", "10.0.0.1"));
    }

    @Test
    void shouldRefuseAnAccountGuessedFromManyAddresses() {
        for (int i = 0; i < RateLimiter.ACCOUNT_ATTEMPTS; i++) {
            limiter.beginLogin("alice", "10.0.1." + i);
        }

        assertThrows(TooManyRequestsException.class, () -> limiter.beginLogin("alice", "10.0.2.1"));
    }

    @Test
    void shouldRefuseAnAddressTryingManyUsernames() {
        for (int i = 0; i < RateLimiter.ADDRESS_ATTEMPTS; i++) {
            limiter.beginLogin("user" + i, "10.0.0.1");
        }

        assertThrows(TooManyRequestsException.class, () -> limiter.beginLogin("someone-else", "10.0.0.1"));
    }

    @Test
    void shouldCountAttemptsBeforeTheyFinishSoABurstCannotSlipThrough() {
        for (int i = 0; i < RateLimiter.LOGIN_ATTEMPTS; i++) {
            limiter.beginLogin("alice", "10.0.0.1");
        }

        assertThrows(TooManyRequestsException.class, () -> limiter.beginLogin("alice", "10.0.0.1"));
    }

    @Test
    void shouldTakeBackASuccessfulAttempt() {
        fail("alice", "10.0.0.1", RateLimiter.LOGIN_ATTEMPTS - 1);
        limiter.beginLogin("alice", "10.0.0.1").succeeded();
        fail("alice", "10.0.0.1", RateLimiter.LOGIN_ATTEMPTS - 1);

        assertDoesNotThrow(() -> limiter.beginLogin("alice", "10.0.0.1"));
    }

    @Test
    void shouldForgiveFailuresOnceTheWindowHasPassed() {
        fail("alice", "10.0.0.1", RateLimiter.LOGIN_ATTEMPTS);
        clock.advance(RateLimiter.LOGIN_WINDOW);

        assertDoesNotThrow(() -> limiter.beginLogin("alice", "10.0.0.1"));
    }

    @Test
    void shouldReportWhenTheOldestCountingFailureExpires() {
        limiter.beginLogin("alice", "10.0.0.1");
        clock.advance(Duration.ofMinutes(5));
        fail("alice", "10.0.0.1", RateLimiter.LOGIN_ATTEMPTS - 1);

        TooManyRequestsException refused = assertThrows(TooManyRequestsException.class,
                () -> limiter.beginLogin("alice", "10.0.0.1"));
        assertEquals(String.valueOf(Duration.ofMinutes(10).toSeconds()),
                refused.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void shouldLimitRequestsPerAddressAndMinute() {
        for (int i = 0; i < RateLimiter.REQUESTS_PER_MINUTE; i++) {
            limiter.checkRequest("refresh", "10.0.0.1");
        }

        assertThrows(TooManyRequestsException.class, () -> limiter.checkRequest("refresh", "10.0.0.1"));
        assertDoesNotThrow(() -> limiter.checkRequest("refresh", "10.0.0.2"));
        clock.advance(RateLimiter.REQUEST_WINDOW);
        assertDoesNotThrow(() -> limiter.checkRequest("refresh", "10.0.0.1"));
    }

    @Test
    void shouldForgetExpiredCounts() {
        fail("alice", "10.0.0.1", 1);
        assertEquals(3, limiter.size());
        clock.advance(RateLimiter.LOGIN_WINDOW.plusSeconds(1));

        limiter.forgetExpired();

        assertEquals(0, limiter.size());
    }

    @Test
    void shouldKeepTheSameSizeForAHugeUsername() {
        limiter.beginLogin("x".repeat(2_000_000), "10.0.0.1");
        limiter.beginLogin("x".repeat(2_000_000), "10.0.0.1");

        assertEquals(3, limiter.size());
    }

    @Test
    void shouldCountIpv6ClientsByTheirNetwork() {
        for (int i = 0; i < RateLimiter.LOGIN_ATTEMPTS; i++) {
            limiter.beginLogin("alice", "2001:db8:1:2::" + Integer.toHexString(i + 1));
        }

        assertThrows(TooManyRequestsException.class, () -> limiter.beginLogin("alice", "2001:db8:1:2:ffff::1"));
        assertDoesNotThrow(() -> limiter.beginLogin("alice", "2001:db8:1:3::1"));
    }

    @Test
    void shouldRefuseNewClientsWhileTheMapIsFull() {
        RateLimiter small = new RateLimiter(clock, 3);
        small.beginLogin("alice", "10.0.0.1");

        assertThrows(TooManyRequestsException.class, () -> small.beginLogin("bob", "10.0.0.2"));
        clock.advance(RateLimiter.LOGIN_WINDOW.plus(RateLimiter.FULL_SWEEP_INTERVAL));
        assertDoesNotThrow(() -> small.beginLogin("bob", "10.0.0.2"));
    }

    private void fail(String username, String address, int times) {
        for (int i = 0; i < times; i++) {
            limiter.beginLogin(username, address);
        }
    }

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-11-04T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
