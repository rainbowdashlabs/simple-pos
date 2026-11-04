package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.User;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A cash entry a user books by hand. The user is always the one logged in, never taken from the request, and the
 * amount must not be 0.
 */
public record CashDto(@NotNull Double amount,
                      @NotNull CashType type,
                      @Size(max = 255) String note) {

    @AssertTrue(message = "must not be 0")
    public boolean isAmount() {
        return amount == null || amount != 0;
    }

    public Cash toNewEntry(User user) {
        return new Cash(user, amount, type, note);
    }
}
