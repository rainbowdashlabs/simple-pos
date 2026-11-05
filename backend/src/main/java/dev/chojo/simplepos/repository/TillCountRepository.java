package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.TillCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TillCountRepository extends JpaRepository<TillCount, Integer> {
    /**
     * Counts, newest first, with the user who counted, in one query.
     */
    @EntityGraph(attributePaths = "countedBy")
    Page<TillCount> findByOrderByCreatedDescIdDesc(Pageable pageable);

    /**
     * Waits until no other transaction is counting the till, and holds that lock until the surrounding transaction
     * ends, so two counts at once cannot both book the same difference.
     */
    @Query(value = "SELECT 1 FROM pg_advisory_xact_lock(hashtext('till_count'))", nativeQuery = true)
    int lockForCount();
}
