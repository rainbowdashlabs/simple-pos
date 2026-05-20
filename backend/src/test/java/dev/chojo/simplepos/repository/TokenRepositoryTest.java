package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TokenRepositoryTest {
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User(null, "tokenuser", "password", true));
    }

    @Test
    void shouldCreateToken() {
        Token token = tokenRepository.save(new Token("mytoken123", testUser));

        assertNotNull(token.getId());
        assertTrue(tokenRepository.findById(token.getId()).isPresent());
    }

    @Test
    void shouldFindByToken() {
        tokenRepository.save(new Token("findme", testUser));

        Optional<Token> found = tokenRepository.findByToken("findme");
        assertTrue(found.isPresent());
        assertEquals("findme", found.get().getToken());
    }

    @Test
    void shouldFindByUserAndToken() {
        tokenRepository.save(new Token("usertoken", testUser));

        Optional<Token> found = tokenRepository.findByUserAndToken(testUser, "usertoken");
        assertTrue(found.isPresent());
        assertEquals(testUser.getId(), found.get().getUser().getId());
    }

    @Test
    void shouldNotFindNonExistentToken() {
        Optional<Token> found = tokenRepository.findByToken("nonexistent");
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldDeleteByUserAndToken() {
        Token token = tokenRepository.save(new Token("deleteme", testUser));

        long count = tokenRepository.deleteByUserAndToken(testUser, "deleteme");
        assertEquals(1, count);
        assertTrue(tokenRepository.findById(token.getId()).isEmpty());
    }

    @Test
    void shouldNotDeleteTokenOfDifferentUser() {
        User otherUser = userRepository.save(new User(null, "otheruser", "password", true));
        tokenRepository.save(new Token("shared", testUser));

        long count = tokenRepository.deleteByUserAndToken(otherUser, "shared");
        assertEquals(0, count);
    }

    @Test
    void shouldStoreMultipleTokensPerUser() {
        tokenRepository.save(new Token("token1", testUser));
        tokenRepository.save(new Token("token2", testUser));

        assertTrue(tokenRepository.findByUserAndToken(testUser, "token1").isPresent());
        assertTrue(tokenRepository.findByUserAndToken(testUser, "token2").isPresent());
    }
}
