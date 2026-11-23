package dev.chojo.simplepos.service;

import dev.chojo.simplepos.configuration.BusinessTimeZone;
import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CountUnit;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.InvoiceAlias;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Storage;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.VatRate;
import dev.chojo.simplepos.entity.dto.InvoicePosition;
import dev.chojo.simplepos.entity.dto.InvoicePosition.Kind;
import dev.chojo.simplepos.entity.dto.InvoiceRequest;
import dev.chojo.simplepos.entity.response.InvoiceResponse;
import dev.chojo.simplepos.entity.response.StorageResponse;
import dev.chojo.simplepos.exceptions.BadRequestException;
import dev.chojo.simplepos.exceptions.ErrorCode;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.StorageRepository;
import dev.chojo.simplepos.repository.VatRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Books a supplier's invoice in one transaction. Every line is computed here from its quantity and price; totals a
 * client may have shown are not trusted. Money is rounded per line, half up, as invoices do.
 * <p>
 * Each item becomes one storage batch whose cost is its gross line total minus the discounts pointing at it. All items
 * together are booked as one purchase in the cash log and all pledge lines as one pledge entry, so the log reads like
 * the invoice. Net prices get the line's VAT rate added: the rate given on the line, else the ingredient's rate, else
 * the default rate; pledge lines use the default rate unless they name one, and a discount uses the rate of its item.
 * <p>
 * The ingredients of the items are locked for the booking, in id order like every other multi-ingredient change,
 * because their price and VAT rate are written back and a concurrent edit must not be overwritten. The cash entries
 * carry the supplier and the purchase day in their note, since their own time is when the invoice was booked.
 * Every item and pledge line that carries its printed text or GTIN is remembered for the next invoice.
 */
@Service
public class InvoiceService {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final IngredientRepository ingredientRepository;
    private final StorageRepository storageRepository;
    private final CashRepository cashRepository;
    private final VatRateRepository vatRateRepository;
    private final BusinessTimeZone timeZone;
    private final InvoiceAliasService aliasService;

    public InvoiceService(IngredientRepository ingredientRepository, StorageRepository storageRepository,
                          CashRepository cashRepository, VatRateRepository vatRateRepository,
                          BusinessTimeZone timeZone, InvoiceAliasService aliasService) {
        this.ingredientRepository = ingredientRepository;
        this.storageRepository = storageRepository;
        this.cashRepository = cashRepository;
        this.vatRateRepository = vatRateRepository;
        this.timeZone = timeZone;
        this.aliasService = aliasService;
    }

