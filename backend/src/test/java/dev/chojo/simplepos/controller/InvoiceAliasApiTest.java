package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.InvoiceAlias;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.InvoiceAliasRepository;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks that booked invoice lines are remembered under their printed text or GTIN, that the latest booking wins, and
 * that the search finds them by GTIN or text prefix regardless of spacing and case.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InvoiceAliasApiTest {
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
    CategoryRepository categoryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    InvoiceAliasRepository aliasRepository;
    @Autowired
    EntityManager entityManager;

    private MockMvc mvc;
    private String token;
    private Ingredient ale;
    private Ingredient wheat;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        userRepository.save(new User(null, "aliasstaff", passwordEncoder.encode("secret"), true, Role.STAFF));
        Category category = categoryRepository.save(new Category(null, "AliasDrinks"));
        ale = ingredientRepository.save(new Ingredient(null, category, "Atlantik Ale", 1.0, 20, 0.08, 1.50, 0));
        wheat = ingredientRepository.save(new Ingredient(null, category, "Bernstein Weizen", 1.0, 20, 0.08, 1.50, 0));
        token = login("aliasstaff");
    }

    @Test
    void bookedLinesAreFoundByTextPrefixIgnoringSpacingAndCase() throws Exception {
        book(item(ale, "STÖRTEB.ATL.ALE  ALKFR.MW 0,5L", null, "CONTAINER", "19.00"),
                "{\"kind\": \"PLEDGE\", \"text\": \"LG 3,10 EUR 20 FL.0,08 + KISTE\", \"quantity\": 1, \"price\": 3.10}")
                .andExpect(status().isCreated());

        search("  störteb.atl ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("STÖRTEB.ATL.ALE ALKFR.MW 0,5L"))
                .andExpect(jsonPath("$[0].kind").value("ITEM"))
                .andExpect(jsonPath("$[0].ingredient.id").value(ale.getId()))
                .andExpect(jsonPath("$[0].unit").value("CONTAINER"))
                .andExpect(jsonPath("$[0].lastPrice").value(19.00));
        search("lg 3,10")
                .andExpect(jsonPath("$[0].kind").value("PLEDGE"))
                .andExpect(jsonPath("$[0].ingredient").doesNotExist())
                .andExpect(jsonPath("$[0].lastPrice").value(3.10));
    }

    @Test
    void theLatestBookingWins() throws Exception {
        book(item(ale, "BEER OF THE MONTH", null, "CONTAINER", "18.00")).andExpect(status().isCreated());
        book(item(wheat, "beer of the month", null, "PIECE", "0.95")).andExpect(status().isCreated());

        search("BEER OF")
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ingredient.id").value(wheat.getId()))
                .andExpect(jsonPath("$[0].unit").value("PIECE"))
                .andExpect(jsonPath("$[0].lastPrice").value(0.95));
    }

    @Test
    void gtinIdentifiesTheAliasAndComesFirst() throws Exception {
        book(item(ale, "4014807 SOMETHING", null, "PIECE", "1.00")).andExpect(status().isCreated());
        book(item(wheat, "OLD NAME", "4014807211060", "PIECE", "0.95")).andExpect(status().isCreated());
        book(item(wheat, "NEW NAME", "4014807211060", "PIECE", "0.97")).andExpect(status().isCreated());
        entityManager.flush();

        assertEquals(1, aliasRepository.findAll().stream().filter(alias -> "4014807211060".equals(alias.getGtin())).count());
        search("4014807211060")
                .andExpect(jsonPath("$[0].gtin").value("4014807211060"))
                .andExpect(jsonPath("$[0].text").value("NEW NAME"))
                .andExpect(jsonPath("$[0].lastPrice").value(0.97));
        search("4014807")
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("4014807 SOMETHING"));
    }

    @Test
    void wildcardsInTheQueryAreTakenLiterally() throws Exception {
        book(item(ale, "100% JUICE", null, "PIECE", "1.00"), item(wheat, "100 JUICE", null, "PIECE", "1.00"))
                .andExpect(status().isCreated());

        search("100%")
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("100% JUICE"));
        search("_00").andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void linesWithoutTextOrGtinAreNotRemembered() throws Exception {
        long before = aliasRepository.count();
        book(item(ale, "   ", null, "PIECE", "1.00")).andExpect(status().isCreated());
        entityManager.flush();

        assertEquals(before, aliasRepository.count());
        search("   ").andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void discountsCarryNoNameAndGtinsAreDigits() throws Exception {
        book(item(ale, "ALE", null, "PIECE", "1.00"),
                "{\"kind\": \"DISCOUNT\", \"text\": \"Sonderrabatt\", \"amount\": 0.10, \"discountOf\": 0}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invoice_position_unexpected"))
                .andExpect(jsonPath("$.args.field").value("text"));
        book(item(ale, "ALE", "40148X", "PIECE", "1.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").exists());
    }

    @Test
    void aTextAliasTakesTheGtinALaterLineBrings() throws Exception {
        book(item(ale, "ALE 0,5L", null, "PIECE", "1.00")).andExpect(status().isCreated());
        book(item(ale, "ale 0,5l", "4014807210902", "PIECE", "1.05")).andExpect(status().isCreated());
        entityManager.flush();

        search("ALE 0,5L")
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].gtin").value("4014807210902"))
                .andExpect(jsonPath("$[0].lastPrice").value(1.05));
    }

    @Test
    void theLastPriceKeepsWhetherItWasNet() throws Exception {
        mvc.perform(post("/api/storage/invoice").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pricesNet\": true, \"positions\": [%s]}".formatted(item(ale, "NET ALE", null, "PIECE", "0.95"))))
                .andExpect(status().isCreated());

        search("NET ALE").andExpect(jsonPath("$[0].lastPriceNet").value(true));
        book(item(ale, "NET ALE", null, "PIECE", "1.13")).andExpect(status().isCreated());
        search("NET ALE").andExpect(jsonPath("$[0].lastPriceNet").value(false));
    }

    @Test
    void exclamationMarksInTheQueryAreTakenLiterally() throws Exception {
        book(item(ale, "WOW! ALE", null, "PIECE", "1.00"), item(wheat, "WOW ALE", null, "PIECE", "1.00"))
                .andExpect(status().isCreated());

        search("wow!").andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].text").value("WOW! ALE"));
    }

    @Test
    void onlyStaffSearchesAndTheQueryIsBounded() throws Exception {
        userRepository.save(new User(null, "aliasuser", passwordEncoder.encode("secret"), true, Role.USER));

        mvc.perform(get("/api/storage/invoice/aliases").param("q", "ale").header("Authorization", "Bearer " + login("aliasuser")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/storage/invoice/aliases").param("q", "ale"))
                .andExpect(status().isUnauthorized());
        search("x".repeat(201)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/storage/invoice/aliases").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void normalizeCollapsesSpacesAndUpperCases() {
        assertEquals("A B C", InvoiceAlias.normalize("  a \t b\n c "));
        assertEquals("A B", InvoiceAlias.normalize("a\u00a0b"));
        assertNull(InvoiceAlias.normalize("   "));
        assertNull(InvoiceAlias.normalize(null));
    }

    private static String item(Ingredient ingredient, String text, String gtin, String unit, String price) {
        return "{\"kind\": \"ITEM\", \"ingredientId\": %d, \"text\": \"%s\", %s\"quantity\": 1, \"unit\": \"%s\", \"price\": %s}"
                .formatted(ingredient.getId(), text, gtin == null ? "" : "\"gtin\": \"" + gtin + "\", ", unit, price);
    }

    private ResultActions book(String... positions) throws Exception {
        return mvc.perform(post("/api/storage/invoice").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pricesNet\": false, \"positions\": [%s]}".formatted(String.join(",", positions))));
    }

    private ResultActions search(String query) throws Exception {
        entityManager.flush();
        return mvc.perform(get("/api/storage/invoice/aliases").param("q", query).header("Authorization", "Bearer " + token));
    }

    private String login(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/login").param("username", username).param("password", "secret"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> tokens = JsonParserFactory.getJsonParser().parseMap(body);
        return (String) ((Map<?, ?>) tokens.get("token")).get("token");
    }
}
