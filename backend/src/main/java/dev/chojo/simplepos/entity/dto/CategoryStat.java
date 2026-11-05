package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.repository.projection.CategorySalesRow;

import java.math.BigDecimal;

/**
 * Sales and revenue of a category, rounded to cents; {@code categoryId} 0 stands for products without a category.
 */
public record CategoryStat(int categoryId, String categoryName, int sales, BigDecimal revenue) {
    public static CategoryStat of(CategorySalesRow row) {
        return new CategoryStat(row.getCategoryId(), row.getCategoryName(), row.getSales().intValue(), Money.orZero(row.getRevenue()));
    }
}
