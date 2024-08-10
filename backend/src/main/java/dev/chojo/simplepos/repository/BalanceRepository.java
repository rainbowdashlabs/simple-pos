package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Balance;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceRepository extends JpaRepository<Balance, Integer> {
    List<Balance> findAllByAccount_IdOrderByAddedDesc(int id, Pageable pageable);
}
