package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.controller.validation.PageNumber;
import dev.chojo.simplepos.controller.validation.PageSize;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.InboundStorage;
import dev.chojo.simplepos.entity.dto.InventoryCorrection;
import dev.chojo.simplepos.entity.dto.StockProjectionDto;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import dev.chojo.simplepos.entity.response.Listing;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.service.StorageService;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/storage")
public class StorageController {
    private final StorageRepository storageRepository;
    private final IngredientRepository ingredientRepository;
    private final StorageService storageService;

    public StorageController(StorageRepository storageRepository, IngredientRepository ingredientRepository, StorageService storageService) {
        this.storageRepository = storageRepository;
        this.ingredientRepository = ingredientRepository;
        this.storageService = storageService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Storage> getSummary(@RequestParam(defaultValue = "100") @PageSize int limit,
                                    @RequestParam(defaultValue = "0") @PageNumber int page) {
        // TODO the response lacks the ingredient of each batch, so it cannot be attributed
        return storageRepository.findAllBy(PageRequest.of(page, limit, Sort.by("id")));
    }

    @GetMapping("/stock/{id}")
    @ResponseStatus(HttpStatus.OK)
    public StorageSummary getStock(@PathVariable int id) {
        return new StorageSummary(storageRepository.summaryById(id));
    }

    @GetMapping("/stock")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Listing<StorageSummary>> getStock() {
        return ResponseEntity.ok(Listing.map(storageRepository.summary().stream().map(StorageSummary::new).toList(), e -> e.getIngredient().getCategory()));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Storage>> byId(@PathVariable int id,
                                              @RequestParam(value = "limit", defaultValue = "100") @PageSize int limit,
                                              @RequestParam(value = "page", defaultValue = "0") @PageNumber int page) {
        Ingredient ingredient = ingredientRepository.require(id);
        PageRequest request = PageRequest.of(page, limit).withSort(Sort.by("id").descending());
        return ResponseEntity.ok(storageRepository.findAllByIngredient(ingredient, request));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Storage> add(@RequestBody InboundStorage storage) {
        var current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.accepted().body(storageService.deliver(storage, current));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateInventory(@RequestBody List<InventoryCorrection> corrections) {
        storageService.processInventoryCorrection(corrections);
    }

    @GetMapping("/stock/projection")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<StockProjectionDto>> getStockProjection() {
        // Get current stock for all ingredients
        Map<Integer, StorageSummary> stockByIngredient = new HashMap<>();
        for (Tuple tuple : storageRepository.summary()) {
            StorageSummary summary = new StorageSummary(tuple);
            stockByIngredient.put(summary.getIngredient().getId(), summary);
        }

        // Get daily consumption data for last 90 days
        Instant ninetyDaysAgo = Instant.now().minus(90, ChronoUnit.DAYS);
        List<Tuple> consumptionData = storageRepository.dailyConsumptionByIngredient(ninetyDaysAgo);

        // Group consumption by ingredient_id -> list of daily consumed amounts
        Map<Integer, List<Integer>> dailyConsumptionMap = new HashMap<>();
        for (Tuple tuple : consumptionData) {
            int ingredientId = (int) tuple.get("ingredient_id");
            int consumed = (int) tuple.get("consumed");
            dailyConsumptionMap.computeIfAbsent(ingredientId, _ -> new ArrayList<>()).add(consumed);
        }

        // Calculate number of days in the period
        long totalDaysInPeriod = 90;

        // Build projections
        List<StockProjectionDto> projections = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : dailyConsumptionMap.entrySet()) {
            int ingredientId = entry.getKey();
            List<Integer> dailyAmounts = entry.getValue();

            StorageSummary summary = stockByIngredient.get(ingredientId);
            if (summary == null) continue;

            // Total consumption over the period
            long totalConsumed = dailyAmounts.stream().mapToLong(Integer::longValue).sum();
            double dailyConsumption = (double) totalConsumed / totalDaysInPeriod;

            Integer daysRemaining = null;
            if (dailyConsumption > 0) {
                daysRemaining = (int) (summary.getStock() / dailyConsumption);
            }

            projections.add(new StockProjectionDto(
                    summary.getIngredient(),
                    summary.getStock(),
                    Math.round(dailyConsumption * 100.0) / 100.0,
                    daysRemaining
            ));
        }

        // Sort by daysRemaining ASC, nulls last — only return the 10 items that will run out soonest
        projections.sort(Comparator.comparing(StockProjectionDto::daysRemaining, Comparator.nullsLast(Comparator.naturalOrder())));

        return ResponseEntity.ok(projections.stream().limit(10).toList());
    }

    @GetMapping("/stock/low")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<StorageSummary>> getStockLow(@RequestParam(value = "limit", defaultValue = "100") @PageSize int limit,
                                                            @RequestParam(value = "page", defaultValue = "0") @PageNumber int page) {
        List<StorageSummary> list = storageRepository.lowStock(PageRequest.of(page, limit)).stream().map(StorageSummary::new).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/stock/out")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<StorageSummary>> getStockOut(@RequestParam(value = "limit", defaultValue = "100") @PageSize int limit,
                                                            @RequestParam(value = "page", defaultValue = "0") @PageNumber int page) {
        List<StorageSummary> list = storageRepository.outOfStock(PageRequest.of(page, limit)).stream().map(StorageSummary::new).toList();
        return ResponseEntity.ok(list);
    }
}
