package dev.chojo.simplepos.repository.projection;

import java.math.BigDecimal;

/**
 * Sales of one product over a period as a query reads them.
 */
public interface ProductSalesRow {
    Integer getProductId();

    Long getSales();

    BigDecimal getRevenue();

    BigDecimal getProfit();
}
