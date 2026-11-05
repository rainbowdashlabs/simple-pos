package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.repository.ProductRepository;
import jakarta.persistence.Tuple;

import java.math.BigDecimal;

public record SalesStatDto(Product product, int sales, BigDecimal revenue, BigDecimal profit) {
    public static SalesStatDto build(Tuple tuple, ProductRepository productRepository) {
        return new SalesStatDto(productRepository.findById((Integer) tuple.get("product_id")).get(),
                ((Long) tuple.get("sales")).intValue(),
                Money.fromQuery((Number) tuple.get("revenue")),
                Money.fromQuery((Number) tuple.get("profit")));
    }
}
