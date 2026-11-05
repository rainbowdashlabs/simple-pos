package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Role;
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
        testUser = userRepository.save(new User(null, "tokenuser", "password", true, Role.STAFF));
    }

    @Test
    void shouldCreateToken() {
        Token token = tokenRepository.save(new Token("mytoken123", testUser, null));

        assertNotNull(token.getId());
        assertTrue(tokenRepository.findById(token.getId()).isPresent());
    }

    @Test
    void shouldFindByToken() {
        tokenRepository.save(new Token("findme", testUser, null));

        Optional<Token> found = tokenRepository.findByToken("findme");
        assertTrue(found.isPresent());
        assertEquals("findme", found.get().getToken());
    }

    @Test
    void shouldFindByUserAndToken() {
        tokenRepository.save(new Token("usertoken", testUser, null));

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
    void shouldDeleteByFingerprint() {
        Token token = tokenRepository.save(new Token("deleteme", testUser, null));

        assertEquals(1, tokenRepository.deleteByFingerprint("deleteme"));
        assertEquals(0, tokenRepository.deleteByFingerprint("deleteme"));
        assertTrue(tokenRepository.findById(token.getId()).isEmpty());
    }

    @Test
    void shouldStoreMultipleTokensPerUser() {
        tokenRepository.save(new Token("token1", testUser, null));
        tokenRepository.save(new Token("token2", testUser, null));

        assertTrue(tokenRepository.findByUserAndToken(testUser, "token1").isPresent());
        assertTrue(tokenRepository.findByUserAndToken(testUser, "token2").isPresent());
    }
}
