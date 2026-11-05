package dev.chojo.simplepos.repository.projection;

import java.math.BigDecimal;

/**
 * Sales over a whole period as a query reads them.
 */
public interface TotalsRow {
    Long getSales();

    BigDecimal getRevenue();

    BigDecimal getProfit();
}
