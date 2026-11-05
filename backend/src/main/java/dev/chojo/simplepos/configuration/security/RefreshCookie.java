package dev.chojo.simplepos.configuration.security;

import dev.chojo.simplepos.entity.security.SessionResponse;
import dev.chojo.simplepos.entity.security.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Hands the refresh token to the browser in a cookie that scripts cannot read, so a script injected into the page
 * cannot steal a session that outlives the short-lived login token.
 * <p>
 * The cookie is {@code HttpOnly}, {@code SameSite=Strict}, sent only to {@value #PATH}, and {@code Secure} unless
 * {@code security.refresh-cookie.secure} is false, which only plain-http development needs. Because the browser
 * adds it on its own, the endpoints that read it also require the header {@value #CSRF_HEADER}: a form on another
 * site cannot set a header, and a script on another origin cannot send one without a CORS preflight that is refused.
 */
@Component
public class RefreshCookie {
    public static final String NAME = "simple_pos_refresh";
    public static final String PATH = "/api/auth";
    public static final String CSRF_HEADER = "X-Requested-With";
    public static final String CSRF_VALUE = "simple-pos";

    private final boolean secure;

    public RefreshCookie(@Value("${security.refresh-cookie.secure:true}") boolean secure) {
        this.secure = secure;
    }

    /**
     * Answers with the login token in the body and the refresh token in the cookie.
     */
    public ResponseEntity<SessionResponse> respond(TokenResponse tokens, String username) {
        ResponseCookie cookie = cookie(tokens.refresh().token(), Duration.ofMillis(tokens.refresh().expiresIn()));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new SessionResponse(tokens.token(), username));
    }

    /**
     * A cookie that makes the browser forget the refresh token.
     */
    public String cleared() {
        return cookie("", Duration.ZERO).toString();
    }

    /**
     * @throws AccessDeniedException if the request lacks the header that a cross-site request cannot carry
     */
    public static void requireOwnFrontend(String requestedWith) {
        if (!CSRF_VALUE.equals(requestedWith)) throw new AccessDeniedException("Missing " + CSRF_HEADER + " header");
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(PATH)
                .maxAge(maxAge)
                .build();
    }
}
