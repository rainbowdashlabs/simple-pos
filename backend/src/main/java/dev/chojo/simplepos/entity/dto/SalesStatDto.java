package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.repository.ProductRepository;
import jakarta.persistence.Tuple;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record SalesStatDto(Product product, int sales, BigDecimal revenue, BigDecimal profit) {
    /**
     * The statistics of the given rows, with their products loaded in one query, in the order of the rows.
     */
    public static List<SalesStatDto> build(List<Tuple> tuples, ProductRepository productRepository) {
        List<Integer> ids = tuples.stream().map(tuple -> ((Number) tuple.get("product_id")).intValue()).toList();
        Map<Integer, Product> products = productRepository.findAllByIdIn(ids).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        return tuples.stream()
                .map(tuple -> new SalesStatDto(products.get(((Number) tuple.get("product_id")).intValue()),
                        ((Number) tuple.get("sales")).intValue(),
                        Money.fromQuery((Number) tuple.get("revenue")),
                        Money.fromQuery((Number) tuple.get("profit"))))
                .toList();
    }
}
