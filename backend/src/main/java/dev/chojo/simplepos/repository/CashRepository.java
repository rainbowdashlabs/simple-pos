package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Cash;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CashRepository extends JpaRepository<Cash, Integer> {
    List<Cash> findByOrderByCreatedDesc(Pageable pageable);

    @Query("SELECT sum(amount) FROM Cash")
    Double totalAmount();

    @Query("SELECT sum(amount) FROM Cash WHERE type = ?1")
    Double totalAmount(String type);
}
