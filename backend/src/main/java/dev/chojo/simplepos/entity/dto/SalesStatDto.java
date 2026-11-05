package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.response.ProductResponse;
import dev.chojo.simplepos.repository.projection.ProductSalesRow;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record SalesStatDto(ProductResponse product, int sales, BigDecimal revenue, BigDecimal profit) {
    /**
     * The statistics of the given rows with their products, in the order of the rows, rounded to cents.
     */
    public static List<SalesStatDto> of(List<ProductSalesRow> rows, Map<Integer, Product> products) {
        return rows.stream()
                .map(row -> new SalesStatDto(product(products.get(row.getProductId())), row.getSales().intValue(),
                        Money.orZero(row.getRevenue()), Money.orZero(row.getProfit())))
                .toList();
    }

    private static ProductResponse product(Product product) {
        return product == null ? null : ProductResponse.of(product);
    }
}
