package dev.chojo.simplepos.entity.dto;

import jakarta.persistence.Tuple;
import org.slf4j.Logger;

import java.time.Instant;

import static org.slf4j.LoggerFactory.getLogger;

public final class ProductSalesStatDto {
    private static final Logger log = getLogger(ProductSalesStatDto.class);
    private Instant day;
    private int sales;
    private double revenue;
    private double profit;

    public ProductSalesStatDto(Instant day, int sales, double revenue, double profit) {
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
        this.revenue = (double) tuple.get("revenue");
        this.profit = (double) tuple.get("profit");
    }
    public ProductSalesStatDto(Tuple tuple, Instant instant) {
        this.day = instant;
        this.sales = ((Long) tuple.get("sales")).intValue();
        this.revenue = (double) tuple.get("revenue");
        this.profit = (double) tuple.get("profit");
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

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }
}
