package dev.chojo.simplepos.entity.dto;

import java.util.List;

/**
 * The purchases a sale booked, one per cart position, so the sale can be undone as a whole.
 */
public record PurchaseReceipt(List<Integer> purchaseIds) {
}
