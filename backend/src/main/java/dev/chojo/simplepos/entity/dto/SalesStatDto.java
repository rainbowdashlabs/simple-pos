package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.repository.ProductRepository;
import jakarta.persistence.Tuple;

public record SalesStatDto(Product product, int sales, double revenue, double profit) {
    public static SalesStatDto build(Tuple tuple, ProductRepository productRepository) {
        return new SalesStatDto(productRepository.findById((Integer) tuple.get("product_id")).get(),
                ((Long) tuple.get("sales")).intValue(),
                (Double) tuple.get("revenue"),
                (Double) tuple.get("profit"));
    }
}
