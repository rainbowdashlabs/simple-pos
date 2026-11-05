package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Role;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The request records refuse what the API must not accept, without a running application.
 */
class RequestConstraintsTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final Ingredient INGREDIENT = new Ingredient(1, null, "Cola", 1.0, 24, 0, 0, 0);
    private static final BigDecimal ONE = BigDecimal.ONE;

    @Test
    void deliveriesNeedOnePriceAPositiveAmountAndNoNegativeMoney() {
        assertTrue(valid(new InboundStorage(INGREDIENT, null, ONE, null, BigDecimal.ZERO, 5)));
        assertTrue(valid(new InboundStorage(INGREDIENT, null, null, ONE, BigDecimal.ZERO, 5)));
        assertTrue(valid(new InboundStorage(INGREDIENT, null, new BigDecimal("3.3333333333333335"), null,
                new BigDecimal("0.44999999999999996"), 3)));
        assertFalse(valid(new InboundStorage(INGREDIENT, null, ONE, ONE, BigDecimal.ZERO, 5)));
        assertFalse(valid(new InboundStorage(INGREDIENT, null, null, null, BigDecimal.ZERO, 5)));
        assertFalse(valid(new InboundStorage(INGREDIENT, null, ONE, null, BigDecimal.ZERO, -5)));
        assertFalse(valid(new InboundStorage(INGREDIENT, null, ONE, null, BigDecimal.ZERO, null)));
        assertFalse(valid(new InboundStorage(INGREDIENT, null, ONE.negate(), null, BigDecimal.ZERO, 5)));
        assertFalse(valid(new InboundStorage(INGREDIENT, null, ONE, null, ONE.negate(), 5)));
        assertFalse(valid(new InboundStorage(INGREDIENT, null, ONE, null, null, 5)));
        assertFalse(valid(new InboundStorage(null, null, ONE, null, BigDecimal.ZERO, 5)));
    }

    @Test
    void cartPositionsNeedAProductAndAtLeastOne() {
        Product product = new Product();
        assertTrue(valid(new CartPositionDto(product, 1)));
        assertFalse(valid(new CartPositionDto(product, 0)));
        assertFalse(valid(new CartPositionDto(product, -10)));
        assertFalse(valid(new CartPositionDto(null, 1)));
    }

    @Test
    void countedStockIsNotNegative() {
        assertTrue(valid(new InventoryCorrection(1, 0)));
        assertFalse(valid(new InventoryCorrection(1, -1)));
        assertFalse(valid(new InventoryCorrection(1, null)));
        assertFalse(valid(new InventoryCorrection(null, 1)));
    }

    @Test
    void accountNamesArePlainAndShort() {
        assertTrue(valid(new AccountRequest("Lilly")));
        assertFalse(valid(new AccountRequest(" ")));
        assertFalse(valid(new AccountRequest(null)));
        assertFalse(valid(new AccountRequest("a".repeat(101))));
        assertFalse(valid(new AccountRequest("line\nbreak")));
        assertFalse(valid(new AccountRequest("next\u0085line")));
    }

    @Test
    void preferencesOnlyTakeKnownValues() {
        assertTrue(valid(new UserPreferencesDto("de", "blue", "dark")));
        assertFalse(valid(new UserPreferencesDto("fr", "blue", "dark")));
        assertFalse(valid(new UserPreferencesDto("de", "purple", "dark")));
        assertFalse(valid(new UserPreferencesDto("de", "blue", "dim")));
        assertFalse(valid(new UserPreferencesDto(null, "blue", "dark")));
    }

    @Test
    void userChangesNeedSomethingToChangeAndNewUsersARole() {
        assertTrue(valid(new UpdateUserRequest(Role.STAFF, null)));
        assertFalse(valid(new UpdateUserRequest(null, null)));
        assertFalse(valid(new CreateUserRequest("someone", "long-enough", null)));
    }

    private static boolean valid(Object request) {
        return VALIDATOR.validate(request).isEmpty();
    }
}
