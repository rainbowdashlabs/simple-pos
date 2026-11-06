package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.controller.validation.PageSize;
import dev.chojo.simplepos.entity.Role;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The request records refuse what the API must not accept, without a running application.
 */
class RequestConstraintsTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final BigDecimal ONE = BigDecimal.ONE;

    @Test
    void deliveriesNeedOnePriceAPositiveAmountAndNoNegativeMoney() {
        assertTrue(valid(new InboundStorage(1, null, ONE, null, BigDecimal.ZERO, 5)));
        assertTrue(valid(new InboundStorage(1, null, null, ONE, BigDecimal.ZERO, 5)));
        assertTrue(valid(new InboundStorage(1, null, new BigDecimal("3.3333333333333335"), null,
                new BigDecimal("0.44999999999999996"), 3)));
        assertFalse(valid(new InboundStorage(1, null, ONE, ONE, BigDecimal.ZERO, 5)));
        assertFalse(valid(new InboundStorage(1, null, null, null, BigDecimal.ZERO, 5)));
        assertFalse(valid(new InboundStorage(1, null, ONE, null, BigDecimal.ZERO, -5)));
        assertFalse(valid(new InboundStorage(1, null, ONE, null, BigDecimal.ZERO, null)));
        assertFalse(valid(new InboundStorage(1, null, ONE.negate(), null, BigDecimal.ZERO, 5)));
        assertFalse(valid(new InboundStorage(1, null, ONE, null, ONE.negate(), 5)));
        assertFalse(valid(new InboundStorage(1, null, ONE, null, null, 5)));
        assertFalse(valid(new InboundStorage(null, null, ONE, null, BigDecimal.ZERO, 5)));
    }

    @Test
    void cartPositionsNeedAProductAndAtLeastOne() {
        assertTrue(valid(new CartPositionDto(1, 1)));
        assertFalse(valid(new CartPositionDto(1, 0)));
        assertFalse(valid(new CartPositionDto(1, -10)));
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

    @Test
    void pageLimitTextsNameTheLimitsInForce() {
        for (Locale locale : List.of(Locale.ROOT, Locale.ENGLISH, Locale.GERMAN)) {
            ResourceBundle messages = ResourceBundle.getBundle("ValidationMessages", locale);
            assertTrue(messages.getString("simplepos.validation.page_size").contains(String.valueOf(PageSize.MAX)));
            assertTrue(messages.getString("simplepos.validation.page_number")
                    .contains(String.valueOf(Integer.MAX_VALUE / PageSize.MAX)));
        }
    }

    private static boolean valid(Object request) {
        return VALIDATOR.validate(request).isEmpty();
    }
}
