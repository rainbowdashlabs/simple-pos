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
import dev.chojo.simplepos.service.StockProjectionService;
import dev.chojo.simplepos.service.StorageService;
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
    @ResponseStatus(HttpStatus.OK)
    public List<Storage> getSummary(@RequestParam(defaultValue = "100") @PageSize int limit,
                                    @RequestParam(defaultValue = "0") @PageNumber int page) {
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
        return ResponseEntity.ok(stockProjectionService.soonestEmpty());
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
