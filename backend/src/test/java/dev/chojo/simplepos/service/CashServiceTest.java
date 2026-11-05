package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.TillCount;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.entity.dto.EmptiesReturn;
import dev.chojo.simplepos.entity.dto.TillCountRequest;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.CategoryRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.TillCountRepository;
import dev.chojo.simplepos.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static dev.chojo.simplepos.MoneyAssertions.assertMoney;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A till count must leave the till total equal to what was counted, and record what was counted. Returned empties
 * must refund exactly their bottle and crate deposits.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CashServiceTest {
    @Autowired
    CashService cashService;
    @Autowired
    CashRepository cashRepository;
    @Autowired
    TillCountRepository tillCountRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager entityManager;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    CategoryRepository categoryRepository;

    private User cashier;

    @BeforeEach
    void setUp() {
        cashier = userRepository.save(new User(null, "tillcounter", "x", true, Role.STAFF));
        BigDecimal before = cashService.tillTotal();
        if (before.signum() != 0) cashRepository.save(new Cash(cashier, before.negate(), CashType.OTHER, "empty the till"));
        cashRepository.save(new Cash(cashier, new BigDecimal("50.00"), CashType.OTHER, "float"));
    }

    @Test
    void shouldBookTheDifferenceSoTheTotalMatchesTheCount() {
        BigDecimal expected = cashService.tillTotal();

        TillCount count = cashService.count(new TillCountRequest(Map.of(200, 20, 50, 3, 1, 7)), cashier);

        assertMoney(new BigDecimal("41.57"), count.getCounted());
        assertMoney(expected, count.getExpected());
        assertMoney(new BigDecimal("41.57"), cashService.tillTotal());
        assertEquals(List.of(new BigDecimal("41.57").subtract(expected)), corrections());
    }

    @Test
    void shouldBookNothingWhenTheCountMatches() {
        cashService.count(new TillCountRequest(Map.of(2000, 2, 500, 2)), cashier);

        assertEquals(List.of(), corrections());
    }

    @Test
    void shouldKeepTheCountedPieces() {
        TillCount count = cashService.count(new TillCountRequest(Map.of(500, 2, 10_000, 1)), cashier);
        entityManager.flush();
        entityManager.clear();

        assertEquals(Map.of(500, 2, 10_000, 1), tillCountRepository.findById(count.getId()).orElseThrow().getDenominations());
    }

    private List<BigDecimal> corrections() {
        entityManager.flush();
        return cashRepository.findAll().stream()
                .filter(cash -> cash.getType() == CashType.CORRECTION)
                .map(Cash::getAmount)
                .toList();
    }

    @Test
    void shouldRefundBottlesAndCratesAtTheirDeposits() {
        Ingredient beer = ingredientRepository.save(new Ingredient(null, categoryRepository.save(new Category(null, "EmptiesCat")),
                "EmptiesBeer", 1.0, 20, 0.08, 1.50, 0));
        BigDecimal before = cashService.tillTotal();

        BigDecimal refunded = cashService.returnEmpties(List.of(new EmptiesReturn(beer.getId(), 7, 2)), cashier);

        assertMoney(new BigDecimal("3.56"), refunded);
        assertMoney(before.add(new BigDecimal("3.56")), cashService.tillTotal());
    }

    @Test
    void shouldRefuseEmptiesWithoutADeposit() {
        Ingredient juice = ingredientRepository.save(new Ingredient(null, categoryRepository.save(new Category(null, "NoDepositCat")),
                "NoDepositJuice", 1.0, 6, 0, 2.0, 0));

        assertThrows(BadRequestException.class,
                () -> cashService.returnEmpties(List.of(new EmptiesReturn(juice.getId(), 1, 0)), cashier));
        assertThrows(BadRequestException.class,
                () -> cashService.returnEmpties(List.of(new EmptiesReturn(juice.getId(), 0, 0)), cashier));
        assertThrows(BadRequestException.class,
                () -> cashService.returnEmpties(List.of(new EmptiesReturn(juice.getId(), 0, 1), new EmptiesReturn(juice.getId(), 0, 1)), cashier));
        assertThrows(BadRequestException.class,
                () -> cashService.returnEmpties(List.of(new EmptiesReturn(-1, 0, 1)), cashier));
    }
}
