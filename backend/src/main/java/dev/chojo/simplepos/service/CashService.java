package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.TillCount;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.EmptiesReturn;
import dev.chojo.simplepos.entity.dto.TillCountRequest;
import dev.chojo.simplepos.exceptions.BadRequestException;
import dev.chojo.simplepos.exceptions.ErrorCode;
import dev.chojo.simplepos.repository.AccountBalanceRepository;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.IngredientRepository;
import dev.chojo.simplepos.repository.TillCountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The till: what it should hold, and counting what it does hold.
 * <p>
 * The till total is the sum of all cash entries and all account deposits. Purchases on accounts do not move it,
 * because their money came in with the deposit.
 */
@Service
public class CashService {
    private final CashRepository cashRepository;
    private final BalanceRepository balanceRepository;
    private final TillCountRepository tillCountRepository;
    private final IngredientRepository ingredientRepository;
    private final AccountBalanceRepository accountBalanceRepository;

    public CashService(CashRepository cashRepository, BalanceRepository balanceRepository,
                       TillCountRepository tillCountRepository, IngredientRepository ingredientRepository,
                       AccountBalanceRepository accountBalanceRepository) {
        this.accountBalanceRepository = accountBalanceRepository;
        this.ingredientRepository = ingredientRepository;
        this.cashRepository = cashRepository;
        this.balanceRepository = balanceRepository;
        this.tillCountRepository = tillCountRepository;
    }

    /**
     * What the till should hold.
     */
    @Transactional(readOnly = true)
    public BigDecimal tillTotal() {
        return Money.orZero(cashRepository.totalAmount()).add(Money.orZero(balanceRepository.totalBalance()));
    }

    /**
     * Stores a till count and books the difference to the expected total as a correction, so the till total matches
     * the count afterwards. Nothing is booked when the count matches. Counts are taken one at a time.
     */
    @Transactional
    public TillCount count(TillCountRequest request, User countedBy) {
        tillCountRepository.lockForCount();
        Map<Integer, Integer> denominations = new TreeMap<>(request.denominations());
        long cents = denominations.entrySet().stream()
                .mapToLong(entry -> (long) entry.getKey() * entry.getValue())
                .sum();
        BigDecimal counted = Money.of(BigDecimal.valueOf(cents, 2));
        BigDecimal expected = tillTotal();
        TillCount count = tillCountRepository.save(new TillCount(counted, expected, denominations, countedBy));
        BigDecimal difference = count.getDifference();
        if (difference.signum() != 0) {
            String note = "Till count: counted %s, expected %s".formatted(counted.toPlainString(), expected.toPlainString());
            cashRepository.save(new Cash(countedBy, difference, CashType.CORRECTION, note));
        }
        return count;
    }

    /**
     * Books the deposit refunded for empties given back to the supplier, one pledge entry per ingredient, in one
     * transaction. The refund is the bottles times the ingredient's bottle deposit plus the crates times its crate
     * deposit. More may be returned than is outstanding: the supplier decides what is refunded.
     *
     * @return the total refunded
     * @throws BadRequestException if the list is empty, names an ingredient twice or one that does not exist, returns
     *                             bottles or crates of an ingredient without that deposit, or a return refunds nothing
     *                             or more than a money column holds
     */
    @Transactional
    public BigDecimal returnEmpties(List<EmptiesReturn> returns, User returnedBy) {
        if (returns.isEmpty()) throw new BadRequestException(ErrorCode.NOTHING_RETURNED);
        if (new HashSet<>(returns.stream().map(EmptiesReturn::ingredientId).toList()).size() != returns.size()) {
            throw new BadRequestException(ErrorCode.INGREDIENT_LISTED_TWICE);
        }
        BigDecimal total = Money.ZERO;
        for (EmptiesReturn empties : returns) {
            Ingredient ingredient = ingredientRepository.requireReferenced(empties.ingredientId());
            BigDecimal refund = refund(ingredient, empties);
            String note = "Empties returned: %s (%d bottles, %d crates)".formatted(ingredient.getName(), empties.bottles(), empties.crates());
            cashRepository.save(new Cash(returnedBy, refund, CashType.PLEDGE, note));
            total = total.add(refund);
        }
        return total;
    }

    private static BigDecimal refund(Ingredient ingredient, EmptiesReturn empties) {
        BigDecimal bottleDeposit = Money.orZero(ingredient.getPledge());
        BigDecimal crateDeposit = Money.orZero(ingredient.getPledgeContainer());
        if (empties.bottles() > 0 && bottleDeposit.signum() == 0) {
            throw new BadRequestException(ErrorCode.NO_BOTTLE_DEPOSIT, ingredient.getName());
        }
        if (empties.crates() > 0 && crateDeposit.signum() == 0) {
            throw new BadRequestException(ErrorCode.NO_CRATE_DEPOSIT, ingredient.getName());
        }
        BigDecimal refund = Money.of(bottleDeposit.multiply(BigDecimal.valueOf(empties.bottles()))
                .add(crateDeposit.multiply(BigDecimal.valueOf(empties.crates()))));
        if (refund.signum() <= 0) throw new BadRequestException(ErrorCode.NOTHING_RETURNED_FOR, ingredient.getName());
        if (Money.integerDigits(refund) > 10) throw new BadRequestException(ErrorCode.REFUND_TOO_LARGE);
        return refund;
    }

    /**
     * What all accounts together hold: their deposits minus their purchases.
     */
    @Transactional(readOnly = true)
    public BigDecimal accountsTotal() {
        return Money.orZero(accountBalanceRepository.totalBalance());
    }

    /**
     * The deposit paid for empties that is still out with the supplier, as a negative amount.
     */
    @Transactional(readOnly = true)
    public BigDecimal pledgeTotal() {
        return Money.orZero(cashRepository.totalAmount(CashType.PLEDGE));
    }
}
