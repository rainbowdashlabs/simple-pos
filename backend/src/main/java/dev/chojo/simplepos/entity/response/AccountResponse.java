package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.Account;

import java.time.Instant;

public record AccountResponse(Integer id, String name, Instant created) {
    public static AccountResponse of(Account account) {
        return new AccountResponse(account.getId(), account.getName(), account.getCreated());
    }
}
