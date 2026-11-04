package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.UserRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
                .andExpect(status().isAccepted());
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
            mvc.perform(post("/api/auth/refresh").param("token", "garbage").with(fromAddress("10.9.1.1")))
                    .andExpect(status().isUnauthorized());
        }

        mvc.perform(post("/api/auth/refresh").param("token", "garbage").with(fromAddress("10.9.1.1")))
                .andExpect(status().isTooManyRequests());
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
                .andExpect(jsonPath("$.detail").value("Product id is missing"));
    }

    @Test
    void shouldEndTheSessionOnLogout() throws Exception {
        String refresh = login().get("refresh");

        mvc.perform(post("/api/auth/logout").param("token", refresh)).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/refresh").param("token", refresh)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/logout").param("token", "unknown")).andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectReusedRefreshToken() throws Exception {
        String refresh = login().get("refresh");

        mvc.perform(post("/api/auth/refresh").param("token", refresh))
                .andExpect(status().isAccepted());
        mvc.perform(post("/api/auth/refresh").param("token", refresh))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRefreshAndLoginTokenOfUserDisabledAfterLogin() throws Exception {
        Map<String, String> tokens = login();
        User user = userRepository.findByUsername("erroruser").orElseThrow();
        user.setEnabled(false);
        userRepository.save(user);

        mvc.perform(post("/api/auth/refresh").param("token", tokens.get("refresh")))
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
        String body = mvc.perform(post("/api/auth/login").param("username", "erroruser").param("password", "secret"))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> tokens = JsonParserFactory.getJsonParser().parseMap(body);
        return Map.of("token", tokenOf(tokens.get("token")), "refresh", tokenOf(tokens.get("refresh")));
    }

    private static String tokenOf(Object token) {
        return (String) ((Map<?, ?>) token).get("token");
    }
}
