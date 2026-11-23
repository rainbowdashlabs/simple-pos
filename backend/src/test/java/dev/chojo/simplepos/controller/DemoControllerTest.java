package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.RateLimiter;
import dev.chojo.simplepos.configuration.security.RefreshCookie;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.security.TokenResponse;
import dev.chojo.simplepos.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Checks that each demo login signs in as its own demo user.
 */
class DemoControllerTest {
    private final AuthenticationService authentication = mock(AuthenticationService.class);
    private final RefreshCookie refreshCookie = mock(RefreshCookie.class);
    private final DemoController controller = new DemoController(authentication, new RateLimiter(), refreshCookie);

    @Test
    void demoLoginSignsInAsTheStaffDemoUser() {
        User demo = user("demo", Role.STAFF);

        controller.demoLogin(new MockHttpServletRequest());

        verify(authentication).issueTokens(demo);
    }

    @Test
    void adminDemoLoginSignsInAsTheDemoAdministrator() {
        User admin = user("demo-admin", Role.ADMIN);

        controller.demoAdminLogin(new MockHttpServletRequest());

        verify(authentication).issueTokens(admin);
    }

    private User user(String name, Role role) {
        User user = new User(null, name, "x", true, role);
        when(authentication.findByUsername(name)).thenReturn(user);
        when(authentication.issueTokens(user)).thenReturn(new TokenResponse(null, null));
        return user;
    }
}
