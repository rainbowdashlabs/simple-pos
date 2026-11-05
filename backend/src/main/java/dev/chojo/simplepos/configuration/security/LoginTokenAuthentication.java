package dev.chojo.simplepos.configuration.security;

import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Turns a verified login token into the authentication of its user. The user is loaded from the database on every
 * request, so a changed role or a disabled account takes effect at once rather than when the token expires.
 */
@Component
public class LoginTokenAuthentication implements Converter<Jwt, AbstractAuthenticationToken> {
    /**
     * The detail of every 401 for a login token that is not accepted, whatever the reason.
     */
    public static final String INVALID_TOKEN = "Invalid or expired token";

    private final UserRepository userRepository;

    public LoginTokenAuthentication(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @throws InvalidBearerTokenException     if no user with the token's subject exists
     * @throws DisabledException               if the user is disabled
     * @throws AuthenticationServiceException if the user could not be loaded, so the failure is answered like every
     *                                        other security error instead of escaping the filter chain
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        User user = load(jwt.getSubject()).orElseThrow(() -> new InvalidBearerTokenException(INVALID_TOKEN));
        if (!user.isEnabled()) throw new DisabledException("Account is disabled");
        return UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
    }

    private Optional<User> load(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (RuntimeException e) {
            throw new AuthenticationServiceException("Could not load the user of a login token", e);
        }
    }
}
