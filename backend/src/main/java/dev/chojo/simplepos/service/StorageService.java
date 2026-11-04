package dev.chojo.simplepos.service;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.InboundStorage;
import dev.chojo.simplepos.entity.dto.InventoryCorrection;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import dev.chojo.simplepos.repository.CashRepository;
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
    private final CashRepository cashRepository;

    public StorageService(StorageRepository storageRepository, IngredientRepository ingredientRepository,
                          CashRepository cashRepository) {
        this.storageRepository = storageRepository;
        this.ingredientRepository = ingredientRepository;
        this.cashRepository = cashRepository;
    }

    /**
     * Books a delivery: the new stock, and the money paid for it and for its pledge, in one transaction. A delivery
     * without a purchase date counts as purchased now, so it is used up in order with the others.
     *
     * @throws BadRequestException if the ingredient is missing, the amount is not positive, or the price or pledge
     *                             is missing or negative
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if the ingredient does not exist
     */
    @Transactional
    public Storage deliver(InboundStorage delivery, User receivedBy) {
        Ingredient ingredient = ingredientRepository.require(delivery.ingredientId());
        if (delivery.amount() == null || delivery.amount() < 1) throw new BadRequestException("The amount must be at least 1");
        if (delivery.price() == null || delivery.price() < 0) throw new BadRequestException("The price must not be negative");
        if (delivery.pledge() == null || delivery.pledge() < 0) throw new BadRequestException("The pledge must not be negative");

        if (delivery.price() > 0) {
            String note = "%s x %d".formatted(ingredient.getName(), delivery.amount());
            cashRepository.save(new Cash(receivedBy, -delivery.price() * delivery.amount(), "purchase", note));
        }
        if (delivery.pledge() > 0) {
            cashRepository.save(new Cash(receivedBy, -delivery.pledge(), "pledge", ingredient.getName()));
        }
        Instant purchased = delivery.purchased() == null ? Instant.now() : delivery.purchased();
        return addStorage(new Storage(null, ingredient, purchased, delivery.price(), delivery.amount(), 0));
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
