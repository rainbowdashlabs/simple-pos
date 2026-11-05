package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface CashRepository extends JpaRepository<Cash, Integer> {
    @EntityGraph(attributePaths = "user")
    List<Cash> findByOrderByCreatedDesc(Pageable pageable);

    @Query("SELECT sum(amount) FROM Cash")
    BigDecimal totalAmount();

    @Query("SELECT sum(amount) FROM Cash WHERE type = ?1")
    BigDecimal totalAmount(CashType type);
}
