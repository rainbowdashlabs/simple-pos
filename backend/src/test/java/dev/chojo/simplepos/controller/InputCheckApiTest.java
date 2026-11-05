package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.repository.UserRepository;
import dev.chojo.simplepos.service.ProductService;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
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
    @Autowired
    ProductService productService;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    PurchaseRepository purchaseRepository;

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

    @Test
    void aSaleSendsItsPurchasesAndCanBeUndone() throws Exception {
        Category category = categoryRepository.save(new Category(null, "UndoCat"));
        Product product = productService.create(new Product(null, category, "UndoProduct", 2.0, true,
                new Recipe(null, null, Collections.emptyList())));
        Account account = accountRepository.save(new Account(null, "UndoAccount", Instant.now()));

        String receipt = mvc.perform(post("/api/account/" + account.getId() + "/purchase").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"product\": {\"id\": " + product.getId() + "}, \"amount\": 2}]"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.purchaseIds.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        String ids = receipt.substring(receipt.indexOf('['), receipt.indexOf(']') + 1);

        mvc.perform(post("/api/purchase/cancel").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\": " + ids + "}"))
                .andExpect(status().isNoContent());
        for (String body : new String[]{"{\"ids\": []}", "{\"ids\": [null]}"}) {
            mvc.perform(post("/api/purchase/cancel").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void shoppingListDaysAreChecked() throws Exception {
        for (String days : new String[]{"0", "91"}) {
            mvc.perform(get("/api/storage/shopping-list").header("Authorization", "Bearer " + token).param("days", days))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("days"));
        }
    }

    @Test
    void requestBodiesAreValidatedAtEveryEndpoint() throws Exception {
        Account account = accountRepository.save(new Account(null, "ValidatedAccount", Instant.now()));
        long cashBefore = cashRepository.count();
        String[][] cases = {
                {"POST", "/api/account", "{\"name\": \" \"}"},
                {"POST", "/api/account", "{\"name\": \"" + "a".repeat(101) + "\"}"},
                {"PATCH", "/api/account/" + account.getId(), "{\"name\": \"line\\nbreak\"}"},
                {"PUT", "/api/user/preferences", "{\"locale\": \"fr\", \"colorTheme\": \"blue\", \"darkMode\": \"dark\"}"},
                {"POST", "/api/storage", "{\"ingredient\": {\"id\": 1}, \"price\": 1, \"pledge\": 0, \"amount\": -5}"},
                {"POST", "/api/storage", "{\"ingredient\": {\"id\": 1}, \"price\": 1, \"total\": 5, \"pledge\": 0, \"amount\": 5}"},
                {"POST", "/api/storage", "{\"ingredient\": {\"id\": 1}, \"price\": 1, \"pledge\": -1, \"amount\": 5}"},
                {"PUT", "/api/storage", "[{\"ingredient\": 1, \"amount\": -1}]"},
                {"PUT", "/api/storage", "[null]"},
                {"POST", "/api/purchase/guest", "[]"},
                {"POST", "/api/purchase/guest", "[{\"product\": {\"id\": 1}, \"amount\": 0}]"},
                {"POST", "/api/account/" + account.getId() + "/purchase", "[]"},
                {"POST", "/api/account/" + account.getId() + "/purchase", "[{\"product\": {\"id\": 1}, \"amount\": 0}]"},
        };
        for (String[] request : cases) {
            mvc.perform(request(HttpMethod.valueOf(request[0]), request[1]).header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request[2]))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isNotEmpty());
        }
        assertEquals(cashBefore, cashRepository.count());
    }

    @Test
    void theSummaryAndCartBodiesWithTheUnitCostWork() throws Exception {
        Category category = categoryRepository.save(new Category(null, "SummaryCat"));
        Product product = productService.create(new Product(null, category, "SummaryProduct", 2.0, true,
                new Recipe(null, null, Collections.emptyList())));

        mvc.perform(post("/api/purchase/guest").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"product\": {\"id\": " + product.getId() + ", \"unitCost\": 1.23}, \"amount\": 2}]"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/purchase/stats/summary").param("after", "2020-01-01T00:00:00Z")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sales").isNumber())
                .andExpect(jsonPath("$.revenue").isNumber())
                .andExpect(jsonPath("$.day").doesNotExist());
    }

    @Test
    void returnedEmptiesAreChecked() throws Exception {
        for (String body : new String[]{"[]", "[null]", "[{\"ingredientId\": 1, \"bottles\": -1, \"crates\": 0}]"}) {
            mvc.perform(post("/api/cash/pledge/return").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void dailyStatsSendTheBusinessDate() throws Exception {
        Category category = categoryRepository.save(new Category(null, "DailyCat"));
        Product product = productService.create(new Product(null, category, "Late", 1.0, true,
                new Recipe(null, null, Collections.emptyList())));
        Account account = accountRepository.save(new Account(null, "Night", Instant.now()));
        purchaseRepository.save(new Purchase(null, account, product, Instant.parse("2026-06-10T22:30:00Z"), 1, 1.0,
                Collections.emptyList()));

        mvc.perform(get("/api/purchase/stats/daily").param("after", "2026-06-10T00:00:00Z")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].day").value("2026-06-11"));
    }

    private String login() throws Exception {
        String body = mvc.perform(post("/api/auth/login").param("username", "inputadmin").param("password", "secret"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> tokens = JsonParserFactory.getJsonParser().parseMap(body);
        return (String) ((Map<?, ?>) tokens.get("token")).get("token");
    }
}
