package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.dto.ProductSalesStatDto;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    List<Purchase> findAllByAccount_IdOrderByPurchasedDesc(int id, Pageable pageable);

    List<Purchase> findAllByProduct_IdOrderByPurchasedDesc(int id, Pageable pageable);

    @Query(value = """
            SELECT
               b.day, b.sales, b.revenue, b.profit
            FROM (
                SELECT
                  date_trunc('day', p.purchased) as day, sum(p.amount) as sales, sum(p.revenue) as revenue, sum(p.revenue - p.raw_price) as profit
                FROM
                  (
                    SELECT p.purchased as purchased, p.amount as amount, p.price * p.amount as revenue, coalesce(raw.raw_price,0) as raw_price
                    FROM Purchase p
                    LEFT JOIN
                      (
                        SELECT pp.purchase_id as id, coalesce(sum(s.price), 0) as raw_price
                        FROM purchase_part pp
                        LEFT JOIN storage s
                        ON pp.storage_id = s.id
                        GROUP BY pp.purchase_id
                      ) raw ON p.id = raw.id
                    WHERE p.product_id = ?1
                  ) p
                GROUP BY day
                ORDER BY day DESC
            ) b
            """, nativeQuery = true)
    List<Tuple> getSalesStatByProduct(int id, Pageable pageable);
}
