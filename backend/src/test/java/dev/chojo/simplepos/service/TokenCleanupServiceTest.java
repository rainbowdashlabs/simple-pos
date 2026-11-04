package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.TokenRepository;
import dev.chojo.simplepos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TokenCleanupServiceTest {
    @Autowired
    TokenCleanupService tokenCleanupService;
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    void shouldDeleteExpiredTokensAndTokensWithoutExpiry() {
        User user = userRepository.save(new User(null, "cleanupuser", "password", true, Role.STAFF));
        Token valid = tokenRepository.save(new Token("valid", user, Instant.now().plus(1, ChronoUnit.DAYS)));
        tokenRepository.save(new Token("expired", user, Instant.now().minus(1, ChronoUnit.MINUTES)));
        tokenRepository.save(new Token("legacy-plain-text", user, null));

        int deleted = tokenCleanupService.deleteUnusableTokens();

        assertTrue(deleted >= 2);
        assertEquals(List.of(valid.getId()), tokenRepository.findAll().stream()
                .filter(token -> token.getUser().getId().equals(user.getId()))
                .map(Token::getId)
                .toList());
    }
}
