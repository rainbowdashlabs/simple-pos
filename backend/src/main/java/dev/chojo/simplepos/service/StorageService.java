package dev.chojo.simplepos.service;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.InboundStorage;
import dev.chojo.simplepos.entity.dto.InventoryCorrection;
import dev.chojo.simplepos.entity.dto.StorageSummary;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.service.storage.StorageTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The shape of deliveries and counts (present fields, positive amounts, limits) is checked by the request validation
 * at the API, not here again; a new caller has to pass validated requests.
 */
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
     * @throws BadRequestException if the ingredient id is missing, or the cost, unit price or pledge is too large for
     *                             the money columns; the rest of the delivery's shape is checked by the request
     *                             validation
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if the ingredient does not exist
     */
    @Transactional
    public Storage deliver(InboundStorage delivery, User receivedBy) {
        Ingredient ingredient = ingredientRepository.require(delivery.ingredientId());
        BigDecimal cost = delivery.total() != null
                ? Money.of(delivery.total())
                : Money.of(delivery.price().multiply(BigDecimal.valueOf(delivery.amount())));
        if (Money.integerDigits(cost) > 10 || Money.integerDigits(Money.perUnit(cost, delivery.amount())) > 8
            || Money.integerDigits(delivery.pledge()) > 10) {
            throw new BadRequestException("The price or pledge is too large");
        }

        if (cost.signum() > 0) {
            String note = "%s x %d".formatted(ingredient.getName(), delivery.amount());
            cashRepository.save(new Cash(receivedBy, cost.negate(), CashType.PURCHASE, note));
        }
        BigDecimal pledge = Money.of(delivery.pledge());
        if (pledge.signum() > 0) {
            cashRepository.save(new Cash(receivedBy, pledge.negate(), CashType.PLEDGE, ingredient.getName()));
        }
        Instant purchased = delivery.purchased() == null ? Instant.now() : delivery.purchased();
        Storage created = storageRepository.save(Storage.withCost(ingredient, purchased, cost, delivery.amount()));
        if (cost.signum() > 0) {
            BigDecimal unitPrice = delivery.price() != null ? Money.unit(delivery.price()) : created.getPrice();
            ingredientRepository.updateByIdIs(ingredient.getId(), unitPrice);
        }
        return created;
    }

    /**
     * Stores a batch and makes its unit cost the ingredient's default price.
     *
     * @throws BadRequestException if the batch holds no units or more sold units than it holds
     */
    @Transactional
    public Storage addStorage(Storage storage) {
        if (storage.getAmount() < 1 || storage.getSold() < 0 || storage.getSold() > storage.getAmount()) {
            throw new BadRequestException("A batch needs at least one unit and cannot have sold more than it holds");
        }
        Storage created = storageRepository.save(storage);
        if (created.getCost().signum() > 0) {
            ingredientRepository.updateByIdIs(storage.getIngredient().getId(), storage.getPrice());
        }
        return created;
    }

    /**
     * Brings the stock of each ingredient to the counted amount, taking from or adding to storage as needed.
     * Runs in one transaction: if any correction is invalid, nothing is changed.
     *
     * @throws dev.chojo.simplepos.controller.exceptions.NotFoundException if a correction names an unknown ingredient;
     *                                                                     ingredient and amount being present and the
     *                                                                     amount not negative is checked by the
     *                                                                     request validation
     */
    @Transactional
    public void processInventoryCorrection(List<InventoryCorrection> corrections) {
        ingredientRepository.requireLockedInOrder(corrections.stream().map(InventoryCorrection::ingredient).toList());
        for (InventoryCorrection correction : corrections) {
            Ingredient ingredient = ingredientRepository.require(correction.ingredient());
            StorageSummary summary = new StorageSummary(storageRepository.summaryById(ingredient.getId()));
            int delta = summary.getStock() - correction.amount();
            if (delta == 0) continue;
            if (delta > 0) {
                takeStorage(ingredient, delta);
            } else {
                storageRepository.save(new Storage(null, ingredient, Instant.now(), ingredient.getPrice(), Math.abs(delta), 0));
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
     * @return one transaction per storage batch that units were taken from, oldest first
     */
    @Transactional
    public List<StorageTransaction> takeStorage(Ingredient ingredient, int amount) {
        ingredientRepository.requireLocked(ingredient.getId());
        List<StorageTransaction> taken = new ArrayList<>();
        int missing = amount;
        while (missing > 0) {
            Optional<Integer> next = storageRepository.findNextIngredient(ingredient.getId());
            if (next.isEmpty()) break;
            Storage storage = storageRepository.findById(next.get())
                    .orElseThrow(() -> new IllegalStateException("Storage %d vanished while taking stock".formatted(next.get())));
            Storage.StockTaken stock = storage.take(missing);
            taken.add(new StorageTransaction(storageRepository.saveAndFlush(storage), stock.units(), stock.cost()));
            missing -= stock.units();
        }
        return taken;
    }
}