    /**
     * Books the invoice.
     *
     * @throws BadRequestException if the invoice has no item, a line lacks what its kind needs or has what it must
     *                             not, an ingredient or VAT rate does not exist, a chosen VAT rate is inactive, an
     *                             item is counted in containers of an ingredient without a container size, a discount
     *                             does not point at an item or the discounts exceed their item's cost, an amount is
     *                             too large for the money columns, a VAT rate is needed and no default is set, or one
     *                             ingredient is given two different rates to keep
     */
    @Transactional
    public InvoiceResponse book(InvoiceRequest invoice, User bookedBy) {
        Booking booking = new Booking(invoice);
        List<InvoicePosition> positions = invoice.positions();
        if (positions.stream().noneMatch(position -> position.kind() == Kind.ITEM)) {
            throw new BadRequestException(ErrorCode.INVOICE_WITHOUT_ITEM);
        }
        for (int index = 0; index < positions.size(); index++) {
            checkShape(positions.get(index), index + 1);
        }
        ingredientRepository.requireReferencedLockedInOrder(positions.stream()
                .filter(position -> position.kind() == Kind.ITEM).map(InvoicePosition::ingredientId).toList());

        Map<Integer, Line> items = new HashMap<>();
        List<Line> itemOrder = new ArrayList<>();
        BigDecimal pledge = Money.ZERO;
        for (int index = 0; index < positions.size(); index++) {
            InvoicePosition position = positions.get(index);
            if (position.kind() == Kind.ITEM) {
                Line line = booking.item(position, index + 1);
                items.put(index, line);
                itemOrder.add(line);
            } else if (position.kind() == Kind.PLEDGE) {
                pledge = pledge.add(booking.gross(lineTotal(position), booking.pledgeRate(position)));
            }
        }
        for (int index = 0; index < positions.size(); index++) {
            InvoicePosition position = positions.get(index);
            if (position.kind() != Kind.DISCOUNT) continue;
            Line target = items.get(position.discountOf());
            if (target == null) throw new BadRequestException(ErrorCode.DISCOUNT_TARGET_INVALID, index + 1);
            target.cost = target.cost.subtract(booking.gross(position.amount(), target.rate));
        }

        checkRateWriteBacks(itemOrder);
        Instant purchased = invoice.purchased() == null ? Instant.now() : invoice.purchased();
        List<StorageResponse> batches = new ArrayList<>();
        BigDecimal goods = Money.ZERO;
        for (Line line : itemOrder) {
            if (line.cost.signum() < 0) throw new BadRequestException(ErrorCode.DISCOUNT_TOO_LARGE, line.number);
            if (!Money.fitsBatch(line.cost, line.units)) throw new BadRequestException(ErrorCode.AMOUNT_TOO_LARGE);
            Storage batch = storageRepository.save(Storage.withCost(line.ingredient, purchased, line.cost, line.units));
            if (line.cost.signum() > 0) line.ingredient.setPrice(batch.getPrice());
            if (line.writeBackRate) line.ingredient.setVatRate(line.rate.isDefaultRate() ? null : line.rate);
            batches.add(StorageResponse.of(batch));
            goods = goods.add(line.cost);
        }
        if (Money.integerDigits(goods.abs()) > 10 || Money.integerDigits(pledge.abs()) > 10) {
            throw new BadRequestException(ErrorCode.AMOUNT_TOO_LARGE);
        }

        String note = note(invoice.supplier(), purchased);
        if (goods.signum() != 0) cashRepository.save(new Cash(bookedBy, goods.negate(), CashType.PURCHASE, note));
        if (pledge.signum() != 0) cashRepository.save(new Cash(bookedBy, pledge.negate(), CashType.PLEDGE, note));
        rememberNames(positions, items, invoice.pricesNet());
        return new InvoiceResponse(batches, Money.of(goods), Money.of(pledge), Money.of(goods.add(pledge)));
    }

    private static BigDecimal lineTotal(InvoicePosition position) {
        return position.price().multiply(BigDecimal.valueOf(position.quantity()));
    }

    private void rememberNames(List<InvoicePosition> positions, Map<Integer, Line> items, boolean net) {
        Instant now = Instant.now();
        for (int index = 0; index < positions.size(); index++) {
            InvoicePosition position = positions.get(index);
            if (position.kind() == Kind.ITEM) {
                aliasService.remember(position.text(), position.gtin(), InvoiceAlias.Kind.ITEM,
                        items.get(index).ingredient, position.unit(), position.price(), net, now);
            } else if (position.kind() == Kind.PLEDGE) {
                aliasService.remember(position.text(), position.gtin(), InvoiceAlias.Kind.PLEDGE, null, null,
                        position.price(), net, now);
            }
        }
    }

    private String note(String supplier, Instant purchased) {
        LocalDate day = LocalDate.ofInstant(purchased, timeZone.zone());
        return supplier == null || supplier.isBlank() ? "Invoice " + day : "Invoice " + supplier.strip() + " " + day;
    }

    private static void checkRateWriteBacks(List<Line> items) {
        Map<Integer, Integer> kept = new HashMap<>();
        for (Line line : items) {
            if (!line.writeBackRate) continue;
            Integer previous = kept.putIfAbsent(line.ingredient.getId(), line.rate.getId());
            if (previous != null && !previous.equals(line.rate.getId())) {
                throw new BadRequestException(ErrorCode.VAT_RATE_CONFLICT, line.ingredient.getName());
            }
        }
    }

    private static void checkShape(InvoicePosition position, int number) {
        switch (position.kind()) {
            case ITEM -> {
                require(position.ingredientId(), "ingredientId", number);
                require(position.quantity(), "quantity", number);
                require(position.unit(), "unit", number);
                require(position.price(), "price", number);
                forbid(position.amount(), "amount", number);
                forbid(position.discountOf(), "discountOf", number);
                if (position.price().signum() < 0) throw new BadRequestException(ErrorCode.INVOICE_PRICE_NEGATIVE, number);
            }
            case PLEDGE -> {
                require(position.quantity(), "quantity", number);
                require(position.price(), "price", number);
                forbid(position.ingredientId(), "ingredientId", number);
                forbid(position.unit(), "unit", number);
                forbid(position.amount(), "amount", number);
                forbid(position.discountOf(), "discountOf", number);
                if (position.updatesIngredientRate()) throw unexpected(number, "updateIngredientRate");
            }
            case DISCOUNT -> {
                require(position.amount(), "amount", number);
                require(position.discountOf(), "discountOf", number);
                forbid(position.ingredientId(), "ingredientId", number);
                forbid(position.quantity(), "quantity", number);
                forbid(position.unit(), "unit", number);
                forbid(position.price(), "price", number);
                forbid(position.vatRateId(), "vatRateId", number);
                forbid(position.text(), "text", number);
                forbid(position.gtin(), "gtin", number);
                if (position.updatesIngredientRate()) throw unexpected(number, "updateIngredientRate");
            }
        }
    }

