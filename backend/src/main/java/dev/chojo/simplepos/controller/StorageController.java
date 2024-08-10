package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.InboundStorage;
import dev.chojo.simplepos.entity.dto.InventoryCorrection;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import dev.chojo.simplepos.entity.response.Listing;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.StorageRepository;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/storage")
public class StorageController {
    private final StorageRepository storageRepository;
    private final IngredientRepository ingredientRepository;
    private final StorageService storageService;
    private final CashRepository cashRepository;

    public StorageController(StorageRepository storageRepository, IngredientRepository ingredientRepository, StorageService storageService, CashRepository cashRepository) {
        this.storageRepository = storageRepository;
        this.ingredientRepository = ingredientRepository;
        this.storageService = storageService;
        this.cashRepository = cashRepository;
    }

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<Storage> getSummary() {
        // TODO: Probably group by ingredient.
        // Ingredient is not contained in response, so this is quite useless at the moment
        return storageRepository.findAll();
    }

    @GetMapping("/stock/{id}")
    @ResponseStatus(HttpStatus.OK)
    public StorageSummary getStock(@PathVariable int id) {
        return storageRepository.summaryById(id);
    }

    @GetMapping("/stock")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Listing<StorageSummary>> getStock() {
        return ResponseEntity.ok(Listing.map(storageRepository.summary(), e -> e.getIngredient().getCategory()));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Storage>> byId(@PathVariable int id,
                                              @RequestParam(value = "limit", defaultValue = "100") int limit,
                                              @RequestParam(value = "page", defaultValue = "0") int page) {
        Optional<Ingredient> ingredient = ingredientRepository.findById(id);
        return ingredient.map(value -> ResponseEntity.ok(storageRepository.findAllByIngredient(value, PageRequest.of(page, limit), Sort.by("id").descending())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Storage> add(InboundStorage storage) {
        var current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Ingredient> ingredient = ingredientRepository.findById(storage.ingredient().getId());
        if (ingredient.isEmpty()) return ResponseEntity.notFound().build();
        if (storage.price() > 0) {
            cashRepository.save(new Cash(current, storage.price(), "purchase", "%s x %d".formatted(storage.ingredient().getName(), storage.amount())));
        }
        if (storage.pledge() > 0) {
            cashRepository.save(new Cash(current, storage.pledge(), "pledge", "%s".formatted(storage.ingredient().getName())));
        }
        return ResponseEntity.accepted().body(storageService.addStorage(new Storage(null, ingredient.get(), storage.purchased(), storage.price(), storage.amount(), 0)));
    }

    @PutMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public void updateInventory(@RequestBody List<InventoryCorrection> corrections) {
        storageService.processInventoryCorrection(corrections);
    }
}
