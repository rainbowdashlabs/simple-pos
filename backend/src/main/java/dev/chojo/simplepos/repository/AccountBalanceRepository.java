package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Integer> {
    /**
     * What all accounts together hold: their deposits minus their purchases.
     */
    @Query("SELECT sum(b.balance) FROM AccountBalance b")
    BigDecimal totalBalance();
}
