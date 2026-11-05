package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pins the fields the frontend reads from the answers, and that users appear without their credentials.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResponseShapeApiTest {
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
    ProductService productService;

    private MockMvc mvc;
    private Ingredient ingredient;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        userRepository.save(new User(null, "shapeadmin", passwordEncoder.encode("secret"), true, Role.ADMIN));
        Category category = categoryRepository.save(new Category(null, "ShapeCat"));
        ingredient = ingredientRepository.save(new Ingredient(null, category, "ShapeIngredient", 0.5, 6, 0, 0, 0));
        token = login();
    }

    @Test
    void theOwnUserCarriesNoCredentials() throws Exception {
        mvc.perform(get("/api/user/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("shapeadmin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.authorities").doesNotExist())
                .andExpect(jsonPath("$.enabled").doesNotExist());
    }

    @Test
    void tillEntriesNameTheirAuthorOnly() throws Exception {
        send(post("/api/cash"), "{\"amount\": 5, \"type\": \"other\", \"note\": \"shape\"}", 201);

        mvc.perform(get("/api/cash").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.note == 'shape')].type").value("other"))
                .andExpect(jsonPath("$.items[?(@.note == 'shape')].user.username").value("shapeadmin"))
                .andExpect(jsonPath("$.items[?(@.note == 'shape')].user.password").isEmpty())
                .andExpect(jsonPath("$.items[?(@.note == 'shape')].user.role").isEmpty());
    }

    @Test
    void stockAnswersCarryTheIngredient() throws Exception {
        send(post("/api/storage"), "{\"ingredientId\": " + ingredient.getId()
                + ", \"price\": 1, \"pledge\": 0, \"amount\": 4}", 201);

        mvc.perform(get("/api/storage/" + ingredient.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].ingredientId").value(ingredient.getId()))
                .andExpect(jsonPath("$.items[0].price").value(1.0))
                .andExpect(jsonPath("$.items[0].amount").value(4));
        mvc.perform(get("/api/storage/stock/" + ingredient.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredient.name").value("ShapeIngredient"))
                .andExpect(jsonPath("$.ingredient.category.name").value("ShapeCat"))
                .andExpect(jsonPath("$.stock").value(4));
    }

    @Test
    void salesNameTheirProducts() throws Exception {
        Product product = productService.create(new Product(null, null, "ShapeProduct", 2.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 1)))));
        send(post("/api/storage"), "{\"ingredientId\": " + ingredient.getId()
                + ", \"price\": 1, \"pledge\": 0, \"amount\": 4}", 201);
        int accountId = idOf(send(post("/api/account"), "{\"name\": \"ShapeAccount\"}", 201));
        send(post("/api/account/" + accountId + "/balance"), "{\"amount\": 5}", 201);
        send(post("/api/account/" + accountId + "/purchase"),
                "[{\"productId\": " + product.getId() + ", \"amount\": 1}]", 201);

        mvc.perform(get("/api/account/" + accountId + "/purchase").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchases.items[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.products." + product.getId() + ".name").value("ShapeProduct"))
                .andExpect(jsonPath("$.products." + product.getId() + ".category").value((Object) null));
        mvc.perform(get("/api/purchase/stats/top").param("after", "2020-01-01T00:00:00Z")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.product.name == 'ShapeProduct')].product.unitCost").value(1.0))
                .andExpect(jsonPath("$.items[?(@.product.name == 'ShapeProduct')].sales").value(1));
    }

    private String send(MockHttpServletRequestBuilder request, String body, int expectedStatus) throws Exception {
        request.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(body);
        return mvc.perform(request).andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString();
    }

    private static int idOf(String json) {
        return ((Number) JsonParserFactory.getJsonParser().parseMap(json).get("id")).intValue();
    }

    private String login() throws Exception {
        String body = mvc.perform(post("/api/auth/login").param("username", "shapeadmin").param("password", "secret"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> tokens = JsonParserFactory.getJsonParser().parseMap(body);
        return (String) ((Map<?, ?>) tokens.get("token")).get("token");
    }
}
