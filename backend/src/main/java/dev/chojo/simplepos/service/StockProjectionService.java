package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Category;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.dto.IngredientUsage;
import dev.chojo.simplepos.entity.dto.ShoppingListEntry;
import dev.chojo.simplepos.entity.dto.StockProjectionDto;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import dev.chojo.simplepos.entity.response.IngredientResponse;
import dev.chojo.simplepos.repository.StorageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
 * Daily usage is what sales actually used, from storage or sold without stock, averaged over the last
 * {@value #WINDOW_DAYS} days, or over the days since the ingredient was first used if that is shorter. So an
 * ingredient that is new, or whose recipes changed, is judged by what it really sold, not by today's recipes or by
 * days it did not exist yet.
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
    @Transactional(readOnly = true)
    public List<StockProjectionDto> soonestEmpty() {
        return soonestEmpty(Instant.now());
    }

    List<StockProjectionDto> soonestEmpty(Instant now) {
        Map<Integer, StorageSummary> stock = new HashMap<>();
        for (StorageSummary summary : storageRepository.summary()) {
            stock.put(summary.getIngredient().getId(), summary);
        }

        List<StockProjectionDto> projections = new ArrayList<>();
        for (Map.Entry<Integer, Usage> usage : usage(now).entrySet()) {
            StorageSummary summary = stock.get(usage.getKey());
            if (summary == null) continue;
            double daily = usage.getValue().daily();
            Integer daysRemaining = daily > 0 ? (int) (summary.getStock() / daily) : null;
            projections.add(new StockProjectionDto(IngredientResponse.of(summary.getIngredient()), summary.getStock(),
                    Math.round(daily * 100.0) / 100.0, daysRemaining));
        }

        projections.sort(Comparator.comparing(StockProjectionDto::daysRemaining, Comparator.nullsLast(Comparator.naturalOrder())));
        return projections.stream().limit(LIMIT).toList();
    }

    /**
     * What to buy so that every active ingredient lasts {@code days} more days at its daily usage and still keeps
     * its minimum stock afterwards. Listed are the ingredients below their minimum stock or running out within
     * {@code days}; the amount is rounded up to full containers when the ingredient comes in containers. Sorted by
     * category, then name.
     */
    @Transactional(readOnly = true)
    public List<ShoppingListEntry> shoppingList(int days) {
        return shoppingList(days, Instant.now());
    }

    List<ShoppingListEntry> shoppingList(int days, Instant now) {
        Map<Integer, Usage> usage = usage(now);
        List<ShoppingListEntry> entries = new ArrayList<>();
        for (StorageSummary summary : storageRepository.summary()) {
            Ingredient ingredient = summary.getIngredient();
            Usage used = usage.getOrDefault(ingredient.getId(), Usage.NONE);
            long stock = summary.getStock();
            boolean belowMinimum = stock < ingredient.getMinStock();
            boolean runsOut = stock * used.window() < used.taken() * days;
            if (!belowMinimum && !runsOut) continue;
            int needed = (int) (Math.ceilDiv(used.taken() * days, used.window()) + ingredient.getMinStock() - stock);
            if (needed <= 0) continue;
            int size = ingredient.getContainerSize();
            int containers = size > 1 ? Math.ceilDiv(needed, size) : 0;
            int amount = size > 1 ? containers * size : needed;
            entries.add(new ShoppingListEntry(IngredientResponse.of(ingredient), summary.getStock(), Math.round(used.daily() * 100.0) / 100.0,
                    amount, containers, Money.of(Money.orZero(ingredient.getPrice()).multiply(BigDecimal.valueOf(amount)))));
        }
        entries.sort(Comparator.comparing((ShoppingListEntry entry) -> categoryName(entry.ingredient()))
                .thenComparingInt(entry -> categoryId(entry.ingredient()))
                .thenComparing(entry -> entry.ingredient().name()));
        return entries;
    }

    private static String categoryName(IngredientResponse ingredient) {
        return ingredient.category() == null ? "" : ingredient.category().name();
    }

    private static int categoryId(IngredientResponse ingredient) {
        return ingredient.category() == null ? Category.UNCATEGORISED_ID : ingredient.category().id();
    }

    /**
     * The units each ingredient used and over how many days, by ingredient id; ingredients without usage are missing.
     */
    private Map<Integer, Usage> usage(Instant now) {
        Map<Integer, Usage> usage = new HashMap<>();
        for (IngredientUsage row : storageRepository.usageByIngredient(now.minus(Duration.ofDays(WINDOW_DAYS)), now)) {
            usage.put(row.ingredientId(), new Usage(row.taken(), windowDays(row.firstTaken(), now)));
        }
        return usage;
    }

    /**
     * {@code taken} units used over {@code window} days, kept as whole numbers so comparisons stay exact.
     */
    private record Usage(long taken, long window) {
        static final Usage NONE = new Usage(0, 1);

        double daily() {
            return (double) taken / window;
        }
    }

    static long windowDays(Instant firstTaken, Instant now) {
        long days = (long) Math.ceil(Duration.between(firstTaken, now).toSeconds() / 86400.0);
        return Math.clamp(days, 1, WINDOW_DAYS);
    }
}
