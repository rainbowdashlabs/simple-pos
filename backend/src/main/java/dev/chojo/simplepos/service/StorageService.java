package dev.chojo.simplepos.service;

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
import java.util.Optional;

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

    public void processInventoryCorrection(List<InventoryCorrection> corrections) {
        for (InventoryCorrection correction : corrections) {
            Optional<Ingredient> optIngredient = ingredientRepository.findById(correction.ingredient());
            if (optIngredient.isEmpty()) continue;
            StorageSummary summary = new StorageSummary(storageRepository.summaryById(correction.ingredient()));
            int delta = summary.getStock() - correction.amount();
            if (delta == 0) continue;
            if (delta > 0) {
                takeStorage(optIngredient.get(), delta);
            } else {
                addStorage(new Storage(null, summary.getIngredient(), Instant.now(), 0, Math.abs(delta), 0));
            }
        }
    }

    public synchronized Collection<StorageTransaction> takeStorage(Ingredient ingredient, int amount) {
        Map<Integer, StorageTransaction> taken = new HashMap<>();
        for (int i = 0; i < amount; i++) {
            var optNext = storageRepository.findNextIngredient(ingredient.getId());
            optNext.ifPresent(id -> {
                Storage storage = storageRepository.findById(id).get();
                storage.take();
                taken.computeIfAbsent(storage.getId(), s -> new StorageTransaction()).update(storageRepository.save(storage));
            });
        }
        return taken.values();
    }
}
