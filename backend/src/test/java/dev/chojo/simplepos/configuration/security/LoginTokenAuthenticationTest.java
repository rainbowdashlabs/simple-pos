package dev.chojo.simplepos.configuration.security;

import dev.chojo.simplepos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginTokenAuthenticationTest {
    @Test
    void shouldReportAFailedUserLookupAsAnAuthenticationServiceFailure() {
        UserRepository users = mock(UserRepository.class);
        when(users.findByUsername(any())).thenThrow(new DataAccessResourceFailureException("database down"));
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "HS256").subject("user").build();

        assertThrows(AuthenticationServiceException.class, () -> new LoginTokenAuthentication(users).convert(jwt));
    }
}
