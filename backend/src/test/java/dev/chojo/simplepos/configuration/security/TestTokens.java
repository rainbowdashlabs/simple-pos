package dev.chojo.simplepos.configuration.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

/**
 * Signs tokens that differ from a valid login token in chosen ways, to check that they are refused.
 */
public final class TestTokens {
    private TestTokens() {
    }

    /**
     * A token for {@code user} signed with {@code key} and {@code algorithm}, holding the claims of a valid login token
     * changed by {@code change}.
     */
    public static String signed(String key, MacAlgorithm algorithm, Consumer<JwtClaimsSet.Builder> change) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("simple-pos")
                .audience(List.of("simple-pos"))
                .subject("user")
                .issuedAt(Instant.now().minusSeconds(300))
                .expiresAt(Instant.now().plusSeconds(60))
                .claim(LoginTokens.TYPE_CLAIM, LoginTokens.LOGIN_TYPE);
        change.accept(claims);
        String jcaName = switch (algorithm) {
            case HS256 -> "HmacSHA256";
            case HS384 -> "HmacSHA384";
            case HS512 -> "HmacSHA512";
        };
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), jcaName)));
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(algorithm).build(), claims.build())).getTokenValue();
    }
}
