package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Product;

import java.util.List;
import java.util.Map;

public record PurchaseHistoryDto(Map<Integer, LazyProduct> products, List<PurchaseDto> purchases) {
}
