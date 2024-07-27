package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.User;

import java.time.Instant;

public record CashDto(User user,
                      Instant created,
                      double amount,
                      String type,
                      String note) {

    public Cash toNewEntry(User user){
        return new Cash(user, amount, type, note);
    }
}
