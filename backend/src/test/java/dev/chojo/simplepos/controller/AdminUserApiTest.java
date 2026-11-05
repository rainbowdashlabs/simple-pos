package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.TokenRepository;
import dev.chojo.simplepos.repository.UserRepository;
import dev.chojo.simplepos.service.AuthenticationService;
import jakarta.persistence.EntityManager;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the user management API as an administrator, through the full security filter chain.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminUserApiTest {
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
    EntityManager entityManager;
    @Autowired
    TokenRepository tokenRepository;

    private MockMvc mvc;
    private User admin;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        admin = userRepository.save(new User(null, "theadmin", passwordEncoder.encode("adminsecret"), true, Role.ADMIN));
        adminToken = login("theadmin", "adminsecret").get("token");
    }

    @Test
    void shouldCreateEnabledUserThatCanLogIn() throws Exception {
        asAdmin(post("/api/admin/users"), "{\"username\": \" newstaff \", \"password\": \"password123\", \"role\": \"STAFF\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newstaff"))
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.enabled").value(true));

        login("newstaff", "password123");
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'newstaff')].role").value("STAFF"))
                .andExpect(jsonPath("$[*].password").isEmpty());
    }

    @Test
    void shouldRejectInvalidNewUsers() throws Exception {
        asAdmin(post("/api/admin/users"), "{\"username\": \"theadmin\", \"password\": \"password123\", \"role\": \"STAFF\"}")
                .andExpect(status().isConflict());
        asAdmin(post("/api/admin/users"), "{\"username\": \"shortpw\", \"password\": \"short\", \"role\": \"STAFF\"}")
                .andExpect(status().isBadRequest());
        asAdmin(post("/api/admin/users"), "{\"username\": \"  \", \"password\": \"password123\", \"role\": \"STAFF\"}")
                .andExpect(status().isBadRequest());
        asAdmin(post("/api/admin/users"), "{\"username\": \"norole\", \"password\": \"password123\"}")
                .andExpect(status().isBadRequest());
        asAdmin(post("/api/admin/users"), "{\"username\": \"blankpw\", \"password\": \"          \", \"role\": \"STAFF\"}")
                .andExpect(status().isBadRequest());
        asAdmin(post("/api/admin/users"), "{\"username\": \"TheAdmin\", \"password\": \"password123\", \"role\": \"STAFF\"}")
                .andExpect(status().isConflict());
        asAdmin(post("/api/admin/users"), "{\"username\": \"line\\nbreak\", \"password\": \"password123\", \"role\": \"STAFF\"}")
                .andExpect(status().isBadRequest());
        asAdmin(post("/api/admin/users"), "{\"username\": \"longpw\", \"password\": \"" + "x".repeat(73) + "\", \"role\": \"STAFF\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldChangeRoleAndEnabledState() throws Exception {
        User staff = userRepository.save(new User(null, "changeme", passwordEncoder.encode("password123"), true, Role.STAFF));

        asAdmin(patch("/api/admin/users/" + staff.getId()), "{\"role\": \"ADMIN\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.enabled").value(true));
        assertStored(staff.getId(), Role.ADMIN, true);
        asAdmin(patch("/api/admin/users/" + staff.getId()), "{\"enabled\": false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.enabled").value(false));
        assertStored(staff.getId(), Role.ADMIN, false);
        asAdmin(patch("/api/admin/users/" + staff.getId()), "{\"enabled\": true}")
                .andExpect(status().isOk());
        assertStored(staff.getId(), Role.ADMIN, true);
        asAdmin(patch("/api/admin/users/" + staff.getId()), "{}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldEndSessionsWhenAUserIsDisabled() throws Exception {
        User staff = userRepository.save(new User(null, "disableme", passwordEncoder.encode("password123"), true, Role.STAFF));
        String refresh = login("disableme", "password123").get("refresh");

        asAdmin(patch("/api/admin/users/" + staff.getId()), "{\"enabled\": false}")
                .andExpect(status().isOk());

        mvc.perform(post("/api/auth/refresh").param("token", refresh)).andExpect(status().isForbidden());
        assertTrue(tokenRepository.findByUserAndToken(staff, AuthenticationService.fingerprint(refresh)).isEmpty());
    }

    @Test
    void shouldKeepTheLastEnabledAdmin() throws Exception {
        userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN && user.isEnabled() && !user.getId().equals(admin.getId()))
                .forEach(user -> user.setEnabled(false));
        User staff = userRepository.save(new User(null, "plainstaff", "x", true, Role.STAFF));
        entityManager.flush();

        asAdmin(patch("/api/admin/users/" + admin.getId()), "{\"enabled\": false}")
                .andExpect(status().isConflict());
        asAdmin(patch("/api/admin/users/" + admin.getId()), "{\"role\": \"STAFF\"}")
                .andExpect(status().isConflict());
        asAdmin(patch("/api/admin/users/" + admin.getId()), "{\"role\": \"STAFF\", \"enabled\": false}")
                .andExpect(status().isConflict());
        asAdmin(patch("/api/admin/users/" + staff.getId()), "{\"role\": \"USER\", \"enabled\": false}")
                .andExpect(status().isOk());
        assertStored(admin.getId(), Role.ADMIN, true);

        userRepository.save(new User(null, "secondadmin", "x", true, Role.ADMIN));
        asAdmin(patch("/api/admin/users/" + admin.getId()), "{\"role\": \"STAFF\"}")
                .andExpect(status().isOk());
    }

    @Test
    void shouldEndSessionsWhenThePasswordIsReset() throws Exception {
        userRepository.save(new User(null, "resetme", passwordEncoder.encode("password123"), true, Role.STAFF));
        String refresh = login("resetme", "password123").get("refresh");
        User user = userRepository.findByUsername("resetme").orElseThrow();

        asAdmin(put("/api/admin/users/" + user.getId() + "/password"), "{\"password\": \"newpassword1\"}")
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/auth/refresh").param("token", refresh)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").param("username", "resetme").param("password", "password123"))
                .andExpect(status().isUnauthorized());
        login("resetme", "newpassword1");
    }

    @Test
    void shouldAnswerUnknownUserWithNotFound() throws Exception {
        asAdmin(patch("/api/admin/users/999999"), "{\"enabled\": true}").andExpect(status().isNotFound());
    }

    @Test
    void shouldStoreOnlyAHashOfTheNewPassword() throws Exception {
        asAdmin(post("/api/admin/users"), "{\"username\": \"hashed\", \"password\": \"password123\", \"role\": \"USER\"}")
                .andExpect(status().isCreated());

        String stored = userRepository.findByUsername("hashed").orElseThrow().getPassword();
        assertFalse(stored.contains("password123"));
        assertTrue(passwordEncoder.matches("password123", stored));
        assertEquals(Role.USER, userRepository.findByUsername("hashed").orElseThrow().getRole());
    }

    private void assertStored(int id, Role role, boolean enabled) {
        entityManager.flush();
        entityManager.clear();
        User stored = userRepository.findById(id).orElseThrow();
        assertEquals(role, stored.getRole());
        assertEquals(enabled, stored.isEnabled());
    }

    private ResultActions asAdmin(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private Map<String, String> login(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").param("username", username).param("password", password))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> tokens = JsonParserFactory.getJsonParser().parseMap(body);
        return Map.of("token", tokenOf(tokens.get("token")), "refresh", tokenOf(tokens.get("refresh")));
    }

    private static String tokenOf(Object token) {
        return (String) ((Map<?, ?>) token).get("token");
    }
}
