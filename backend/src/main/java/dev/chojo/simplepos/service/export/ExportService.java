package dev.chojo.simplepos.service.export;

import dev.chojo.simplepos.configuration.BusinessTimeZone;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.exceptions.BadRequestException;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Stream;

/**
 * Exports sales, cash entries and stock batches of a range of business days as CSV, row by row as they are read, so
 * a long range is never held in memory: rows are fetched in batches and each is detached once written. Both days of
 * the range are included. The query runs before anything is written, so a failing query never produces a file.
 */
@Service
public class ExportService {
    static final int MAX_RANGE_YEARS = 3;

    private final PurchaseRepository purchaseRepository;
    private final CashRepository cashRepository;
    private final StorageRepository storageRepository;
    private final EntityManager entityManager;
    private final ZoneId zone;

    public ExportService(PurchaseRepository purchaseRepository, CashRepository cashRepository,
                         StorageRepository storageRepository, EntityManager entityManager,
                         BusinessTimeZone businessTimeZone) {
        this.purchaseRepository = purchaseRepository;
        this.cashRepository = cashRepository;
        this.storageRepository = storageRepository;
        this.entityManager = entityManager;
        this.zone = ZoneId.of(businessTimeZone.id());
    }

    /**
     * Refuses a range that ends before it starts or spans more than {@value #MAX_RANGE_YEARS} years.
     *
     * @throws BadRequestException if {@code to} is before {@code from} or too far after it
     */
    public static void requireRange(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) throw new BadRequestException("The range ends before it starts");
        if (to.isAfter(from.plusYears(MAX_RANGE_YEARS))) {
            throw new BadRequestException("The range may span at most %d years".formatted(MAX_RANGE_YEARS));
        }
    }

    /**
     * One row per purchase: when, which account (or "Guest"), product, amount, unit price, total and cashier.
     */
    @Transactional(readOnly = true)
    public void writeSales(LocalDate from, LocalDate to, CsvFormat format, Writer out) throws IOException {
        try (Stream<Purchase> purchases = purchaseRepository.streamBetween(start(from), end(to))) {
            CsvWriter csv = new CsvWriter(out, zone, format);
            csv.header("time", "account", "product", "amount", "unit_price", "total", "sold_by");
            purchases.forEach(purchase -> row(purchase, () -> csv
                    .dateTime(purchase.getPurchased())
                    .text(purchase.getAccount() == null ? "Guest" : purchase.getAccount().getName())
                    .text(purchase.getProduct() == null ? null : purchase.getProduct().getName())
                    .number(purchase.getAmount())
                    .number(purchase.getPrice())
                    .number(Money.of(purchase.getPrice().multiply(BigDecimal.valueOf(purchase.getAmount()))))
                    .text(purchase.getSoldBy() == null ? null : purchase.getSoldBy().getUsername())
                    .endRow()));
        }
    }

    /**
     * One row per cash entry: when, type, amount, note and the user who booked it.
     */
    @Transactional(readOnly = true)
    public void writeCash(LocalDate from, LocalDate to, CsvFormat format, Writer out) throws IOException {
        try (Stream<Cash> entries = cashRepository.streamBetween(start(from), end(to))) {
            CsvWriter csv = new CsvWriter(out, zone, format);
            csv.header("time", "type", "amount", "note", "user");
            entries.forEach(cash -> row(cash, () -> csv
                    .dateTime(cash.getCreated())
                    .text(cash.getType() == null ? null : cash.getType().value())
                    .number(cash.getAmount())
                    .text(cash.getNote())
                    .text(cash.getUser() == null ? null : cash.getUser().getUsername())
                    .endRow()));
        }
    }

    /**
     * One row per stock batch, which covers deliveries and units added by an inventory count: when, ingredient,
     * amount, what was paid in total and per unit.
     */
    @Transactional(readOnly = true)
    public void writeStock(LocalDate from, LocalDate to, CsvFormat format, Writer out) throws IOException {
        try (Stream<Storage> batches = storageRepository.streamBetween(start(from), end(to))) {
            CsvWriter csv = new CsvWriter(out, zone, format);
            csv.header("time", "ingredient", "amount", "cost", "unit_price");
            batches.forEach(batch -> row(batch, () -> csv
                    .dateTime(batch.getPurchased())
                    .text(batch.getIngredient().getName())
                    .number(batch.getAmount())
                    .number(batch.getCost())
                    .number(batch.getPrice())
                    .endRow()));
        }
    }

    private Instant start(LocalDate day) {
        return day.atStartOfDay(zone).toInstant();
    }

    private Instant end(LocalDate day) {
        return day.plusDays(1).atStartOfDay(zone).toInstant();
    }

    private void row(Object entity, IoAction action) {
        try {
            action.run();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        entityManager.detach(entity);
    }

    @FunctionalInterface
    private interface IoAction {
        void run() throws IOException;
    }
}
