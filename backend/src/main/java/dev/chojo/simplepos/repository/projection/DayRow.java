package dev.chojo.simplepos.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sales of one business day as a query reads them.
 */
public interface DayRow {
    LocalDate getDay();

    Long getSales();

    BigDecimal getRevenue();

    BigDecimal getProfit();
}
