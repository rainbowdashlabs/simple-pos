package dev.chojo.simplepos.entity.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * A booked invoice: the stored batches in the order of their items, what the goods cost after discounts, the pledge,
 * and both together.
 */
public record InvoiceResponse(List<StorageResponse> batches, BigDecimal goods, BigDecimal pledge, BigDecimal total) {
}
