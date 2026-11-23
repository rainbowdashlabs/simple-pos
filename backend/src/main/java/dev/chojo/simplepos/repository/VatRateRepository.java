package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.VatRate;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VatRateRepository extends LookupRepository<VatRate> {
    @Override
    default String entityName() {
        return "VatRate";
    }

    /**
     * Every rate, active ones first, then by rate from high to low.
     */
    @Query("SELECT v FROM VatRate v ORDER BY v.active DESC, v.rate DESC, v.name")
    List<VatRate> findAllOrdered();

    /**
     * The active rates, by rate from high to low.
     */
    @Query("SELECT v FROM VatRate v WHERE v.active = true ORDER BY v.rate DESC, v.name")
    List<VatRate> findActive();

    @Query("SELECT v FROM VatRate v WHERE v.defaultRate = true")
    Optional<VatRate> findDefault();

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

    boolean existsByNameIgnoreCase(String name);

    /**
     * Takes the default mark off every rate, so another one can get it without breaking the one-default index.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE VatRate v SET v.defaultRate = false WHERE v.defaultRate = true")
    void clearDefault();
}
