package dev.chojo.simplepos.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * An invoice of a supplier, booked as a whole: its items go into storage, its pledge and what was paid into the cash
 * log. Prices are as printed, so with {@code pricesNet} they are without VAT and each line gets its rate added.
 * Without a purchase date the invoice counts as purchased now.
 */
public record InvoiceRequest(Instant purchased,
                             @Size(max = 100) String supplier,
                             @NotNull Boolean pricesNet,
                             @NotEmpty @Size(max = 200) List<@Valid @NotNull InvoicePosition> positions) {
}
