package dev.chojo.simplepos.configuration.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import dev.chojo.simplepos.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Issues login tokens and builds the decoder that checks them: HS256-signed JWTs naming the user as subject.
 * <p>
 * The signing key comes from {@code security.jwt.secret-key} and is deliberately not part of the shipped
 * configuration: a key that is public lets anyone forge tokens. The application refuses to start without a key of
 * at least {@value #MIN_KEY_BYTES} bytes.
 * <p>
 * Tokens name this application as issuer and audience ({@code security.jwt.issuer} and {@code security.jwt.audience},
 * both {@code simple-pos} by default) and carry {@code type: login}. A token lacking any of them is rejected: one
 * signed with the same key for another service is not accepted here, and neither are the refresh tokens earlier
 * versions issued as JWTs with the same key, which stay unexpired for up to their whole lifetime.
 */
@Component
public class LoginTokens {
    static final int MIN_KEY_BYTES = 32;
    static final String TYPE_CLAIM = "type";
    static final String LOGIN_TYPE = "login";

    private final SecretKey key;
    private final String issuer;
    private final String audience;
    private final Duration lifetime;
    private final JwtEncoder encoder;

    /**
     * @throws IllegalStateException if the secret key is missing or shorter than {@value #MIN_KEY_BYTES} bytes
     */
    public LoginTokens(@Value("${security.jwt.secret-key:}") String secretKey,
                       @Value("${security.jwt.issuer:simple-pos}") String issuer,
                       @Value("${security.jwt.audience:simple-pos}") String audience,
                       @Value("${security.jwt.expiration-time}") long lifetimeMillis) {
        this.key = signingKey(secretKey);
        this.issuer = issuer;
        this.audience = audience;
        this.lifetime = Duration.ofMillis(lifetimeMillis);
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    private static SecretKey signingKey(String secretKey) {
        byte[] bytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("security.jwt.secret-key (env SECURITY_JWT_SECRET_KEY) must be set to a "
                    + "random secret of at least " + MIN_KEY_BYTES + " bytes, e.g. the output of `openssl rand -hex 32`.");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    /**
     * A new login token for the user, valid for {@link #lifetime()}.
     */
    public String issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .audience(List.of(audience))
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(lifetime))
                .claim(TYPE_CLAIM, LOGIN_TYPE)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Duration lifetime() {
        return lifetime;
    }

    /**
     * A decoder that accepts only login tokens of this application signed with the key, carrying an expiry that has
     * not passed. No clock skew is allowed: tokens are issued and checked by the same server.
     */
    public JwtDecoder decoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(
                new JwtTimestampValidator(Duration.ZERO),
                new JwtClaimValidator<Instant>(JwtClaimNames.EXP, Objects::nonNull),
                new JwtIssuerValidator(issuer),
                new JwtClaimValidator<Collection<String>>(JwtClaimNames.AUD, aud -> aud != null && aud.contains(audience)),
                new JwtClaimValidator<Object>(TYPE_CLAIM, LOGIN_TYPE::equals)));
        return decoder;
    }
}
