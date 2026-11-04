package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.configuration.security.JwtService;
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

import java.util.List;
import java.util.Map;

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
    @Autowired
    JwtService jwtService;

    @Test
    void shouldDeleteOnlyExpiredAndUnverifiableTokens() {
        User user = userRepository.save(new User(null, "cleanupuser", "password", true));
        Map<String, Object> refresh = Map.of("type", "refresh");
        Token valid = tokenRepository.save(new Token(jwtService.generateToken(refresh, user, 60_000), user));
        tokenRepository.save(new Token(jwtService.generateToken(refresh, user, -60_000), user));
        tokenRepository.save(new Token("not-a-jwt", user));

        int deleted = tokenCleanupService.deleteUnusableTokens();

        assertTrue(deleted >= 2);
        assertEquals(List.of(valid.getId()), tokenRepository.findAll().stream()
                .filter(token -> token.getUser().getId().equals(user.getId()))
                .map(Token::getId)
                .toList());
    }
}
