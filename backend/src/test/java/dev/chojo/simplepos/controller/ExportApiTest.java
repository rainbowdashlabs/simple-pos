package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.AccountRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.repository.StorageRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The CSV exports: byte order mark, separator and decimal mark per language, quoting, defused formulas and
 * business-day boundaries, also across a daylight saving change.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExportApiTest {
    private static final String BOM = "\uFEFF";

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
    CashRepository cashRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    PurchaseRepository purchaseRepository;
    @Autowired
    ProductService productService;

    private MockMvc mvc;
    private User staff;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        staff = userRepository.save(new User(null, "exporter", passwordEncoder.encode("secret"), true, Role.STAFF));
        String body = mvc.perform(post("/api/auth/login").param("username", "exporter").param("password", "secret"))
                .andReturn().getResponse().getContentAsString();
        token = (String) ((Map<?, ?>) JsonParserFactory.getJsonParser().parseMap(body).get("token")).get("token");
    }

    @Test
    void cashKeepsToTheBusinessDayAndDefusesFormulas() throws Exception {
        cash("-3.50", "=SUM(A1)", "2001-03-01T22:30:00Z");
        cash("12.00", "float; morning", "2001-03-01T05:00:00Z");
        cash("1.00", "next day in Berlin", "2001-03-01T23:30:00Z");

        String csv = export("cash", "2001-03-01", "2001-03-01", "de");

        assertEquals(List.of(
                BOM + "time;type;amount;note;user",
                "2001-03-01 06:00:00;other;12,00;\"float; morning\";exporter",
                "2001-03-01 23:30:00;other;-3,50;'=SUM(A1);exporter"
        ), List.of(csv.split("\r\n")));
    }

    @Test
    void salesAndStockListTheirRows() throws Exception {
        Category category = categoryRepository.save(new Category(null, "ExportCat"));
        Product product = productService.create(new Product(null, category, "ExportProduct", 2.5, true,
                new Recipe(null, null, Collections.emptyList())));
        Account account = accountRepository.save(new Account(null, "ExportAccount", Instant.now()));
        Purchase purchase = new Purchase(null, account, product, Instant.parse("2001-03-01T12:00:00Z"), 3, 2.5, List.of());
        purchase.setSoldBy(staff);
        purchaseRepository.save(purchase);
        Ingredient ingredient = ingredientRepository.save(new Ingredient(null, category, "ExportIngredient", 1.0, 20, 0, 0, 0));
        storageRepository.save(Storage.withCost(ingredient, Instant.parse("2001-03-01T12:00:00Z"), new BigDecimal("10.00"), 3));

        Purchase guest = new Purchase(null, null, product, Instant.parse("2001-03-01T14:00:00Z"), 1, 2.5, List.of());
        purchaseRepository.save(guest);

        assertEquals(List.of(
                BOM + "time,account,product,amount,unit_price,total,sold_by",
                "2001-03-01 13:00:00,ExportAccount,ExportProduct,3,2.50,7.50,exporter",
                "2001-03-01 15:00:00,Guest,ExportProduct,1,2.50,2.50,"
        ), List.of(export("sales", "2001-03-01", "2001-03-01", "en").split("\r\n")));
        assertEquals(List.of(
                BOM + "time,ingredient,amount,cost,unit_price",
                "2001-03-01 13:00:00,ExportIngredient,3,10.00,3.3333"
        ), List.of(export("stock", "2001-03-01", "2001-03-01", "en").split("\r\n")));
    }

    @Test
    void theBusinessDayFollowsDaylightSavingAndQuotesAreDoubled() throws Exception {
        cash("1.00", "before the switch", "2001-03-24T23:30:00Z");
        cash("2.00", "say \"hi\"\nnext line", "2001-03-25T21:30:00Z");
        cash("3.00", "already the 26th", "2001-03-25T22:30:00Z");

        assertEquals(List.of(
                BOM + "time;type;amount;note;user",
                "2001-03-25 00:30:00;other;1,00;before the switch;exporter",
                "2001-03-25 23:30:00;other;2,00;\"say \"\"hi\"\"\nnext line\";exporter"
        ), List.of(export("cash", "2001-03-25", "2001-03-25", "de").split("\r\n")));
    }

    @Test
    void aBackwardsOrTooLongRangeIsABadRequest() throws Exception {
        mvc.perform(get("/api/export/cash.csv").header("Authorization", "Bearer " + token)
                        .param("from", "2001-03-02").param("to", "2001-03-01"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/export/cash.csv").header("Authorization", "Bearer " + token)
                        .param("from", "2001-03-01").param("to", "2004-03-02"))
                .andExpect(status().isBadRequest());
    }

    private void cash(String amount, String note, String created) {
        Cash cash = new Cash(staff, new BigDecimal(amount), CashType.OTHER, note);
        cash.setCreated(Instant.parse(created));
        cashRepository.save(cash);
    }

    private String export(String name, String from, String to, String language) throws Exception {
        byte[] body = mvc.perform(get("/api/export/" + name + ".csv").header("Authorization", "Bearer " + token)
                        .param("from", from).param("to", to).param("language", language))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"%s_%s_%s.csv\"".formatted(name, from, to)))
                .andReturn().getResponse().getContentAsByteArray();
        return new String(body, StandardCharsets.UTF_8);
    }
}
