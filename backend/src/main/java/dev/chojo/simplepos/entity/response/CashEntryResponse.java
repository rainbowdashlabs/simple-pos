package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.User;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One booking of the till, with who booked it, if anyone did.
 */
public record CashEntryResponse(int id, Author user, Instant created, BigDecimal amount, CashType type, String note) {
    public static CashEntryResponse of(Cash cash) {
        return new CashEntryResponse(cash.getId(), Author.of(cash.getUser()), cash.getCreated(), cash.getAmount(),
                cash.getType(), cash.getNote());
    }

    public record Author(Integer id, String username) {
        static Author of(User user) {
            return user == null ? null : new Author(user.getId(), user.getUsername());
        }
    }
}
