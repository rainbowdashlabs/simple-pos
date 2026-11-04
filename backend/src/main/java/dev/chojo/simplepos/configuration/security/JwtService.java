package dev.chojo.simplepos.configuration.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Issues and verifies the login and refresh tokens.
 * <p>
 * The signing key comes from {@code security.jwt.secret-key} and is deliberately not part of the shipped
 * configuration: a key that is public lets anyone forge tokens. The application refuses to start without a key of
 * at least {@value #MIN_KEY_BYTES} bytes.
 * <p>
 * Tokens name this application as issuer and audience ({@code security.jwt.issuer} and {@code security.jwt.audience},
 * both {@code simple-pos} by default), and a token naming another issuer or audience is rejected, so a token signed
 * with the same key for another service is not accepted here.
 */
@Service
public class JwtService {
    static final int MIN_KEY_BYTES = 32;

    @Value("${security.jwt.secret-key:}")
    private String secretKey;

    @Value("${security.jwt.issuer:simple-pos}")
    private String issuer;
    @Value("${security.jwt.audience:simple-pos}")
    private String audience;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;
    @Value("${security.jwt.refresh-expiration-time}")
    private long jwtRefreshExpiration;

    @PostConstruct
    void requireSecretKey() {
        if (secretKey.getBytes(StandardCharsets.UTF_8).length < MIN_KEY_BYTES) {
            throw new IllegalStateException("security.jwt.secret-key (env SECURITY_JWT_SECRET_KEY) must be set to a "
                    + "random secret of at least " + MIN_KEY_BYTES + " bytes, e.g. the output of `openssl rand -hex 32`.");
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(Collections.emptyMap(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return generateToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails, Map<String, Object> claims) {
        if (!userDetails.getUsername().equals(extractUsername(token))) return false;
        if (isTokenExpired(token)) return false;
        Claims allClaims = extractAllClaims(token);
        for (var entry : claims.entrySet()) {
            if (!Objects.equals(allClaims.get(entry.getKey()), entry.getValue())) return false;
        }
        return true;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        requireOwnIssuerAndAudience(claims);
        return claims;
    }

    /**
     * Rejects a token issued by or for someone else. Tokens issued before the claims were added carry neither and
     * are still accepted, so nobody is logged out when this is deployed.
     */
    private void requireOwnIssuerAndAudience(Claims claims) {
        // TODO require both claims once tokens issued without them have expired
        if (claims.getIssuer() != null && !issuer.equals(claims.getIssuer())) {
            throw new IncorrectClaimException(null, claims, Claims.ISSUER, claims.getIssuer(), "Token was issued by someone else");
        }
        Set<String> audiences = claims.getAudience();
        if (audiences != null && !audiences.isEmpty() && !audiences.contains(audience)) {
            throw new IncorrectClaimException(null, claims, Claims.AUDIENCE, audiences, "Token is meant for someone else");
        }
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
