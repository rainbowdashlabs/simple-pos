package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.response.PageResponse;

import java.util.Map;

/**
 * A page of an account's purchases, with the products those purchases name.
 */
public record PurchaseHistoryDto(Map<Integer, LazyProduct> products, PageResponse<PurchaseDto> purchases) {
}
