package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.Balance;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One deposit to an account. The account is not repeated in every row; the request names it.
 */
public record BalanceResponse(Integer id, Instant added, BigDecimal amount) {
    public static BalanceResponse of(Balance balance) {
        return new BalanceResponse(balance.getId(), balance.getAdded(), balance.getAmount());
    }
}
