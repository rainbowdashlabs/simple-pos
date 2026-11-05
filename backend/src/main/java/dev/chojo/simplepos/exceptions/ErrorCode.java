package dev.chojo.simplepos.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Why a request was refused, as a code the frontend translates. Each code has an English text for the detail of the
 * answer and the names of the values it mentions; the answer carries the code as {@code code} and the values by name
 * as {@code args}, so a translation can place them in its own order.
 */
public enum ErrorCode {
    NOT_FOUND("%s %s not found", "entity", "id"),
    REFERENCE_UNKNOWN("%s %s does not exist", "entity", "id"),
    ID_MISSING("%s id is missing", "entity"),
    INVALID_REQUEST("The request is invalid"),
    DATA_CONFLICT("The change conflicts with data that was changed meanwhile"),
    CONCURRENT_CHANGE("The data was changed concurrently. Please retry."),
    RANGE_BACKWARDS("The range ends before it starts"),
    RANGE_TOO_LONG("The range may span at most %s years", "years"),
    NOTHING_RETURNED("Nothing is returned"),
    INGREDIENT_LISTED_TWICE("Each ingredient may only be listed once"),
    NO_BOTTLE_DEPOSIT("%s has no bottle deposit", "name"),
    NO_CRATE_DEPOSIT("%s has no crate deposit", "name"),
    NOTHING_RETURNED_FOR("Nothing is returned for %s", "name"),
    REFUND_TOO_LARGE("The refund is too large"),
    RECIPE_MISSING("Recipe is missing"),
    RECIPE_ENTRY_AMBIGUOUS("Each recipe entry needs either an existing ingredient or a new one"),
    NO_PURCHASE_TO_CANCEL("No purchase to cancel"),
    AMOUNT_TOO_LARGE("The price or pledge is too large"),
    BATCH_INVALID("A batch needs at least one unit and cannot have sold more than it holds"),
    CURRENT_PASSWORD_WRONG("The current password is wrong"),
    USERNAME_MISSING("Username is missing"),
    USERNAME_CONTROL_CHARACTERS("Username must not contain control characters"),
    USERNAME_TOO_LONG("Username must be at most %s characters", "max"),
    PASSWORD_TOO_SHORT("Password must be at least %s characters", "min"),
    PASSWORD_TOO_LONG("Password must be at most %s bytes", "max"),
    USERNAME_TAKEN("Username %s is already taken", "name"),
    LAST_ADMIN("At least one enabled administrator must remain"),
    CATEGORY_IN_USE("%s is still used by %s product(s) and %s ingredient(s), inactive ones included. Move them to another category first.",
            "name", "products", "ingredients"),
    CATEGORY_JUST_USED("%s was just given a product or ingredient. Move it to another category first.", "name"),
    INGREDIENT_IN_USE("%s is used in recipes or storage. Deactivate it instead.", "name"),
    PRODUCT_NOT_ON_SALE("%s is not on sale", "name"),
    PURCHASE_OF_CLOSED_ACCOUNT("The purchase belongs to a closed account and cannot be cancelled");

    private final String template;
    private final String[] params;

    ErrorCode(String template, String... params) {
        this.template = template;
        this.params = params;
    }

    /**
     * The code as the answer carries it.
     */
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * The answer for this code with the given values, one per parameter name, in order.
     *
     * @throws IllegalArgumentException if the number of values does not match the parameters
     */
    public ProblemDetail problem(HttpStatusCode status, Object... args) {
        if (args.length != params.length) {
            throw new IllegalArgumentException("%s takes %d values, got %d".formatted(this, params.length, args.length));
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, template.formatted(args));
        problem.setProperty("code", value());
        Map<String, String> named = new LinkedHashMap<>();
        for (int i = 0; i < params.length; i++) named.put(params[i], String.valueOf(args[i]));
        problem.setProperty("args", named);
        return problem;
    }
}
