package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Balance;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BalanceRepository extends JpaRepository<Balance, Integer> {
    List<Balance> findAllByAccount_IdOrderByAddedDesc(int id, Pageable pageable);
    @Query("SELECT sum(amount) FROM Balance")
    Double totalBalance();

    @Query("SELECT sum(s.amount) FROM (SELECT sum(amount) as amount FROM Balance GROUP BY account) s WHERE s.amount < 0")
    Double totalDept();
}
