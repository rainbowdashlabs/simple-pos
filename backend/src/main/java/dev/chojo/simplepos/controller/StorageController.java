package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.controller.validation.PageNumber;
import dev.chojo.simplepos.controller.validation.PageSize;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.InboundStorage;
import dev.chojo.simplepos.entity.dto.InventoryCorrection;
import dev.chojo.simplepos.entity.dto.ShoppingListEntry;
import dev.chojo.simplepos.entity.dto.StockProjectionDto;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import dev.chojo.simplepos.entity.response.PageResponse;
import dev.chojo.simplepos.entity.response.StockResponse;
import dev.chojo.simplepos.entity.response.StorageResponse;
import dev.chojo.simplepos.exceptions.NotFoundException;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.service.StockProjectionService;
import dev.chojo.simplepos.service.StorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/storage")
public class StorageController {
    private final StorageRepository storageRepository;
    private final IngredientRepository ingredientRepository;
    private final StorageService storageService;
    private final StockProjectionService stockProjectionService;

    public StorageController(StorageRepository storageRepository, IngredientRepository ingredientRepository,
                             StorageService storageService, StockProjectionService stockProjectionService) {
        this.storageRepository = storageRepository;
        this.ingredientRepository = ingredientRepository;
        this.storageService = storageService;
        this.stockProjectionService = stockProjectionService;
    }

    @GetMapping
    public PageResponse<StorageResponse> getSummary(@RequestParam(defaultValue = "100") @PageSize int limit,
                                                    @RequestParam(defaultValue = "0") @PageNumber int page) {
        return PageResponse.of(storageRepository.findAllBy(PageRequest.of(page, limit, Sort.by("id"))), StorageResponse::of);
    }

    @GetMapping("/stock/{id}")
    public StockResponse getStock(@PathVariable int id) {
        StorageSummary summary = storageRepository.summaryById(id);
        if (summary == null) throw new NotFoundException("Ingredient", id);
        return StockResponse.of(summary);
    }

    @GetMapping("/stock")
    public PageResponse<StockResponse> getStock(@RequestParam(defaultValue = "100") @PageSize int limit,
                                                @RequestParam(defaultValue = "0") @PageNumber int page) {
        return PageResponse.of(storageRepository.summaryPage(PageRequest.of(page, limit)), StockResponse::of);
    }

    @GetMapping("/{id}")
    public PageResponse<StorageResponse> byId(@PathVariable int id,
                                              @RequestParam(value = "limit", defaultValue = "100") @PageSize int limit,
                                              @RequestParam(value = "page", defaultValue = "0") @PageNumber int page) {
        Ingredient ingredient = ingredientRepository.require(id);
        PageRequest request = PageRequest.of(page, limit).withSort(Sort.by("id").descending());
        return PageResponse.of(storageRepository.findAllByIngredient(ingredient, request), StorageResponse::of);
    }

    @PostMapping
    ResponseEntity<StorageResponse> add(@Valid @RequestBody InboundStorage storage, @CurrentUser User current) {
        return ResponseEntity.status(HttpStatus.CREATED).body(StorageResponse.of(storageService.deliver(storage, current)));
    }

    @PutMapping
    public void updateInventory(@RequestBody @Size(max = 1000) List<@Valid @NotNull InventoryCorrection> corrections) {
        storageService.processInventoryCorrection(corrections);
    }

    @GetMapping("/shopping-list")
    public List<ShoppingListEntry> shoppingList(@RequestParam(defaultValue = "14") @Min(1) @Max(90) int days) {
        return stockProjectionService.shoppingList(days);
    }

    @GetMapping("/stock/projection")
    public ResponseEntity<List<StockProjectionDto>> getStockProjection() {
        return ResponseEntity.ok(stockProjectionService.soonestEmpty());
    }

    @GetMapping("/stock/low")
    public PageResponse<StockResponse> getStockLow(@RequestParam(defaultValue = "100") @PageSize int limit,
                                                   @RequestParam(defaultValue = "0") @PageNumber int page) {
        return PageResponse.of(storageRepository.lowStock(PageRequest.of(page, limit)), StockResponse::of);
    }

    @GetMapping("/stock/out")
    public PageResponse<StockResponse> getStockOut(@RequestParam(defaultValue = "100") @PageSize int limit,
                                                   @RequestParam(defaultValue = "0") @PageNumber int page) {
        return PageResponse.of(storageRepository.outOfStock(PageRequest.of(page, limit)), StockResponse::of);
    }
}
