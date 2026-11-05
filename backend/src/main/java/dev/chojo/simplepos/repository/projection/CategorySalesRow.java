package dev.chojo.simplepos.repository.projection;

import java.math.BigDecimal;

/**
 * Sales of one category over a period as a query reads them; products without a category count under id 0.
 */
public interface CategorySalesRow {
    Integer getCategoryId();

    String getCategoryName();

    Long getSales();

    BigDecimal getRevenue();
}
