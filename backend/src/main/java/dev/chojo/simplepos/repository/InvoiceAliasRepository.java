package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.InvoiceAlias;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceAliasRepository extends JpaRepository<InvoiceAlias, Integer> {
    Optional<InvoiceAlias> findByGtin(String gtin);

    @Query("SELECT a FROM InvoiceAlias a WHERE a.gtin IS NULL AND a.text = :text")
    Optional<InvoiceAlias> findByTextWithoutGtin(@Param("text") String text);

    /**
     * The aliases with this GTIN or a text starting with this prefix, most recently used first. The prefix must be
     * {@link InvoiceAlias#normalize normalized} and have LIKE's wildcards and {@code !} escaped with {@code !}.
     */
    @Query("""
            SELECT a FROM InvoiceAlias a
            LEFT JOIN FETCH a.ingredient i LEFT JOIN FETCH i.category LEFT JOIN FETCH i.vatRate
            WHERE a.gtin = :gtin OR a.text LIKE CONCAT(:prefix, '%') ESCAPE '!'
            ORDER BY CASE WHEN a.gtin = :gtin THEN 0 ELSE 1 END, a.lastUsed DESC, a.id""")
    List<InvoiceAlias> search(@Param("gtin") String gtin, @Param("prefix") String prefix, Pageable page);
}
