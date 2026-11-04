package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.CashRepository;
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

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks that paging, stats sorting, cash entries and deposits are validated and rejected with 400.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InputCheckApiTest {
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
    @Autowired
    CashRepository cashRepository;

    private MockMvc mvc;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        userRepository.save(new User(null, "inputadmin", passwordEncoder.encode("secret"), true, Role.ADMIN));
        token = login();
    }

    @Test
    void pageSizeOutOfRangeIsABadRequest() throws Exception {
        for (String limit : new String[]{"0", "501", "-1"}) {
            mvc.perform(get("/api/purchase/stats/top").param("after", "2020-01-01T00:00:00Z").param("limit", limit)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("limit"));
        }
    }

    @Test
    void negativePageIsABadRequest() throws Exception {
        mvc.perform(get("/api/cash").param("page", "-1").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("page"));
    }

    @Test
    void storageIsPaged() throws Exception {
        mvc.perform(get("/api/storage").param("limit", "1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(1)));
    }

    @Test
    void unknownSortingIsABadRequest() throws Exception {
        mvc.perform(get("/api/purchase/stats/top").param("after", "2020-01-01T00:00:00Z").param("sorting", "price; drop")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/purchase/stats/top").param("after", "2020-01-01T00:00:00Z").param("sorting", "profit")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void depositMustBePositive() throws Exception {
        Account account = accountRepository.save(new Account(null, "Depositor", Instant.now()));
        for (String amount : new String[]{"0", "-5", "null"}) {
            mvc.perform(post("/api/account/" + account.getId() + "/balance").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": " + amount + "}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void cashEntriesNeedAKnownTypeAndAnAmount() throws Exception {
        mvc.perform(post("/api/cash").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 0, \"type\": \"other\", \"note\": \"zero\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/cash").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 5, \"type\": \"bribe\", \"note\": \"unknown\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/cash").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": -3.5, \"type\": \"pledge\", \"note\": \"bottles\"}"))
                .andExpect(status().isCreated());
        assertTrue(cashRepository.findAll().stream()
                .anyMatch(cash -> cash.getType() == CashType.PLEDGE && "bottles".equals(cash.getNote())));
    }

    private String login() throws Exception {
        String body = mvc.perform(post("/api/auth/login").param("username", "inputadmin").param("password", "secret"))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> tokens = JsonParserFactory.getJsonParser().parseMap(body);
        return (String) ((Map<?, ?>) tokens.get("token")).get("token");
    }
}
