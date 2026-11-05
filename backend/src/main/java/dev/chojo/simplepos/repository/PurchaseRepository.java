package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Purchase;
import jakarta.persistence.QueryHint;
import jakarta.persistence.Tuple;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    @Query("SELECT p.account.id FROM Purchase p WHERE p.id = ?1")
    Optional<Integer> findAccountIdOf(int purchaseId);

    /**
     * An account's purchases, newest first, with their products and cashiers, in one query.
     */
    @EntityGraph(attributePaths = {"product", "product.category", "product.recipe", "soldBy"})
    List<Purchase> findAllByAccount_IdOrderByPurchasedDesc(int id, Pageable pageable);

    @Query(value = """
            SELECT
               b.day, b.sales, b.revenue, b.profit
            FROM (
                SELECT
                  cast(p.purchased AT TIME ZONE ?3 AS date) as day, sum(p.amount) as sales, sum(p.revenue) as revenue, sum(p.revenue - p.raw_price) as profit
                FROM
                  (
                    SELECT p.purchased as purchased, p.amount as amount, p.price * p.amount as revenue, coalesce(raw.raw_price,0) as raw_price
                    FROM Purchase p
                    LEFT JOIN
                      (
                        SELECT pp.purchase_id as id, coalesce(sum(pp.cost), 0) as raw_price
                        FROM purchase_part pp
                        GROUP BY pp.purchase_id
                      ) raw ON p.id = raw.id
                    WHERE p.product_id = ?1
                      AND p.purchased > ?2
                  ) p
                GROUP BY day
                ORDER BY day DESC
            ) b
            """, nativeQuery = true)
    List<Tuple> getSalesStatByProduct(int id, Instant after, String zone, Pageable pageable);

    @Query(value = """
            SELECT
              coalesce(sum(p.amount),0 ) as sales,
              coalesce(sum(p.revenue),0) as revenue,
              coalesce(sum(p.revenue - p.raw_price), 0) as profit
            FROM
              (
                SELECT p.amount as amount, p.price * p.amount as revenue, coalesce(raw.raw_price, 0) as raw_price
                FROM Purchase p
                LEFT JOIN
                  (
                    SELECT pp.purchase_id as id, coalesce(sum(pp.cost), 0) as raw_price
                    FROM purchase_part pp
                    GROUP BY pp.purchase_id
                  ) raw ON p.id = raw.id
                WHERE p.product_id = ?1
                    AND purchased > ?2
              ) p
              ORDER BY sales DESC
            """, nativeQuery = true)
    Tuple getSalesStatSummary(int id, Instant after);

    @Query("SELECT product, sum(amount) as amount FROM Purchase WHERE purchased > ?1 GROUP BY product ORDER BY amount DESC")
    List<Tuple> getTopSalesAmount(Instant after, Pageable pageable);

    @Query(value = """
            SELECT
              p.product_id as product_id,
              coalesce(sum(p.amount), 0) as sales,
              coalesce(sum(p.revenue), 0) as revenue,
              coalesce(sum(p.revenue - p.raw_price), 0) as profit
            FROM
              (
                SELECT p.product_id, p.amount as amount, p.price * p.amount as revenue, coalesce(raw.raw_price, 0) as raw_price
                FROM Purchase p
                LEFT JOIN
                  (
                    SELECT pp.purchase_id as id, coalesce(sum(pp.cost), 0) as raw_price
                    FROM purchase_part pp
                    GROUP BY pp.purchase_id
                  ) raw ON p.id = raw.id
                WHERE purchased > ?1
              ) p
            GROUP BY p.product_id
            """, nativeQuery = true)
    List<Tuple> getTopSales(Instant after, Pageable pageable);

    @Query(value = """
            SELECT
                cast(p.purchased AT TIME ZONE ?2 AS date) as day,
                cast(coalesce(sum(p.amount), 0) as integer) as sales,
                coalesce(sum(p.price * p.amount), 0) as revenue,
                coalesce(sum(p.price * p.amount - coalesce(raw.raw_price, 0)), 0) as profit
            FROM purchase p
            LEFT JOIN (
                SELECT pp.purchase_id as id, coalesce(sum(pp.cost), 0) as raw_price
                FROM purchase_part pp
                GROUP BY pp.purchase_id
            ) raw ON p.id = raw.id
            WHERE p.purchased > ?1
            GROUP BY day
            ORDER BY day ASC
            """, nativeQuery = true)
    List<Tuple> getDailyStats(Instant after, String zone);

    @Query(value = """
            SELECT
                coalesce(c.id, 0) as category_id,
                c.name as category_name,
                cast(coalesce(sum(p.amount), 0) as integer) as sales,
                coalesce(sum(p.price * p.amount), 0) as revenue
            FROM purchase p
            JOIN product pr ON p.product_id = pr.id
            LEFT JOIN category c ON pr.category_id = c.id
            WHERE p.purchased > ?1
            GROUP BY c.id, c.name
            ORDER BY revenue DESC
            """, nativeQuery = true)
    List<Tuple> getSalesByCategory(Instant after);

    @Query(value = """
            SELECT
              pr.id as product_id,
              coalesce(sum(p.amount), 0) as sales,
              coalesce(sum(p.price * p.amount), 0) as revenue,
              coalesce(sum(p.price * p.amount - coalesce(raw.raw_price, 0)), 0) as profit
            FROM product pr
            LEFT JOIN purchase p ON p.product_id = pr.id AND p.purchased > ?1
            LEFT JOIN (
                SELECT pp.purchase_id as id, coalesce(sum(pp.cost), 0) as raw_price
                FROM purchase_part pp
                GROUP BY pp.purchase_id
            ) raw ON p.id = raw.id
            WHERE pr.active = true AND pr.deleted = false
            GROUP BY pr.id
            ORDER BY sales ASC, pr.name ASC
            """, nativeQuery = true)
    List<Tuple> getBottomSales(Instant after, Pageable pageable);

    @Query("SELECT sum(price * amount) FROM Purchase")
    BigDecimal totalPurchases();

    @Query("SELECT sum(price * amount) FROM Purchase WHERE purchased > ?1")
    BigDecimal totalPurchases(Instant after);

    /**
     * The purchases made in {@code [from, to)}, oldest first, with account, product and cashier, read as a stream.
     */
    @QueryHints({@QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "500"), @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")})
    @Query("SELECT p FROM Purchase p LEFT JOIN FETCH p.account LEFT JOIN FETCH p.product LEFT JOIN FETCH p.soldBy "
           + "WHERE p.purchased >= ?1 AND p.purchased < ?2 ORDER BY p.purchased, p.id")
    Stream<Purchase> streamBetween(Instant from, Instant to);
}
