package dev.chojo.simplepos.service;

import dev.chojo.simplepos.configuration.security.JwtService;
import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.repository.TokenRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Deletes stored refresh tokens that can never be used again: expired ones, and ones that no longer verify, e.g.
 * because the signing key changed. A refresh token is only deleted on use, so without this the table keeps every
 * token of a session that was abandoned. Runs a minute after startup ({@code security.jwt.cleanup.initial-delay}) and
 * then once a day.
 */
@Service
public class TokenCleanupService {
    private static final Logger log = getLogger(TokenCleanupService.class);
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    public TokenCleanupService(TokenRepository tokenRepository, JwtService jwtService) {
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
    }

    /**
     * @return the number of deleted tokens
     */
    @Scheduled(initialDelayString = "${security.jwt.cleanup.initial-delay:PT1M}", fixedDelayString = "PT24H")
    @Transactional
    public int deleteUnusableTokens() {
        List<Token> unusable = tokenRepository.findAll().stream()
                .filter(token -> !jwtService.isUnexpiredAndVerifiable(token.getToken()))
                .toList();
        tokenRepository.deleteAllInBatch(unusable);
        if (!unusable.isEmpty()) log.info("Deleted {} expired or unverifiable refresh tokens", unusable.size());
        return unusable.size();
    }
}
