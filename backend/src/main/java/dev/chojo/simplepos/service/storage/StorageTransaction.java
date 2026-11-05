package dev.chojo.simplepos.service.storage;

import dev.chojo.simplepos.entity.Storage;

import java.math.BigDecimal;

/**
 * Units taken from one storage batch at once, and what they cost.
 */
public record StorageTransaction(Storage storage, int amount, BigDecimal cost) {
}
