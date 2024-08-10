package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Purchase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    List<Purchase> findAllByAccount_IdOrderByPurchasedDesc(int id, Pageable pageable);
}
