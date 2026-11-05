package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.Money;
import jakarta.persistence.Tuple;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.Instant;

import static org.slf4j.LoggerFactory.getLogger;

public final class ProductSalesStatDto {
    private static final Logger log = getLogger(ProductSalesStatDto.class);
    private Instant day;
    private int sales;
    private BigDecimal revenue;
    private BigDecimal profit;

    public ProductSalesStatDto(Instant day, int sales, BigDecimal revenue, BigDecimal profit) {
        this.day = day;
        this.sales = sales;
        this.revenue = revenue;
        this.profit = profit;
    }

    public ProductSalesStatDto() {
    }

    public ProductSalesStatDto(Tuple tuple) {
        this.day = (Instant) tuple.get("day");
        this.sales = ((Long) tuple.get("sales")).intValue();
        this.revenue = Money.fromQuery((Number) tuple.get("revenue"));
        this.profit = Money.fromQuery((Number) tuple.get("profit"));
    }
    public ProductSalesStatDto(Tuple tuple, Instant instant) {
        this.day = instant;
        this.sales = ((Long) tuple.get("sales")).intValue();
        this.revenue = Money.fromQuery((Number) tuple.get("revenue"));
        this.profit = Money.fromQuery((Number) tuple.get("profit"));
    }

    public Instant getDay() {
        return day;
    }

    public void setDay(Instant day) {
        this.day = day;
    }

    public int getSales() {
        return sales;
    }

    public void setSales(int sales) {
        this.sales = sales;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }
}
