package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Balance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface BalanceRepository extends JpaRepository<Balance, Integer> {
    Page<Balance> findAllByAccount_IdOrderByAddedDescIdDesc(int id, Pageable pageable);
    @Query("SELECT sum(amount) FROM Balance")
    BigDecimal totalBalance();
}
