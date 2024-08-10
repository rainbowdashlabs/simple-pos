package dev.chojo.simplepos.entity.dto;

import java.time.Instant;

public record AccountDto(int id, String name, Instant created, double balance) {
}
