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
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.UserRepository;
import dev.chojo.simplepos.service.ProductService;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks that the catalogue endpoints only take the fields a client may set, with the id from the path, and reject
 * invalid bodies with 400.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CatalogueApiTest {
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
    ProductRepository productRepository;
    @Autowired
    ProductService productService;
    @Autowired
    EntityManager entityManager;

    private MockMvc mvc;
    private Category category;
    private Ingredient ingredient;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        userRepository.save(new User(null, "catalogueadmin", passwordEncoder.encode("secret"), true, Role.ADMIN));
        category = categoryRepository.save(new Category(null, "ApiCat"));
        ingredient = ingredientRepository.save(new Ingredient(null, category, "ApiIngredient", 0.5, 0, 0, 0, 0));
        token = login();
    }

    @Test
    void updateDoesNotReviveADeletedProduct() throws Exception {
        Product product = product("Deleted");
        product.setDeleted(true);
        productRepository.save(product);
        entityManager.flush();

        mvc.perform(put("/api/product/" + product.getId()).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Deleted", "\"deleted\": false, \"id\": " + product.getId() + ",")))
                .andExpect(status().isNotFound());
        entityManager.clear();
        assertTrue(productRepository.findById(product.getId()).orElseThrow().isDeleted());
    }

    @Test
    void createIgnoresAnIdInTheBody() throws Exception {
        Product existing = product("Existing");
        entityManager.flush();

        mvc.perform(post("/api/product").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Overwrite", "\"id\": " + existing.getId() + ",")))
                .andExpect(status().isCreated());
        entityManager.clear();
        assertEquals("Existing", productRepository.findById(existing.getId()).orElseThrow().getName());
    }

    @Test
    void updateChangesOnlyTheProductOfThePath() throws Exception {
        Product target = product("Target");
        Product bystander = product("Bystander");
        entityManager.flush();

        mvc.perform(put("/api/product/" + target.getId()).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Renamed", "\"id\": " + bystander.getId() + ",")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(target.getId()));
        entityManager.flush();
        entityManager.clear();
        assertEquals("Renamed", productRepository.findById(target.getId()).orElseThrow().getName());
        assertEquals("Bystander", productRepository.findById(bystander.getId()).orElseThrow().getName());
    }

    @Test
    void invalidProductIsRejectedWithTheFields() throws Exception {
        mvc.perform(post("/api/product").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\", \"categoryId\": " + category.getId() + ", \"price\": -1, \"recipe\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'name')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'price')]").exists());
    }

    @Test
    void categoryIsRenamedByPathId() throws Exception {
        Category other = categoryRepository.save(new Category(null, "Untouched"));

        mvc.perform(patch("/api/category/" + category.getId()).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": " + other.getId() + ", \"name\": \"Renamed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Renamed"));
        entityManager.flush();
        entityManager.clear();
        assertEquals("Untouched", categoryRepository.findById(other.getId()).orElseThrow().getName());
    }

    @Test
    void ingredientUpdateKeepsItsActiveState() throws Exception {
        ingredient.setActive(false);
        ingredientRepository.save(ingredient);
        entityManager.flush();

        mvc.perform(patch("/api/ingredient/" + ingredient.getId()).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Changed\", \"categoryId\": " + category.getId()
                                + ", \"price\": 0.7, \"active\": true, \"containerSize\": 6, \"pledge\": 0, \"pledgeContainer\": 0, \"minStock\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Changed"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void ingredientWithMissingFieldsNamesThem() throws Exception {
        mvc.perform(post("/api/ingredient").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Partial\", \"categoryId\": " + category.getId() + ", \"price\": 0.7}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'minStock')]").exists());
    }

    @Test
    void ingredientWithUnknownCategoryIsABadRequest() throws Exception {
        mvc.perform(post("/api/ingredient").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Lost\", \"categoryId\": 999999, \"price\": 0.7, \"containerSize\": 0, \"pledge\": 0, \"pledgeContainer\": 0, \"minStock\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Category 999999 does not exist"));
    }

    @Test
    void createAnswersCreatedAndDeleteAnswersNoContent() throws Exception {
        int categoryId = idOf(send(post("/api/category"), "{\"name\": \"LifecycleCat\"}", 201));
        int ingredientId = idOf(send(post("/api/ingredient"), "{\"name\": \"LifecycleIngr\", \"categoryId\": " + categoryId
                + ", \"price\": 0.5, \"containerSize\": 0, \"pledge\": 0, \"pledgeContainer\": 0, \"minStock\": 0}", 201));
        int productId = idOf(send(post("/api/product"), "{\"name\": \"LifecycleProduct\", \"categoryId\": " + category.getId()
                + ", \"price\": 2.0, \"recipe\": [{\"ingredientId\": " + ingredient.getId() + ", \"amount\": 1}]}", 201));
        int accountId = idOf(send(post("/api/account"), "{\"name\": \"LifecycleAccount\"}", 201));
        send(post("/api/account/" + accountId + "/balance"), "{\"amount\": 5}", 201);
        send(post("/api/storage"), "{\"ingredient\": {\"id\": " + ingredient.getId()
                + "}, \"price\": 1, \"pledge\": 0, \"amount\": 2}", 201);
        String receipt = send(post("/api/account/" + accountId + "/purchase"),
                "[{\"product\": {\"id\": " + productId + "}, \"amount\": 1}]", 201);
        int purchaseId = ((Number) ((List<?>) JsonParserFactory.getJsonParser().parseMap(receipt).get("purchaseIds")).get(0)).intValue();

        send(delete("/api/purchase/" + purchaseId), null, 204);
        send(delete("/api/product/" + productId), null, 204);
        send(delete("/api/ingredient/" + ingredientId), null, 204);
        send(delete("/api/category/" + categoryId), null, 204);
        send(delete("/api/account/" + accountId), null, 204);
    }

    private String send(MockHttpServletRequestBuilder request, String body, int expectedStatus) throws Exception {
        request.header("Authorization", "Bearer " + token);
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(body);
        return mvc.perform(request).andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString();
    }

    private static int idOf(String json) {
        return ((Number) JsonParserFactory.getJsonParser().parseMap(json).get("id")).intValue();
    }

    private Product product(String name) {
        return productService.create(new Product(null, category, name, 2.0, true,
                new Recipe(null, null, List.of(new RecipeEntry(null, ingredient, 1)))));
    }

    private String productJson(String name, String extra) {
        return "{" + extra + "\"name\": \"" + name + "\", \"categoryId\": " + category.getId()
                + ", \"price\": 2.0, \"recipe\": [{\"ingredientId\": " + ingredient.getId() + ", \"amount\": 1}]}";
    }

    private String login() throws Exception {
        String body = mvc.perform(post("/api/auth/login").param("username", "catalogueadmin").param("password", "secret"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> tokens = JsonParserFactory.getJsonParser().parseMap(body);
        return (String) ((Map<?, ?>) tokens.get("token")).get("token");
    }
}
