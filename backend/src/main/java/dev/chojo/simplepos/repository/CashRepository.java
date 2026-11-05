package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Cash;
import dev.chojo.simplepos.entity.CashType;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.Stream;

public interface CashRepository extends JpaRepository<Cash, Integer> {
    @EntityGraph(attributePaths = "user")
    Page<Cash> findByOrderByCreatedDescIdDesc(Pageable pageable);

    @Query("SELECT sum(amount) FROM Cash")
    BigDecimal totalAmount();

    @Query("SELECT sum(amount) FROM Cash WHERE type = ?1")
    BigDecimal totalAmount(CashType type);

    /**
     * The cash entries booked in {@code [from, to)}, oldest first, with their users, read as a stream.
     */
    @QueryHints({@QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "500"), @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")})
    @Query("SELECT c FROM Cash c LEFT JOIN FETCH c.user WHERE c.created >= ?1 AND c.created < ?2 ORDER BY c.created, c.id")
    Stream<Cash> streamBetween(Instant from, Instant to);
}
