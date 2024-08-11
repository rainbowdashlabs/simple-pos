package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.PurchasePart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchasePartRepository extends JpaRepository<PurchasePart, Integer> {
}
