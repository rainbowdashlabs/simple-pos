package dev.chojo.simplepos.service;

import dev.chojo.simplepos.repository.TokenRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Deletes stored refresh tokens that can never be used again: expired ones, and ones stored before expiries were
 * recorded. A refresh token is only deleted on use or logout, so without this the table keeps every token of a
 * session that was abandoned. Runs a minute after startup ({@code security.jwt.cleanup.initial-delay}) and then once
 * a day.
 */
@Service
public class TokenCleanupService {
    private static final Logger log = getLogger(TokenCleanupService.class);
    private final TokenRepository tokenRepository;

    public TokenCleanupService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * @return the number of deleted tokens
     */
    @Scheduled(initialDelayString = "${security.jwt.cleanup.initial-delay:PT1M}", fixedDelayString = "PT24H")
    @Transactional
    public int deleteUnusableTokens() {
        int deleted = tokenRepository.deleteExpired(Instant.now());
        if (deleted > 0) log.info("Deleted {} expired refresh tokens", deleted);
        return deleted;
    }
}
