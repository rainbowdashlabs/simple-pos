package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.VatRate;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.UserRepository;
import dev.chojo.simplepos.repository.VatRateRepository;
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

import java.math.BigDecimal;
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
 * Checks that staff can read the active VAT rates, that only administrators manage them, that the default rate stays
 * unique and active, and that ingredients take a rate.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VatRateApiTest {
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
    VatRateRepository vatRateRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    EntityManager entityManager;

    private MockMvc mvc;
    private String admin;
    private String staff;
    private VatRate standard;
    private VatRate reduced;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        userRepository.save(new User(null, "vatadmin", passwordEncoder.encode("secret"), true, Role.ADMIN));
        userRepository.save(new User(null, "vatstaff", passwordEncoder.encode("secret"), true, Role.STAFF));
        standard = vatRateRepository.findDefault().orElseThrow();
        reduced = vatRateRepository.findAll().stream().filter(rate -> rate.getName().equals("Reduced")).findFirst().orElseThrow();
        admin = login("vatadmin");
        staff = login("vatstaff");
    }

    @Test
    void migrationSeedsStandardAsDefaultAndReduced() {
        assertEquals(0, new BigDecimal("19.00").compareTo(standard.getRate()));
        assertEquals(0, new BigDecimal("7.00").compareTo(reduced.getRate()));
        assertFalse(reduced.isDefaultRate());
    }

    @Test
    void staffSeesOnlyActiveRates() throws Exception {
        reduced.setActive(false);
        vatRateRepository.saveAndFlush(reduced);

        mvc.perform(get("/api/vat-rates").header("Authorization", "Bearer " + staff))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Standard')].isDefault").value(true))
                .andExpect(jsonPath("$[?(@.name == 'Reduced')]").isEmpty());
    }

    @Test
    void staffCannotManageRates() throws Exception {
        mvc.perform(get("/api/admin/vat-rates").header("Authorization", "Bearer " + staff))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/vat-rates").header("Authorization", "Bearer " + staff)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Zero\", \"rate\": 0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreatesAndRenamesARate() throws Exception {
        String body = mvc.perform(post("/api/admin/vat-rates").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Zero\", \"rate\": 0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isDefault").value(false))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();
        Object id = JsonParserFactory.getJsonParser().parseMap(body).get("id");

        mvc.perform(patch("/api/admin/vat-rates/" + id).header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Exempt\", \"rate\": 0.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Exempt"))
                .andExpect(jsonPath("$.rate").value(0.5));
    }

    @Test
    void namesAreUniqueRegardlessOfCase() throws Exception {
        mvc.perform(post("/api/admin/vat-rates").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"standard\", \"rate\": 5}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("vat_rate_name_taken"));
        mvc.perform(patch("/api/admin/vat-rates/" + reduced.getId()).header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"STANDARD\", \"rate\": 7}"))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidRatesAreRejected() throws Exception {
        for (String rate : new String[]{"-1", "100", "12.345"}) {
            mvc.perform(post("/api/admin/vat-rates").header("Authorization", "Bearer " + admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Odd\", \"rate\": " + rate + "}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[?(@.field == 'rate')]").exists());
        }
    }

    @Test
    void makingARateDefaultMovesTheMark() throws Exception {
        mvc.perform(put("/api/admin/vat-rates/" + reduced.getId() + "/default").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault").value(true));
        entityManager.flush();
        entityManager.clear();
        assertEquals(reduced.getId(), vatRateRepository.findDefault().orElseThrow().getId());
        assertFalse(vatRateRepository.findById(standard.getId()).orElseThrow().isDefaultRate());
    }

    @Test
    void theDefaultRateCannotBeDeactivated() throws Exception {
        mvc.perform(patch("/api/admin/vat-rates/" + standard.getId() + "/active").header("Authorization", "Bearer " + admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("default_vat_rate_active"));
    }

    @Test
    void anInactiveRateCannotBecomeTheDefault() throws Exception {
        mvc.perform(patch("/api/admin/vat-rates/" + reduced.getId() + "/active").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mvc.perform(put("/api/admin/vat-rates/" + reduced.getId() + "/default").header("Authorization", "Bearer " + admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("vat_rate_inactive"));
    }

    @Test
    void ingredientTakesARateAndShowsIt() throws Exception {
        Category category = categoryRepository.save(new Category(null, "VatSnacks"));

        mvc.perform(post("/api/ingredient").header("Authorization", "Bearer " + staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ingredientJson(category, "Gummy bears", reduced.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vatRate.id").value(reduced.getId()))
                .andExpect(jsonPath("$.vatRate.name").value("Reduced"));
    }

    @Test
    void ingredientWithoutRateUsesTheDefault() throws Exception {
        Category category = categoryRepository.save(new Category(null, "VatDrinks"));

        mvc.perform(post("/api/ingredient").header("Authorization", "Bearer " + staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ingredientJson(category, "Lemonade", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vatRate").doesNotExist());
    }

    @Test
    void ingredientKeepsARateDeactivatedSinceButCannotGetANewInactiveOne() throws Exception {
        Category category = categoryRepository.save(new Category(null, "VatMixed"));
        Ingredient kept = new Ingredient(null, category, "Kept", 1.0, 0, 0, 0, 0);
        kept.setVatRate(reduced);
        ingredientRepository.save(kept);
        reduced.setActive(false);
        vatRateRepository.saveAndFlush(reduced);

        mvc.perform(patch("/api/ingredient/" + kept.getId()).header("Authorization", "Bearer " + staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ingredientJson(category, "Kept renamed", reduced.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vatRate.id").value(reduced.getId()));
        mvc.perform(post("/api/ingredient").header("Authorization", "Bearer " + staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ingredientJson(category, "Fresh", reduced.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("vat_rate_inactive"));
    }

    @Test
    void unknownRatesAreNotFound() throws Exception {
        mvc.perform(put("/api/admin/vat-rates/999999/default").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/api/admin/vat-rates/999999/active").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void makingTheDefaultDefaultAgainChangesNothing() throws Exception {
        mvc.perform(put("/api/admin/vat-rates/" + standard.getId() + "/default").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault").value(true));
        entityManager.flush();
        entityManager.clear();
        assertEquals(standard.getId(), vatRateRepository.findDefault().orElseThrow().getId());
    }

    @Test
    void anInactiveRateCanBeTurnedBackOn() throws Exception {
        mvc.perform(patch("/api/admin/vat-rates/" + reduced.getId() + "/active").header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.active").value(false));
        mvc.perform(patch("/api/admin/vat-rates/" + reduced.getId() + "/active").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void namesAreStoredWithoutSurroundingSpaces() throws Exception {
        mvc.perform(post("/api/admin/vat-rates").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \" Standard \", \"rate\": 5}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/admin/vat-rates").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \" Zero \", \"rate\": 0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Zero"));
    }

    @Test
    void ingredientRateCanBeClearedBackToTheDefault() throws Exception {
        Category category = categoryRepository.save(new Category(null, "VatCleared"));
        Ingredient ingredient = new Ingredient(null, category, "Cleared", 1.0, 0, 0, 0, 0);
        ingredient.setVatRate(reduced);
        ingredientRepository.saveAndFlush(ingredient);

        mvc.perform(patch("/api/ingredient/" + ingredient.getId()).header("Authorization", "Bearer " + staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ingredientJson(category, "Cleared", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vatRate").doesNotExist());
    }

    @Test
    void ingredientWithUnknownRateIsABadRequest() throws Exception {
        Category category = categoryRepository.save(new Category(null, "VatLost"));

        mvc.perform(post("/api/ingredient").header("Authorization", "Bearer " + staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ingredientJson(category, "Lost", 999999)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("reference_unknown"))
                .andExpect(jsonPath("$.args.entity").value("VatRate"));
        assertTrue(ingredientRepository.findAll().stream().noneMatch(ingredient -> ingredient.getName().equals("Lost")));
    }

    private static String ingredientJson(Category category, String name, Integer vatRateId) {
        return "{\"name\": \"" + name + "\", \"categoryId\": " + category.getId()
                + ", \"price\": 0.5, \"containerSize\": 0, \"pledge\": 0, \"pledgeContainer\": 0, \"minStock\": 0"
                + ", \"vatRateId\": " + vatRateId + "}";
    }

    private String login(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/login").param("username", username).param("password", "secret"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> tokens = JsonParserFactory.getJsonParser().parseMap(body);
        return (String) ((Map<?, ?>) tokens.get("token")).get("token");
    }
}
