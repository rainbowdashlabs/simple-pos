package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.TillCount;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.TillCountRequest;
import dev.chojo.simplepos.repository.BalanceRepository;
import dev.chojo.simplepos.repository.CashRepository;
import dev.chojo.simplepos.repository.TillCountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    public CashService(CashRepository cashRepository, BalanceRepository balanceRepository,
                       TillCountRepository tillCountRepository) {
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
}
