package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.repository.projection.DayRow;
import dev.chojo.simplepos.repository.projection.TotalsRow;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sales, revenue and profit of a business day, or of a whole period when {@code day} is null, rounded to cents.
 */
public record DayStat(LocalDate day, int sales, BigDecimal revenue, BigDecimal profit) {
    public static DayStat of(DayRow row) {
        return new DayStat(row.getDay(), row.getSales().intValue(), Money.orZero(row.getRevenue()), Money.orZero(row.getProfit()));
    }

    public static DayStat of(TotalsRow row) {
        return new DayStat(null, row.getSales().intValue(), Money.orZero(row.getRevenue()), Money.orZero(row.getProfit()));
    }
}
