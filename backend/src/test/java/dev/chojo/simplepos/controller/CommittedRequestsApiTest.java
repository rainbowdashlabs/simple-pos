package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.configuration.security.RefreshCookie;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.UserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Walks every endpoint but the demo ones with real data and no test transaction around the requests, as the
 * application runs. Each request then has only the transactions the application opens itself, so a response that
 * reads a relation outside of them fails here instead of in production.
 * <p>
 * Because this test commits, the context is discarded afterwards; the next context cleans and migrates the schema
 * afresh, so no data leaks into other tests.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CommittedRequestsApiTest {
    private static final String AFTER = "2020-01-01T00:00:00Z";

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
    private String token;
    private Cookie refreshCookie;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        userRepository.save(new User(null, "committedadmin", passwordEncoder.encode("secret"), true, Role.ADMIN));
        MvcResult login = mvc.perform(post("/api/auth/login").param("username", "committedadmin").param("password", "secret"))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> answer = JsonParserFactory.getJsonParser().parseMap(login.getResponse().getContentAsString());
        token = (String) ((Map<?, ?>) answer.get("token")).get("token");
        refreshCookie = login.getResponse().getCookie(RefreshCookie.NAME);
    }

    @Test
    void everyEndpointAnswersWithoutATestTransaction() throws Exception {
        int category = idOf(send(post("/api/category"), "{\"name\": \"CommittedCat\"}", 201));
        int ingredient = idOf(send(post("/api/ingredient"), ingredientJson("CommittedIngredient", category), 201));
        int product = idOf(send(post("/api/product"), "{\"name\": \"CommittedProduct\", \"categoryId\": " + category
                + ", \"price\": 2.0, \"recipe\": [{\"ingredientId\": " + ingredient + ", \"amount\": 1}]}", 201));
        send(post("/api/storage"), "{\"ingredientId\": " + ingredient + ", \"price\": 0.5, \"pledge\": 0, \"amount\": 12}", 201);
        int account = idOf(send(post("/api/account"), "{\"name\": \"CommittedAccount\"}", 201));
        send(post("/api/account/" + account + "/balance"), "{\"amount\": 10}", 201);
        int sold = firstPurchase(send(post("/api/account/" + account + "/purchase"),
                "[{\"productId\": " + product + ", \"amount\": 2}]", 201));
        int soldToGuest = firstPurchase(send(post("/api/purchase/guest"),
                "[{\"productId\": " + product + ", \"amount\": 1}]", 201));
        send(post("/api/cash"), "{\"amount\": 5, \"type\": \"other\", \"note\": \"committed\"}", 201);
        send(post("/api/cash/pledge/return"), "[{\"ingredientId\": " + ingredient + ", \"bottles\": 1, \"crates\": 0}]", 201);
        send(post("/api/cash/count"), "{\"denominations\": {\"200\": 3}}", 201);

        send(patch("/api/category/" + category), "{\"name\": \"CommittedCategory\"}", 200);
        send(patch("/api/ingredient/" + ingredient + "/active"), null, 200);
        send(patch("/api/ingredient/" + ingredient + "/active"), null, 200);
        send(patch("/api/product/" + product + "/active"), null, 200);
        send(patch("/api/product/" + product + "/active"), null, 200);
        send(put("/api/product/" + product), "{\"name\": \"CommittedProduct\", \"categoryId\": " + category
                + ", \"price\": 2.5, \"recipe\": [{\"ingredientId\": " + ingredient + ", \"amount\": 1}]}", 200);
        send(patch("/api/account/" + account), "{\"name\": \"CommittedRenamed\"}", 200);
        send(put("/api/storage"), "[{\"ingredient\": " + ingredient + ", \"amount\": 9}]", 200);
        int spareCategory = idOf(send(post("/api/category"), "{\"name\": \"SpareCat\"}", 201));
        int spareIngredient = idOf(send(post("/api/ingredient"), ingredientJson("SpareIngredient", spareCategory), 201));
        send(patch("/api/ingredient/" + spareIngredient), ingredientJson("SpareRenamed", spareCategory), 200);
        send(delete("/api/ingredient/" + spareIngredient), null, 204);
        send(delete("/api/category/" + spareCategory), null, 204);
        send(put("/api/user/preferences"), "{\"locale\": \"en\", \"colorTheme\": \"blue\", \"darkMode\": \"dark\"}", 200);
        int staff = idOf(send(post("/api/admin/users"),
                "{\"username\": \"committedstaff\", \"password\": \"staff-secret\", \"role\": \"STAFF\"}", 201));
        send(patch("/api/admin/users/" + staff), "{\"enabled\": false}", 200);
        send(put("/api/admin/users/" + staff + "/password"), "{\"password\": \"other-secret\"}", 204);

        String from = LocalDate.now().minusDays(1).toString();
        String to = LocalDate.now().plusDays(1).toString();
        for (String path : List.of(
                "/api/category", "/api/category/" + category,
                "/api/ingredient", "/api/ingredient/" + ingredient,
                "/api/product", "/api/product/active", "/api/product/" + product,
                "/api/product/" + product + "/sales/stat", "/api/product/" + product + "/sales/stat/summary?after=" + AFTER,
                "/api/account", "/api/account/recent", "/api/account/" + account,
                "/api/account/" + account + "/balance", "/api/account/" + account + "/purchase",
                "/api/storage", "/api/storage/" + ingredient, "/api/storage/stock", "/api/storage/stock/" + ingredient,
                "/api/storage/shopping-list", "/api/storage/stock/projection", "/api/storage/stock/low",
                "/api/storage/stock/out",
                "/api/cash", "/api/cash/total", "/api/cash/count", "/api/cash/total/balance", "/api/cash/total/pledge",
                "/api/purchase/stats/total?after=" + AFTER, "/api/purchase/stats/top?after=" + AFTER,
                "/api/purchase/stats/bottom?after=" + AFTER, "/api/purchase/stats/summary?after=" + AFTER,
                "/api/purchase/stats/daily?after=" + AFTER, "/api/purchase/stats/categories?after=" + AFTER,
                "/api/export/sales.csv?from=" + from + "&to=" + to, "/api/export/cash.csv?from=" + from + "&to=" + to,
                "/api/export/stock.csv?from=" + from + "&to=" + to,
                "/api/user/me", "/api/user/preferences", "/api/admin/users", "/api/version")) {
            send(get(path), null, 200);
        }

        send(post("/api/purchase/cancel"), "{\"ids\": [" + soldToGuest + "]}", 204);
        send(delete("/api/purchase/" + sold), null, 204);
        send(delete("/api/product/" + product), null, 204);
        send(delete("/api/account/" + account), null, 204);

        refreshCookie = mvc.perform(post("/api/auth/refresh").cookie(refreshCookie)
                        .header(RefreshCookie.CSRF_HEADER, RefreshCookie.CSRF_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie(RefreshCookie.NAME);
        refreshCookie = mvc.perform(put("/api/user/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"current\": \"secret\", \"password\": \"new-secret\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie(RefreshCookie.NAME);
        mvc.perform(post("/api/auth/logout").cookie(refreshCookie).header(RefreshCookie.CSRF_HEADER, RefreshCookie.CSRF_VALUE))
                .andExpect(status().isNoContent());
    }

    private static String ingredientJson(String name, int category) {
        return "{\"name\": \"" + name + "\", \"categoryId\": " + category + ", \"price\": 0.5, \"containerSize\": 6,"
                + " \"pledge\": 0.25, \"pledgeContainer\": 1.5, \"minStock\": 50}";
    }

    private String send(MockHttpServletRequestBuilder request, String body, int expectedStatus) throws Exception {
        request.header("Authorization", "Bearer " + token);
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(body);
        return mvc.perform(request).andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString();
    }

    private static int idOf(String json) {
        return ((Number) JsonParserFactory.getJsonParser().parseMap(json).get("id")).intValue();
    }

    private static int firstPurchase(String receipt) {
        List<?> ids = (List<?>) JsonParserFactory.getJsonParser().parseMap(receipt).get("purchaseIds");
        return ((Number) ids.get(0)).intValue();
    }
}
