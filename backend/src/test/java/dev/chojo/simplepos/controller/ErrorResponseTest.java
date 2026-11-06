package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.RefreshCookie;
import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.configuration.security.LoginTokens;
import dev.chojo.simplepos.configuration.security.TestTokens;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.UserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks the status and the ProblemDetail body of error responses through the full filter chain, including the
 * security filters.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ErrorResponseTest {
    @Autowired
    LoginTokens loginTokens;
    @Value("${security.jwt.secret-key}")
    String secretKey;
    @Autowired
    WebApplicationContext context;
    @Autowired
    @Qualifier("springSecurityFilterChain")
    Filter springSecurityFilterChain;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AccountRepository accountRepository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        userRepository.save(new User(null, "erroruser", passwordEncoder.encode("secret"), true, Role.STAFF));
        userRepository.save(new User(null, "disableduser", passwordEncoder.encode("secret"), false, Role.STAFF));
    }

    @Test
    void shouldAnswerMissingTokenWithUnauthorized() throws Exception {
        mvc.perform(get("/api/account"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldAnswerInvalidTokenWithUnauthorized() throws Exception {
        mvc.perform(get("/api/account").header("Authorization", "Bearer garbage"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid or expired token"));
    }

    @Test
    void shouldAnswerOldRefreshTokensAndTokensOfUnknownUsersWithUnauthorized() throws Exception {
        String oldRefresh = TestTokens.signed(secretKey, MacAlgorithm.HS256, claims -> claims
                .subject("erroruser").claim("type", "refresh"));
        String unknownUser = loginTokens.issue(new User(null, "ghostuser", "x", true, Role.STAFF));

        for (String token : new String[]{oldRefresh, unknownUser}) {
            mvc.perform(get("/api/account").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Invalid or expired token"));
        }
    }

    @Test
    void shouldAnswerBlankTokenWithUnauthorized() throws Exception {
        mvc.perform(get("/api/account").header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAnswerBadCredentialsWithUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "erroruser").param("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.instance").value("/api/auth/login"));
    }

    @Test
    void shouldAnswerTooManyFailedLoginsWithTooManyRequests() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").param("username", "erroruser").param("password", "wrong")
                            .with(fromAddress("10.9.0.1")))
                    .andExpect(status().isUnauthorized());
        }

        mvc.perform(post("/api/auth/login").param("username", "erroruser").param("password", "secret")
                        .with(fromAddress("10.9.0.1")))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.retryAfter").isNumber());
        mvc.perform(post("/api/auth/login").param("username", "erroruser").param("password", "secret")
                        .with(fromAddress("10.9.0.2")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotCountADisabledAccountWithTheRightPassword() throws Exception {
        for (int i = 0; i < 6; i++) {
            mvc.perform(post("/api/auth/login").param("username", "disableduser").param("password", "secret")
                            .with(fromAddress("10.9.2.1")))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void shouldRejectOverlongCredentialsAsWrongWithoutAnError() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "erroruser").param("password", "x".repeat(100))
                        .with(fromAddress("10.9.3.1")))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").param("username", "u".repeat(65)).param("password", "secret")
                        .with(fromAddress("10.9.3.1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAnswerTooManyRefreshesWithTooManyRequests() throws Exception {
        for (int i = 0; i < 30; i++) {
            mvc.perform(refresh("garbage").with(fromAddress("10.9.1.1")))
                    .andExpect(status().isUnauthorized());
        }

        mvc.perform(refresh("garbage").with(fromAddress("10.9.1.1")))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldReportNoPurchasesAsAZeroTotal() throws Exception {
        mvc.perform(get("/api/purchase/stats/total").param("after", "2999-01-01T00:00:00Z")
                        .header("Authorization", "Bearer " + login().get("token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(0.0));
    }

    @Test
    void shouldAnswerDisabledAccountWithForbidden() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "disableduser").param("password", "secret"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Account is disabled. Ask an administrator to enable it."))
                .andExpect(jsonPath("$.accountDisabled").value(true));
    }

    @Test
    void shouldNotRevealDisabledAccountWithoutCorrectPassword() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "disableduser").param("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accountDisabled").doesNotExist());
    }

    @Test
    void shouldAnswerUnknownAccountWithNotFound() throws Exception {
        mvc.perform(get("/api/account/999999").header("Authorization", "Bearer " + login().get("token")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Account 999999 not found"))
                .andExpect(jsonPath("$.instance").value("/api/account/999999"))
                .andExpect(jsonPath("$.entity").value("Account"))
                .andExpect(jsonPath("$.id").value(999999));
    }

    @Test
    void shouldAnswerMissingParameterWithBadRequest() throws Exception {
        mvc.perform(get("/api/purchase/stats/top").header("Authorization", "Bearer " + login().get("token")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldAnswerPurchaseWithoutProductWithBadRequest() throws Exception {
        Account account = accountRepository.save(new Account(null, "BadRequestAccount", Instant.now()));

        mvc.perform(post("/api/account/" + account.getId() + "/purchase")
                        .header("Authorization", "Bearer " + login().get("token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"amount\": 1}]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("positions[0].productId"));
    }

    @Test
    void shouldEndTheSessionOnLogout() throws Exception {
        String refresh = login().get("refresh");

        mvc.perform(logout(refresh)).andExpect(status().isNoContent());
        mvc.perform(refresh(refresh)).andExpect(status().isUnauthorized());
        mvc.perform(logout("unknown")).andExpect(status().isNoContent());
    }

    @Test
    void shouldChangeTheOwnPasswordAndEndOtherSessions() throws Exception {
        userRepository.save(new User(null, "pwchanger", passwordEncoder.encode("old-secret"), true, Role.USER));
        Map<String, String> other = login("pwchanger", "old-secret");
        Map<String, String> mine = login("pwchanger", "old-secret");

        mvc.perform(put("/api/user/me/password").header("Authorization", "Bearer " + mine.get("token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"current\": \"wrong\", \"password\": \"new-secret\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/user/me/password").header("Authorization", "Bearer " + mine.get("token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"current\": \"old-secret\", \"password\": \"short\"}"))
                .andExpect(status().isBadRequest());
        String newRefresh = mvc.perform(put("/api/user/me/password").header("Authorization", "Bearer " + mine.get("token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"current\": \"old-secret\", \"password\": \"new-secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refresh").doesNotExist())
                .andExpect(cookie().httpOnly(RefreshCookie.NAME, true))
                .andExpect(cookie().path(RefreshCookie.NAME, RefreshCookie.PATH))
                .andReturn().getResponse().getCookie(RefreshCookie.NAME).getValue();

        mvc.perform(refresh(other.get("refresh"))).andExpect(status().isUnauthorized());
        mvc.perform(refresh(newRefresh)).andExpect(status().isOk());
        login("pwchanger", "new-secret");
    }

    @Test
    void shouldCountAWrongCurrentPasswordAsAFailedLogin() throws Exception {
        userRepository.save(new User(null, "pwguesser", passwordEncoder.encode("old-secret"), true, Role.USER));
        String token = login("pwguesser", "old-secret").get("token");
        for (int attempt = 0; attempt < 6; attempt++) {
            mvc.perform(put("/api/user/me/password").header("Authorization", "Bearer " + token).with(fromAddress("10.7.1.1"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"current\": \"old-secret\", \"password\": \"short\"}"))
                    .andExpect(status().isBadRequest());
        }
        for (int attempt = 0; attempt < 5; attempt++) {
            mvc.perform(put("/api/user/me/password").header("Authorization", "Bearer " + token).with(fromAddress("10.7.1.2"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"current\": \"wrong\", \"password\": \"new-secret\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value("current"));
        }
        mvc.perform(put("/api/user/me/password").header("Authorization", "Bearer " + token).with(fromAddress("10.7.1.2"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"current\": \"old-secret\", \"password\": \"new-secret\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldKeepTheRefreshTokenInAnHttpOnlyCookie() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "erroruser").param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refresh").doesNotExist())
                .andExpect(jsonPath("$.username").value("erroruser"))
                .andExpect(jsonPath("$.token.token").isString())
                .andExpect(cookie().httpOnly(RefreshCookie.NAME, true))
                .andExpect(cookie().secure(RefreshCookie.NAME, true))
                .andExpect(cookie().sameSite(RefreshCookie.NAME, "Strict"))
                .andExpect(cookie().path(RefreshCookie.NAME, RefreshCookie.PATH));
    }

    @Test
    void shouldRefuseRefreshAndLogoutWithoutTheFrontendHeader() throws Exception {
        String refresh = login().get("refresh");

        mvc.perform(post("/api/auth/refresh").cookie(new Cookie(RefreshCookie.NAME, refresh)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout").cookie(new Cookie(RefreshCookie.NAME, refresh)))
                .andExpect(status().isForbidden());
        mvc.perform(refresh(refresh)).andExpect(status().isOk());
    }

    @Test
    void shouldAnswerARefreshWithoutCookieWithUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/refresh").header(RefreshCookie.CSRF_HEADER, RefreshCookie.CSRF_VALUE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldClearTheCookieOnLogout() throws Exception {
        mvc.perform(logout(login().get("refresh")))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(RefreshCookie.NAME, 0));
    }

    @Test
    void shouldRotateTheRefreshCookieAndRejectAReusedOne() throws Exception {
        String refresh = login().get("refresh");

        String rotated = mvc.perform(refresh(refresh))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("erroruser"))
                .andExpect(cookie().httpOnly(RefreshCookie.NAME, true))
                .andExpect(cookie().sameSite(RefreshCookie.NAME, "Strict"))
                .andExpect(cookie().path(RefreshCookie.NAME, RefreshCookie.PATH))
                .andExpect(cookie().maxAge(RefreshCookie.NAME, 2592000))
                .andReturn().getResponse().getCookie(RefreshCookie.NAME).getValue();

        assertNotEquals(refresh, rotated);
        mvc.perform(refresh(refresh))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().maxAge(RefreshCookie.NAME, 0));
        mvc.perform(refresh(rotated)).andExpect(status().isOk());
    }

    @Test
    void shouldAnswerALogoutWithoutCookieAndClearIt() throws Exception {
        mvc.perform(post("/api/auth/logout").header(RefreshCookie.CSRF_HEADER, RefreshCookie.CSRF_VALUE))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(RefreshCookie.NAME, 0));
    }

    @Test
    void shouldRejectRefreshAndLoginTokenOfUserDisabledAfterLogin() throws Exception {
        Map<String, String> tokens = login();
        User user = userRepository.findByUsername("erroruser").orElseThrow();
        user.setEnabled(false);
        userRepository.save(user);

        mvc.perform(refresh(tokens.get("refresh")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Account is disabled. Ask an administrator to enable it."))
                .andExpect(jsonPath("$.accountDisabled").value(true));
        mvc.perform(get("/api/account").header("Authorization", "Bearer " + tokens.get("token")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Account is disabled. Ask an administrator to enable it."))
                .andExpect(jsonPath("$.accountDisabled").value(true));
    }

    private static RequestPostProcessor fromAddress(String address) {
        return request -> {
            request.setRemoteAddr(address);
            return request;
        };
    }

    private Map<String, String> login() throws Exception {
        return login("erroruser", "secret");
    }

    private Map<String, String> login(String username, String password) throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/api/auth/login").param("username", username).param("password", password))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        Map<String, Object> tokens = JsonParserFactory.getJsonParser().parseMap(response.getContentAsString());
        return Map.of("token", tokenOf(tokens.get("token")), "refresh", response.getCookie(RefreshCookie.NAME).getValue());
    }

    private static MockHttpServletRequestBuilder refresh(String token) {
        return post("/api/auth/refresh").cookie(new Cookie(RefreshCookie.NAME, token))
                .header(RefreshCookie.CSRF_HEADER, RefreshCookie.CSRF_VALUE);
    }

    private static MockHttpServletRequestBuilder logout(String token) {
        return post("/api/auth/logout").cookie(new Cookie(RefreshCookie.NAME, token))
                .header(RefreshCookie.CSRF_HEADER, RefreshCookie.CSRF_VALUE);
    }

    private static String tokenOf(Object token) {
        return (String) ((Map<?, ?>) token).get("token");
    }
}
