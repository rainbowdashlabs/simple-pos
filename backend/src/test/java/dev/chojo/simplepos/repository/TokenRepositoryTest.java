package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
public class TokenRepositoryTest {
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    UserRepository userRepository;

    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));


    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgresContainer.stop();
    }

    @Test
    public void shouldCreateToken() {
        User user = userRepository.save(new User(null, "test", "test", false));
        Token token = tokenRepository.save(new Token("test", user));
        Assertions.assertNotNull(tokenRepository.findById(token.getId()));
    }

    @Test
    @Transactional
    public void shouldDeleteUserToken() {
        User user = userRepository.save(new User(null, "test", "test", false));
        Token token = tokenRepository.save(new Token("test", user));
        long count = tokenRepository.deleteByUserAndToken(user, "test");
        Assertions.assertTrue(tokenRepository.findById(token.getId()).isEmpty());
        Assertions.assertEquals(1, count);
    }
}
