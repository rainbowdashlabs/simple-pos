package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.User;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        userRepository.save(new User(null, "erroruser", passwordEncoder.encode("secret"), true));
        userRepository.save(new User(null, "disableduser", passwordEncoder.encode("secret"), false));
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
    void shouldAnswerDisabledAccountWithForbidden() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "disableduser").param("password", "secret"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Account is disabled. Ask an administrator to enable it."));
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
    void shouldAnswerDuplicateSignupWithConflict() throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"erroruser\", \"password\": \"other\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Username erroruser is already taken"))
                .andExpect(jsonPath("$.instance").value("/api/auth/signup"));
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
                .andExpect(jsonPath("$.detail").value("Account is disabled. Ask an administrator to enable it."));
        mvc.perform(get("/api/account").header("Authorization", "Bearer " + tokens.get("token")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Account is disabled. Ask an administrator to enable it."));
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
