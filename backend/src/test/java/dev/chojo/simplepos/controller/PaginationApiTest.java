package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Paged lists: the page shape and totals, the account search, and an order that neither repeats nor loses an entry
 * from one page to the next when names repeat.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaginationApiTest {
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
    CategoryRepository categoryRepository;
    @Autowired
    ProductService productService;

    private MockMvc mvc;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        userRepository.save(new User(null, "pager", passwordEncoder.encode("secret"), true, Role.ADMIN));
        String body = mvc.perform(post("/api/auth/login").param("username", "pager").param("password", "secret"))
                .andReturn().getResponse().getContentAsString();
        token = (String) ((Map<?, ?>) JsonParserFactory.getJsonParser().parseMap(body).get("token")).get("token");
    }

    @Test
    void aPageCarriesItsPositionAndTheTotals() throws Exception {
        for (String name : List.of("PagedCatA", "PagedCatB", "PagedCatC")) categoryRepository.save(new Category(null, name));
        long categories = categoryRepository.count();

        mvc.perform(authorized(get("/api/category")).param("limit", "2").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalItems").value(categories))
                .andExpect(jsonPath("$.totalPages").value((categories + 1) / 2));
    }

    @Test
    void outOfRangePagesAreBadRequests() throws Exception {
        for (String[] params : new String[][]{{"limit", "0"}, {"limit", "501"}, {"page", "-1"}}) {
            mvc.perform(authorized(get("/api/account")).param(params[0], params[1]))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void accountsAreSearchedByPartOfTheNameIgnoringCaseAndWildcards() throws Exception {
        accountRepository.save(new Account(null, "Searchable Sabine", Instant.now()));
        accountRepository.save(new Account(null, "Other Otto", Instant.now()));
        accountRepository.save(new Account(null, "Discount 100% Dora", Instant.now()));

        assertEquals(List.of("Searchable Sabine"), names(authorized(get("/api/account")).param("q", "SABINE")));
        assertEquals(List.of("Discount 100% Dora"), names(authorized(get("/api/account")).param("q", "100%")));
        assertEquals(List.of(), names(authorized(get("/api/account")).param("q", "_tto")));
    }

    @Test
    void aTypedOutShortNameComesFirstAndOddInputIsHandled() throws Exception {
        for (String name : List.of("Alice", "Amelie", "Charlie", "Li")) accountRepository.save(new Account(null, name, Instant.now()));
        accountRepository.save(new Account(null, "Bang!Bang", Instant.now()));

        assertEquals("Li", names(authorized(get("/api/account")).param("q", "li").param("limit", "1")).get(0));
        assertEquals(List.of("Bang!Bang"), names(authorized(get("/api/account")).param("q", "g!b")));
        mvc.perform(authorized(get("/api/account")).param("q", "x".repeat(101))).andExpect(status().isBadRequest());
    }

    @Test
    void countsAreRightWhenMoreRowsExistThanOnePageHolds() throws Exception {
        Category category = categoryRepository.save(new Category(null, "CountedCat"));
        Product first = productService.create(new Product(null, category, "Counted A", 1.0, true, new Recipe(null, null, Collections.emptyList())));
        Product second = productService.create(new Product(null, category, "Counted B", 1.0, true, new Recipe(null, null, Collections.emptyList())));
        Account buyer = accountRepository.save(new Account(null, "Counter", Instant.now()));
        for (Product product : List.of(first, second)) {
            mvc.perform(authorized(post("/api/account/" + buyer.getId() + "/purchase"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[{\"productId\": " + product.getId() + ", \"amount\": 1}]"))
                    .andExpect(status().isCreated());
        }

        String top = read(authorized(get("/api/purchase/stats/top")).param("after", "2020-01-01T00:00:00Z").param("limit", "1"));
        assertTrue(((Number) JsonParserFactory.getJsonParser().parseMap(top).get("totalItems")).intValue() >= 2);
        mvc.perform(authorized(get("/api/product/active")).param("categoryId", "0").param("limit", "1"))
                .andExpect(status().isOk());
        mvc.perform(authorized(get("/api/ingredient")).param("categoryId", "0").param("limit", "1"))
                .andExpect(status().isOk());
        mvc.perform(authorized(get("/api/storage/stock")).param("limit", "1"))
                .andExpect(status().isOk());
        mvc.perform(authorized(get("/api/ingredient")).param("limit", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void pagesNeitherRepeatNorLoseEntriesWithEqualNames() throws Exception {
        List<Integer> created = new ArrayList<>();
        for (int i = 0; i < 4; i++) created.add(accountRepository.save(new Account(null, "Twin", Instant.now())).getId());

        List<Integer> seen = new ArrayList<>();
        for (int page = 0; page < 4; page++) {
            seen.addAll(ids(authorized(get("/api/account")).param("q", "twin").param("limit", "1").param("page", String.valueOf(page))));
        }

        assertEquals(new HashSet<>(created), new HashSet<>(seen));
        assertEquals(4, seen.size());
    }

    @Test
    void productsArePagedPerCategoryInTheirOrder() throws Exception {
        Category category = categoryRepository.save(new Category(null, "PagedProducts"));
        for (String name : List.of("Zitrone", "Apfel", "Mango")) {
            productService.create(new Product(null, category, name, 1.0, true, new Recipe(null, null, Collections.emptyList())));
        }

        String body = mvc.perform(authorized(get("/api/product/active")).param("categoryId", String.valueOf(category.getId()))
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(3))
                .andReturn().getResponse().getContentAsString();

        assertEquals(List.of("Apfel", "Mango"), itemsOf(body).stream().map(item -> item.get("name")).toList());
    }

    @Test
    void productsWithoutACategoryAreAskedForAsCategoryZero() throws Exception {
        Product loose = productService.create(new Product(null, null, "Uncategorised Juice", 1.0, true,
                new Recipe(null, null, Collections.emptyList())));

        String body = read(authorized(get("/api/product/active")).param("categoryId", "0").param("limit", "500"));

        assertTrue(itemsOf(body).stream().anyMatch(item -> loose.getId().equals(((Number) item.get("id")).intValue())));
        assertTrue(itemsOf(body).stream().allMatch(item -> item.get("category") == null));
    }

    private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + token);
    }

    private List<Object> names(MockHttpServletRequestBuilder request) throws Exception {
        return itemsOf(read(request)).stream().map(item -> (Object) item.get("name")).toList();
    }

    private List<Integer> ids(MockHttpServletRequestBuilder request) throws Exception {
        return itemsOf(read(request)).stream().map(item -> ((Number) item.get("id")).intValue()).toList();
    }

    private String read(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private static List<Map<?, ?>> itemsOf(String body) {
        return ((List<?>) JsonParserFactory.getJsonParser().parseMap(body).get("items")).stream()
                .<Map<?, ?>>map(item -> (Map<?, ?>) item)
                .toList();
    }
}
