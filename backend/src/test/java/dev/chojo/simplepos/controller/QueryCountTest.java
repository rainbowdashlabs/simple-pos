package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.configuration.DemoDataSeeder;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.repository.UserRepository;
import dev.chojo.simplepos.service.ProductService;
import dev.chojo.simplepos.service.PurchaseService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.Filter;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The listing and statistics endpoints run a fixed, small number of SQL statements on the demo data, however many
 * products, ingredients and purchases there are: associations are loaded with their owners, not one by one.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class QueryCountTest {
    @Autowired
    WebApplicationContext context;
    @Autowired
    @Qualifier("springSecurityFilterChain")
    Filter springSecurityFilterChain;
    @Autowired
    EntityManagerFactory entityManagerFactory;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    EntityManager entityManager;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    ProductService productService;
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    BalanceRepository balanceRepository;
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    CashRepository cashRepository;
    @Autowired
    ProductRepository productRepository;

    private static final long MAX_STATEMENTS = 5;

    private MockMvc mvc;
    private Statistics statistics;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        new DemoDataSeeder(entityManager, userRepository, passwordEncoder, categoryRepository, ingredientRepository,
                productService, storageRepository, accountRepository, balanceRepository, purchaseService,
                cashRepository, true).run();
        userRepository.save(new User(null, "countadmin", passwordEncoder.encode("secret"), true, Role.ADMIN));
        entityManager.flush();
        entityManager.clear();
        String body = mvc.perform(post("/api/auth/login").param("username", "countadmin").param("password", "secret"))
                .andReturn().getResponse().getContentAsString();
        token = (String) ((Map<?, ?>) JsonParserFactory.getJsonParser().parseMap(body).get("token")).get("token");
    }

    @Test
    void listingsDoNotLoadAssociationsOneByOne() throws Exception {
        int account = accountRepository.findAll().get(0).getId();
        int product = productRepository.findAll().get(0).getId();
        List<String> urls = List.of("/api/product", "/api/product/active", "/api/ingredient", "/api/category",
                "/api/storage/stock", "/api/storage/stock/low", "/api/storage/stock/out", "/api/storage/stock/projection",
                "/api/storage", "/api/purchase/stats/top?after=2020-01-01T00:00:00Z",
                "/api/purchase/stats/bottom?after=2020-01-01T00:00:00Z", "/api/purchase/stats/daily?after=2020-01-01T00:00:00Z",
                "/api/purchase/stats/categories?after=2020-01-01T00:00:00Z", "/api/account", "/api/cash",
                "/api/account/" + account + "/purchase", "/api/product/" + product + "/sales/stat");
        List<String> tooMany = new ArrayList<>();
        for (String url : urls) {
            entityManager.clear();
            statistics.clear();
            mvc.perform(get(url).header("Authorization", "Bearer " + token)).andExpect(status().isOk());
            long statements = statistics.getPrepareStatementCount();
            if (statements > MAX_STATEMENTS) tooMany.add(url + " ran " + statements);
        }
        assertTrue(tooMany.isEmpty(), () -> String.join(", ", tooMany));
    }
}
