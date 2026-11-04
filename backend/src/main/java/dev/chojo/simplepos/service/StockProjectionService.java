package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.dto.StockProjectionDto;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import dev.chojo.simplepos.repository.StorageRepository;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estimates how long the stock of each ingredient lasts.
 * <p>
 * Daily usage is what was actually taken from storage for sales, averaged over the last {@value #WINDOW_DAYS} days,
 * or over the days since the ingredient was first taken if that is shorter. So an ingredient that is new, or whose
 * recipes changed, is judged by what it really sold, not by today's recipes or by days it did not exist yet.
 */
@Service
public class StockProjectionService {
    static final int WINDOW_DAYS = 90;
    static final int LIMIT = 10;

    private final StorageRepository storageRepository;

    public StockProjectionService(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    /**
     * The {@value #LIMIT} ingredients that run out soonest; ingredients without usage come last.
     */
    public List<StockProjectionDto> soonestEmpty() {
        return soonestEmpty(Instant.now());
    }

    List<StockProjectionDto> soonestEmpty(Instant now) {
        Map<Integer, StorageSummary> stock = new HashMap<>();
        for (Tuple tuple : storageRepository.summary()) {
            StorageSummary summary = new StorageSummary(tuple);
            stock.put(summary.getIngredient().getId(), summary);
        }

        List<StockProjectionDto> projections = new ArrayList<>();
        for (Tuple usage : storageRepository.usageByIngredient(now.minus(Duration.ofDays(WINDOW_DAYS)), now)) {
            StorageSummary summary = stock.get(((Number) usage.get("ingredient_id")).intValue());
            if (summary == null) continue;
            long taken = ((Number) usage.get("taken")).longValue();
            Instant firstTaken = usage.get("first_taken", Instant.class);
            double daily = (double) taken / windowDays(firstTaken, now);
            Integer daysRemaining = daily > 0 ? (int) (summary.getStock() / daily) : null;
            projections.add(new StockProjectionDto(summary.getIngredient(), summary.getStock(),
                    Math.round(daily * 100.0) / 100.0, daysRemaining));
        }

        projections.sort(Comparator.comparing(StockProjectionDto::daysRemaining, Comparator.nullsLast(Comparator.naturalOrder())));
        return projections.stream().limit(LIMIT).toList();
    }

    static long windowDays(Instant firstTaken, Instant now) {
        long days = (long) Math.ceil(Duration.between(firstTaken, now).toSeconds() / 86400.0);
        return Math.clamp(days, 1, WINDOW_DAYS);
    }
}