    private static void require(Object value, String field, int number) {
        if (value == null) throw new BadRequestException(ErrorCode.INVOICE_POSITION_INCOMPLETE, number, field);
    }

    private static void forbid(Object value, String field, int number) {
        if (value != null) throw unexpected(number, field);
    }

    private static BadRequestException unexpected(int number, String field) {
        return new BadRequestException(ErrorCode.INVOICE_POSITION_UNEXPECTED, number, field);
    }

    /**
     * An item line while the invoice is computed: its batch's units and its cost so far.
     */
    private static final class Line {
        private final int number;
        private final Ingredient ingredient;
        private final int units;
        private final VatRate rate;
        private final boolean writeBackRate;
        private BigDecimal cost;

        private Line(int number, Ingredient ingredient, int units, VatRate rate, boolean writeBackRate, BigDecimal cost) {
            this.number = number;
            this.ingredient = ingredient;
            this.units = units;
            this.rate = rate;
            this.writeBackRate = writeBackRate;
            this.cost = cost;
        }
    }

    /**
     * The state of one booking: whether its prices are net, and the default VAT rate, looked up once and only when a
     * line needs it.
     */
    private final class Booking {
        private final boolean pricesNet;
        private VatRate defaultRate;

        private Booking(InvoiceRequest invoice) {
            this.pricesNet = invoice.pricesNet();
        }

        private Line item(InvoicePosition position, int number) {
            Ingredient ingredient = ingredientRepository.requireReferenced(position.ingredientId());
            int units = position.quantity();
            if (position.unit() == CountUnit.CONTAINER) {
                if (ingredient.getContainerSize() <= 0) {
                    throw new BadRequestException(ErrorCode.CONTAINER_SIZE_MISSING, ingredient.getName());
                }
                try {
                    units = Math.multiplyExact(units, ingredient.getContainerSize());
                } catch (ArithmeticException e) {
                    throw new BadRequestException(ErrorCode.AMOUNT_TOO_LARGE);
                }
            }
            VatRate rate = itemRate(position, ingredient);
            return new Line(number, ingredient, units, rate, position.updatesIngredientRate(),
                    gross(lineTotal(position), rate));
        }

        private VatRate itemRate(InvoicePosition position, Ingredient ingredient) {
            if (!pricesNet && !position.updatesIngredientRate() && position.vatRateId() == null) return null;
            if (position.vatRateId() == null) {
                return ingredient.getVatRate() != null ? ingredient.getVatRate() : defaultRate();
            }
            VatRate chosen = vatRateRepository.requireReferenced(position.vatRateId());
            boolean current = ingredient.getVatRate() != null
                    && Objects.equals(ingredient.getVatRate().getId(), chosen.getId());
            if (!chosen.isActive() && !current) {
                throw new BadRequestException(ErrorCode.VAT_RATE_INACTIVE, chosen.getName());
            }
            return chosen;
        }

        private VatRate pledgeRate(InvoicePosition position) {
            if (position.vatRateId() != null) {
                VatRate chosen = vatRateRepository.requireReferenced(position.vatRateId());
                if (!chosen.isActive()) throw new BadRequestException(ErrorCode.VAT_RATE_INACTIVE, chosen.getName());
                return chosen;
            }
            return pricesNet ? defaultRate() : null;
        }

        private VatRate defaultRate() {
            if (defaultRate == null) {
                defaultRate = vatRateRepository.findDefault()
                        .orElseThrow(() -> new BadRequestException(ErrorCode.DEFAULT_VAT_RATE_MISSING));
            }
            return defaultRate;
        }

        private BigDecimal gross(BigDecimal net, VatRate rate) {
            if (!pricesNet) return Money.of(net);
            return Money.of(net.multiply(HUNDRED.add(rate.getRate())).divide(HUNDRED));
        }
    }
}
