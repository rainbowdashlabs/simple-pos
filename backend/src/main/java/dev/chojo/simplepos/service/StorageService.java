package dev.chojo.simplepos.service;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.dto.InventoryCorrection;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.service.storage.StorageTransaction;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class StorageService {
    private final StorageRepository storageRepository;
    private final IngredientRepository ingredientRepository;

    public StorageService(StorageRepository storageRepository, IngredientRepository ingredientRepository) {
        this.storageRepository = storageRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional
    public Storage addStorage(Storage storage) {
        Storage created = storageRepository.save(storage);
        if (created.getPrice() > 0) {
            ingredientRepository.updateByIdIs(storage.getIngredient().getId(), storage.getPrice());
        }
        return created;
    }

    /**
     * Brings the stock of each ingredient to the counted amount, taking from or adding to storage as needed.
     * Runs in one transaction: if any correction is invalid, nothing is changed.
     *
     * @throws BadRequestException if a correction lacks its ingredient or amount
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if a correction names an unknown ingredient
     */
    @Transactional
    public void processInventoryCorrection(List<InventoryCorrection> corrections) {
        ingredientRepository.requireLockedInOrder(corrections.stream().map(InventoryCorrection::ingredient).toList());
        for (InventoryCorrection correction : corrections) {
            Ingredient ingredient = ingredientRepository.require(correction.ingredient());
            if (correction.amount() == null) throw new BadRequestException("Amount is missing");
            StorageSummary summary = new StorageSummary(storageRepository.summaryById(ingredient.getId()));
            int delta = summary.getStock() - correction.amount();
            if (delta == 0) continue;
            if (delta > 0) {
                takeStorage(ingredient, delta);
            } else {
                addStorage(new Storage(null, summary.getIngredient(), Instant.now(), 0, Math.abs(delta), 0));
            }
        }
    }

    /**
     * Takes up to {@code amount} units of the ingredient from storage, oldest batch first. Takes less when the stock
     * runs out.
     * <p>
     * The ingredient row stays locked until the surrounding transaction commits, so concurrent purchases and
     * inventory corrections of the same ingredient cannot overwrite each other's stock changes.
     *
     * @return one transaction per storage batch that units were taken from
     */
    @Transactional
    public Collection<StorageTransaction> takeStorage(Ingredient ingredient, int amount) {
        ingredientRepository.requireLocked(ingredient.getId());
        Map<Integer, StorageTransaction> taken = new HashMap<>();
        for (int i = 0; i < amount; i++) {
            var optNext = storageRepository.findNextIngredient(ingredient.getId());
            optNext.ifPresent(id -> {
                Storage storage = storageRepository.findById(id)
                        .orElseThrow(() -> new IllegalStateException("Storage %d vanished while taking stock".formatted(id)));
                storage.take();
                taken.computeIfAbsent(storage.getId(), s -> new StorageTransaction()).update(storageRepository.saveAndFlush(storage));
            });
        }
        return taken.values();
    }
}
