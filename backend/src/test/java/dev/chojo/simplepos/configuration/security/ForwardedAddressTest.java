package dev.chojo.simplepos.configuration.security;

import dev.chojo.simplepos.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The client address behind a reverse proxy, on a real server: Tomcat takes it from {@code X-Forwarded-For} when the
 * request comes from an internal proxy, here the loopback address the test connects from.
 * <p>
 * Runs its own web server and so its own Spring context.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ForwardedAddressTest {
    @LocalServerPort
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void shouldLimitByTheForwardedAddress() throws Exception {
        for (int i = 0; i < RateLimiter.LOGIN_ATTEMPTS; i++) {
            assertEquals(401, login("203.0.113.7"));
        }

        assertEquals(429, login("203.0.113.7"));
        assertEquals(401, login("198.51.100.9"));
    }

    private int login(String forwardedFor) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/auth/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Forwarded-For", forwardedFor)
                .POST(HttpRequest.BodyPublishers.ofString("username=forwarded-ghost&password=wrong"))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
