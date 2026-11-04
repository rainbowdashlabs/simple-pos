package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.configuration.RoleMigration;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.UserRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks which role reaches which part of the API through the full security filter chain.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoleAccessTest {
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
    RoleMigration roleMigration;
    @Autowired
    EntityManager entityManager;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        for (Role role : Role.values()) {
            String name = "role" + role.name().toLowerCase();
            userRepository.save(new User(null, name, passwordEncoder.encode("secret"), true, role));
        }
    }

    @Test
    void shouldLetUserOnlyReachOwnProfile() throws Exception {
        String token = login("roleuser");
        mvc.perform(get("/api/user/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
        mvc.perform(get("/api/user/preferences").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(put("/api/user/preferences").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locale\": \"de\", \"colorTheme\": \"blue\", \"darkMode\": \"dark\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/account").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldLetStaffUseTheApplicationButNotAdministration() throws Exception {
        String token = login("rolestaff");
        mvc.perform(get("/api/account").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/user/preferences").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldLetAdminDoWhatStaffCan() throws Exception {
        mvc.perform(get("/api/account").header("Authorization", "Bearer " + login("roleadmin")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotOfferPublicSignup() throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"newcomer\", \"password\": \"password123\"}"))
                .andExpect(status().isMethodNotAllowed());

        assertTrue(userRepository.findByUsername("newcomer").isEmpty());
    }

    @Test
    void shouldMakeEnabledUsersWithoutRoleAdminsAndDisabledOnesUsers() {
        User legacy = userRepository.save(new User(null, "legacyuser", "secret", true, null));
        User signup = userRepository.save(new User(null, "legacysignup", "secret", false, null));
        entityManager.flush();

        roleMigration.run(null);

        entityManager.clear();
        assertEquals(Role.ADMIN, userRepository.findById(legacy.getId()).orElseThrow().getRole());
        assertEquals(Role.USER, userRepository.findById(signup.getId()).orElseThrow().getRole());
        assertEquals(Role.STAFF, userRepository.findByUsername("rolestaff").orElseThrow().getRole());
    }

    private String login(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/login").param("username", username).param("password", "secret"))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> tokens = JsonParserFactory.getJsonParser().parseMap(body);
        return (String) ((Map<?, ?>) tokens.get("token")).get("token");
    }
}
