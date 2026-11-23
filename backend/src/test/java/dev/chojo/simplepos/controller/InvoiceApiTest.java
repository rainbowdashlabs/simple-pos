package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.VatRate;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.StorageRepository;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static dev.chojo.simplepos.MoneyAssertions.assertMoney;
import static dev.chojo.simplepos.MoneyAssertions.assertUnitPrice;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Books invoices through the API: a supermarket receipt with gross prices, pledge lines and discounts, and a
 * wholesale invoice with net prices, and checks the batches, cash entries, VAT handling and refused invoices.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InvoiceApiTest {
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
    StorageRepository storageRepository;
    @Autowired
    CashRepository cashRepository;
    @Autowired
    VatRateRepository vatRateRepository;
    @Autowired
    EntityManager entityManager;

    private MockMvc mvc;
    private String token;
    private Category category;
    private VatRate reduced;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        userRepository.save(new User(null, "invoicestaff", passwordEncoder.encode("secret"), true, Role.STAFF));
        category = categoryRepository.save(new Category(null, "InvoiceDrinks"));
        reduced = vatRateRepository.findAll().stream().filter(rate -> rate.getName().equals("Reduced")).findFirst().orElseThrow();
        userRepository.save(new User(null, "invoiceuser", passwordEncoder.encode("secret"), true, Role.USER));
        token = login("invoicestaff");
    }

    @Test
    void supermarketReceiptBooksItemsPledgeAndDiscounts() throws Exception {
        Ingredient pils = ingredient("Radeberger Pils", 20);
        Ingredient spezi = ingredient("Paulaner Spezi Zero", 20);
        Ingredient alcoholFree = ingredient("Radeberger Alkoholfrei", 20);

        book("""
                {"supplier": "Netto", "pricesNet": false, "positions": [
                  %s,
                  {"kind": "PLEDGE", "text": "Traeger voll", "quantity": 2, "price": 3.10},
                  {"kind": "DISCOUNT", "amount": 2.40, "discountOf": 0},
                  %s,
                  {"kind": "PLEDGE", "text": "Traeger voll", "quantity": 2, "price": 3.10},
                  %s,
                  {"kind": "PLEDGE", "text": "Traeger voll", "quantity": 1, "price": 3.10},
                  {"kind": "DISCOUNT", "amount": 1.20, "discountOf": 5}
                ]}""".formatted(item(pils, 2, "CONTAINER", "10.99"), item(spezi, 2, "CONTAINER", "9.99"),
                item(alcoholFree, 1, "CONTAINER", "10.99")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goods").value(49.35))
                .andExpect(jsonPath("$.pledge").value(15.50))
                .andExpect(jsonPath("$.total").value(64.85))
                .andExpect(jsonPath("$.batches.length()").value(3))
                .andExpect(jsonPath("$.batches[0].amount").value(40))
                .andExpect(jsonPath("$.batches[0].cost").value(19.58))
                .andExpect(jsonPath("$.batches[1].cost").value(19.98))
                .andExpect(jsonPath("$.batches[2].amount").value(20))
                .andExpect(jsonPath("$.batches[2].cost").value(9.79));

        List<Cash> entries = cashFor("Invoice Netto");
        assertEquals(2, entries.size());
        assertMoney(-49.35, entry(entries, CashType.PURCHASE).getAmount());
        assertMoney(-15.50, entry(entries, CashType.PLEDGE).getAmount());
        entityManager.flush();
        entityManager.clear();
        assertUnitPrice(0.4895, ingredientRepository.findById(pils.getId()).orElseThrow().getPrice());
    }

    @Test
    void wholesaleInvoiceAddsVatToNetPricesPerLine() throws Exception {
        Ingredient ale = ingredient("Störtebeker Atlantik Ale alkoholfrei", 20);
        Ingredient wheat = ingredient("Störtebeker Bernstein-Weizen alkoholfrei", 20);

        book("""
                {"supplier": "Getränkegroßhandel", "pricesNet": true, "positions": [
                  %s,
                  {"kind": "PLEDGE", "text": "LG 3,10 EUR 20 FL.0,08 + KISTE", "quantity": 1, "price": 3.10},
                  %s,
                  {"kind": "PLEDGE", "text": "LG 3,10 EUR 20 FL.0,08 + KISTE", "quantity": 1, "price": 3.10}
                ]}""".formatted(item(ale, 20, "PIECE", "0.95"), item(wheat, 20, "PIECE", "0.95")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batches[0].cost").value(22.61))
                .andExpect(jsonPath("$.batches[1].cost").value(22.61))
                .andExpect(jsonPath("$.pledge").value(7.38))
                .andExpect(jsonPath("$.total").value(52.60));
    }

    @Test
    void netItemUsesItsIngredientsRate() throws Exception {
        Ingredient candy = ingredient("Gummy bears", 0);
        candy.setVatRate(reduced);
        ingredientRepository.saveAndFlush(candy);

        book(invoice(true, item(candy, 10, "PIECE", "1.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goods").value(10.70));
    }

    @Test
    void discountOnANetItemGetsTheItemsRate() throws Exception {
        Ingredient candy = ingredient("Lollipops", 0);
        candy.setVatRate(reduced);
        ingredientRepository.saveAndFlush(candy);

        book(invoice(true, item(candy, 10, "PIECE", "1.00"), "{\"kind\": \"DISCOUNT\", \"amount\": 1.00, \"discountOf\": 0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goods").value(9.63));
    }

    @Test
    void lineRateCanBecomeTheIngredientsRate() throws Exception {
        Ingredient chips = ingredient("Chips", 0);

        book(invoice(true, """
                {"kind": "ITEM", "ingredientId": %d, "quantity": 5, "unit": "PIECE", "price": 1.00,
                 "vatRateId": %d, "updateIngredientRate": true}""".formatted(chips.getId(), reduced.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goods").value(5.35));
        entityManager.flush();
        entityManager.clear();
        assertEquals(reduced.getId(), ingredientRepository.findById(chips.getId()).orElseThrow().getVatRate().getId());
    }

    @Test
    void choosingTheDefaultRateClearsTheIngredientsRate() throws Exception {
        Ingredient soda = ingredient("Soda", 0);
        soda.setVatRate(reduced);
        ingredientRepository.saveAndFlush(soda);
        VatRate standard = vatRateRepository.findDefault().orElseThrow();

        book(invoice(false, """
                {"kind": "ITEM", "ingredientId": %d, "quantity": 1, "unit": "PIECE", "price": 1.00,
                 "vatRateId": %d, "updateIngredientRate": true}""".formatted(soda.getId(), standard.getId())))
                .andExpect(status().isCreated());
        entityManager.flush();
        entityManager.clear();
        assertNull(ingredientRepository.findById(soda.getId()).orElseThrow().getVatRate());
    }

    @Test
    void returnedEmptiesBookPledgeBack() throws Exception {
        Ingredient water = ingredient("Water", 12);

        book(invoice(false, item(water, 1, "CONTAINER", "3.00"),
                "{\"kind\": \"PLEDGE\", \"label\": \"Leergut\", \"quantity\": 2, \"price\": -3.30}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pledge").value(-6.60))
                .andExpect(jsonPath("$.total").value(-3.60));
        assertMoney(6.60, entry(cashFor("Invoice"), CashType.PLEDGE).getAmount());
    }

    @Test
    void invoiceWithoutItemIsRefused() throws Exception {
        book(invoice(false, "{\"kind\": \"PLEDGE\", \"quantity\": 1, \"price\": 3.10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invoice_without_item"));
    }

    @Test
    void itemWithoutUnitNamesTheField() throws Exception {
        Ingredient cola = ingredient("Cola", 24);

        book(invoice(false, "{\"kind\": \"ITEM\", \"ingredientId\": %d, \"quantity\": 1, \"price\": 1}".formatted(cola.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invoice_position_incomplete"))
                .andExpect(jsonPath("$.args.position").value("1"))
                .andExpect(jsonPath("$.args.field").value("unit"));
    }

    @Test
    void containersOfAnIngredientWithoutContainerSizeAreRefused() throws Exception {
        Ingredient nachos = ingredient("Nachos", 0);

        book(invoice(false, item(nachos, 1, "CONTAINER", "2.00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("container_size_missing"));
    }

    @Test
    void discountMustPointAtAnItem() throws Exception {
        Ingredient beer = ingredient("Beer", 20);

        book(invoice(false, item(beer, 1, "CONTAINER", "10.00"),
                "{\"kind\": \"PLEDGE\", \"quantity\": 1, \"price\": 3.10}",
                "{\"kind\": \"DISCOUNT\", \"amount\": 1.00, \"discountOf\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("discount_target_invalid"))
                .andExpect(jsonPath("$.args.position").value("3"));
        book(invoice(false, item(beer, 1, "CONTAINER", "10.00"),
                "{\"kind\": \"DISCOUNT\", \"amount\": 1.00, \"discountOf\": 7}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("discount_target_invalid"));
    }

    @Test
    void discountLargerThanItsItemIsRefused() throws Exception {
        Ingredient beer = ingredient("Cheap beer", 20);
        long batches = storageRepository.count();

        book(invoice(false, item(beer, 1, "CONTAINER", "5.00"),
                "{\"kind\": \"DISCOUNT\", \"amount\": 6.00, \"discountOf\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("discount_too_large"))
                .andExpect(jsonPath("$.args.position").value("1"));
        assertEquals(batches, storageRepository.count());
        assertTrue(cashFor("Invoice").isEmpty());
    }

    @Test
    void inactiveLineRateIsRefused() throws Exception {
        Ingredient tea = ingredient("Tea", 0);
        reduced.setActive(false);
        vatRateRepository.saveAndFlush(reduced);

        book(invoice(true, """
                {"kind": "ITEM", "ingredientId": %d, "quantity": 1, "unit": "PIECE", "price": 1.00,
                 "vatRateId": %d}""".formatted(tea.getId(), reduced.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("vat_rate_inactive"));
    }

    @Test
    void cashNoteNamesSupplierAndPurchaseDay() throws Exception {
        Ingredient cola = ingredient("Note cola", 24);

        book("""
                {"purchased": "2026-11-17T10:00:00Z", "supplier": " Netto ", "pricesNet": false, "positions": [%s]}"""
                .formatted(item(cola, 1, "CONTAINER", "12.00")))
                .andExpect(status().isCreated());
        assertEquals("Invoice Netto 2026-11-17", entry(cashFor("Invoice Netto"), CashType.PURCHASE).getNote());
    }

    @Test
    void onlyStaffBooksInvoices() throws Exception {
        Ingredient cola = ingredient("Guarded cola", 24);
        String body = invoice(false, item(cola, 1, "CONTAINER", "12.00"));

        mvc.perform(post("/api/storage/invoice").header("Authorization", "Bearer " + login("invoiceuser"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/storage/invoice").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void amountsBeyondTheMoneyColumnsAreRefused() throws Exception {
        Ingredient gold = ingredient("Gold", 0);
        Ingredient silver = ingredient("Silver", 0);

        book(invoice(false, item(gold, 1000, "PIECE", "99999999")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("amount_too_large"));
        book(invoice(false, item(gold, 70, "PIECE", "99999999"), item(silver, 70, "PIECE", "99999999")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("amount_too_large"));
    }

    @Test
    void unitCountBeyondAnIntIsRefused() throws Exception {
        Ingredient pallet = ingredient("Pallet", 5000);

        book(invoice(false, item(pallet, 1_000_000, "CONTAINER", "0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("amount_too_large"));
    }

    @Test
    void negativeItemPriceIsRefused() throws Exception {
        Ingredient cola = ingredient("Negative cola", 24);

        book(invoice(false, item(cola, 1, "PIECE", "-1.00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invoice_price_negative"))
                .andExpect(jsonPath("$.args.position").value("1"));
    }

    @Test
    void fieldsAKindMustNotHaveAreRefused() throws Exception {
        Ingredient cola = ingredient("Shaped cola", 24);

        book(invoice(false, item(cola, 1, "PIECE", "1.00"),
                "{\"kind\": \"DISCOUNT\", \"amount\": 0.10, \"discountOf\": 0, \"vatRateId\": %d}".formatted(reduced.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invoice_position_unexpected"))
                .andExpect(jsonPath("$.args.position").value("2"))
                .andExpect(jsonPath("$.args.field").value("vatRateId"));
        book(invoice(false, item(cola, 1, "PIECE", "1.00"),
                "{\"kind\": \"PLEDGE\", \"quantity\": 1, \"price\": 0.25, \"updateIngredientRate\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.args.field").value("updateIngredientRate"));
    }

    @Test
    void unknownIngredientIsABadRequest() throws Exception {
        book(invoice(false, "{\"kind\": \"ITEM\", \"ingredientId\": 999999, \"quantity\": 1, \"unit\": \"PIECE\", \"price\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("reference_unknown"))
                .andExpect(jsonPath("$.args.entity").value("Ingredient"));
    }

    @Test
    void inactiveRateIsAcceptedWhenItIsTheIngredientsOwn() throws Exception {
        Ingredient candy = ingredient("Old candy", 0);
        candy.setVatRate(reduced);
        ingredientRepository.saveAndFlush(candy);
        reduced.setActive(false);
        vatRateRepository.saveAndFlush(reduced);

        book(invoice(true, """
                {"kind": "ITEM", "ingredientId": %d, "quantity": 1, "unit": "PIECE", "price": 1.00,
                 "vatRateId": %d}""".formatted(candy.getId(), reduced.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goods").value(1.07));
    }

    @Test
    void pledgeLineMayNameItsRateButNotAnInactiveOne() throws Exception {
        Ingredient cola = ingredient("Pledge cola", 24);

        book(invoice(true, item(cola, 1, "PIECE", "1.00"),
                "{\"kind\": \"PLEDGE\", \"quantity\": 1, \"price\": 1.00, \"vatRateId\": %d}".formatted(reduced.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pledge").value(1.07));
        reduced.setActive(false);
        vatRateRepository.saveAndFlush(reduced);
        book(invoice(false, item(cola, 1, "PIECE", "1.00"),
                "{\"kind\": \"PLEDGE\", \"quantity\": 1, \"price\": 1.00, \"vatRateId\": %d}".formatted(reduced.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("vat_rate_inactive"));
    }

    @Test
    void severalDiscountsReduceTheSameItem() throws Exception {
        Ingredient beer = ingredient("Discounted beer", 20);

        book(invoice(false, item(beer, 1, "CONTAINER", "10.00"),
                "{\"kind\": \"DISCOUNT\", \"amount\": 1.00, \"discountOf\": 0}",
                "{\"kind\": \"DISCOUNT\", \"amount\": 2.00, \"discountOf\": 0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batches[0].cost").value(7.00));
    }

    @Test
    void linesRoundHalfUp() throws Exception {
        Ingredient gum = ingredient("Gum", 0);
        Ingredient mint = ingredient("Mint", 0);
        mint.setVatRate(reduced);
        ingredientRepository.saveAndFlush(mint);

        book(invoice(false, item(gum, 1, "PIECE", "0.105")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goods").value(0.11));
        book(invoice(true, item(mint, 1, "PIECE", "2.50")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goods").value(2.68));
    }

    @Test
    void differentRatesToKeepForOneIngredientAreRefused() throws Exception {
        Ingredient chips = ingredient("Torn chips", 0);
        VatRate standard = vatRateRepository.findDefault().orElseThrow();
        String line = """
                {"kind": "ITEM", "ingredientId": %d, "quantity": 1, "unit": "PIECE", "price": 1.00,
                 "vatRateId": %%d, "updateIngredientRate": true}""".formatted(chips.getId());

        book(invoice(true, line.formatted(reduced.getId()), line.formatted(standard.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("vat_rate_conflict"));
    }

    @Test
    void invalidShapesAreRejectedByValidation() throws Exception {
        book("{\"pricesNet\": false, \"positions\": []}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'positions')]").exists());
        book(invoice(false, "{\"kind\": \"ITEM\", \"ingredientId\": 1, \"quantity\": 0, \"unit\": \"PIECE\", \"price\": 1}"))
                .andExpect(status().isBadRequest());
    }

    private Ingredient ingredient(String name, int containerSize) {
        return ingredientRepository.save(new Ingredient(null, category, name, 1.0, containerSize, 0.08, 1.50, 0));
    }

    private static String item(Ingredient ingredient, int quantity, String unit, String price) {
        return "{\"kind\": \"ITEM\", \"ingredientId\": %d, \"quantity\": %d, \"unit\": \"%s\", \"price\": %s}"
                .formatted(ingredient.getId(), quantity, unit, price);
    }

    private static String invoice(boolean pricesNet, String... positions) {
        return "{\"pricesNet\": %s, \"positions\": [%s]}".formatted(pricesNet, String.join(",", positions));
    }

    private ResultActions book(String body) throws Exception {
        return mvc.perform(post("/api/storage/invoice").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private List<Cash> cashFor(String note) {
        entityManager.flush();
        return cashRepository.findAll().stream().filter(cash -> cash.getNote() != null && cash.getNote().startsWith(note)).toList();
    }

    private static Cash entry(List<Cash> entries, CashType type) {
        return entries.stream().filter(cash -> cash.getType() == type).findFirst().orElseThrow();
    }

    private String login(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/login").param("username", username).param("password", "secret"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> tokens = JsonParserFactory.getJsonParser().parseMap(body);
        return (String) ((Map<?, ?>) tokens.get("token")).get("token");
    }
}
