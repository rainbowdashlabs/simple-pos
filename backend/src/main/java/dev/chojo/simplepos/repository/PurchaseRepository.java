package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.repository.projection.CategorySalesRow;
import dev.chojo.simplepos.repository.projection.DayRow;
import dev.chojo.simplepos.repository.projection.ProductSalesRow;
import dev.chojo.simplepos.repository.projection.TotalsRow;
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
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    /**
     * Pairs of purchase id and account id for the purchases that exist among {@code ids}; the account id is null for
     * a guest purchase.
     */
    @Query("SELECT p.id, a.id FROM Purchase p LEFT JOIN p.account a WHERE p.id IN ?1")
    List<Object[]> findAccountIdsOf(Collection<Integer> ids);

    /**
     * An account's purchases, newest first, with their products and cashiers, in one query; for tests that look at what
     * a sale booked. The history the API sends is read a page at a time through
     * {@link #findByAccount_IdOrderByPurchasedDescIdDesc}.
     */
    @EntityGraph(attributePaths = {"product", "product.category", "product.recipe", "soldBy"})
    List<Purchase> findAllByAccount_IdOrderByPurchasedDesc(int id, Pageable pageable);

    /**
     * A page of an account's purchases, newest first, with their products and cashiers.
     */
    @EntityGraph(attributePaths = {"product", "product.category", "product.recipe", "soldBy"})
    Page<Purchase> findByAccount_IdOrderByPurchasedDescIdDesc(int id, Pageable pageable);

    @Query(value = """
            SELECT
              cast(p.purchased AT TIME ZONE ?3 AS date) as "day",
              sum(p.amount) as "sales",
              sum(p.price * p.amount) as "revenue",
              sum(p.price * p.amount - p.cost) as "profit"
            FROM purchase p
            WHERE p.product_id = ?1
              AND p.purchased > ?2
            GROUP BY "day"
            ORDER BY "day" DESC
            """,
            countQuery = """
                    SELECT count(DISTINCT cast(p.purchased AT TIME ZONE ?3 AS date))
                    FROM purchase p WHERE p.product_id = ?1 AND p.purchased > ?2""",
            nativeQuery = true)
    Page<DayRow> getSalesStatByProduct(int id, Instant after, String zone, Pageable pageable);

    @Query(value = """
            SELECT
              coalesce(sum(p.amount), 0) as "sales",
              coalesce(sum(p.price * p.amount), 0) as "revenue",
              coalesce(sum(p.price * p.amount - p.cost), 0) as "profit"
            FROM purchase p
            WHERE p.product_id = ?1
              AND p.purchased > ?2
            """, nativeQuery = true)
    TotalsRow getSalesStatSummary(int id, Instant after);

    /**
     * Sales, revenue and profit of all products since {@code after}.
     */
    @Query(value = """
            SELECT
              coalesce(sum(p.amount), 0) as "sales",
              coalesce(sum(p.price * p.amount), 0) as "revenue",
              coalesce(sum(p.price * p.amount - p.cost), 0) as "profit"
            FROM purchase p
            WHERE p.purchased > ?1
            """, nativeQuery = true)
    TotalsRow getSalesSummary(Instant after);

    /**
     * Sales per product since {@code after}. The sort of {@code pageable} names one of the columns sales, revenue or
     * profit; the rows are wrapped in a derived table so it applies to them.
     */
    @Query(value = """
            SELECT s.product_id as "productId", s.sales as sales, s.revenue as revenue, s.profit as profit FROM (
              SELECT
                p.product_id as product_id,
                coalesce(sum(p.amount), 0) as sales,
                coalesce(sum(p.price * p.amount), 0) as revenue,
                coalesce(sum(p.price * p.amount - p.cost), 0) as profit
              FROM purchase p
              WHERE p.purchased > ?1 AND p.product_id IS NOT NULL
              GROUP BY p.product_id
            ) s
            """,
            countQuery = "SELECT count(DISTINCT p.product_id) FROM purchase p WHERE p.purchased > ?1 AND p.product_id IS NOT NULL",
            nativeQuery = true)
    Page<ProductSalesRow> getTopSales(Instant after, Pageable pageable);

    @Query(value = """
            SELECT
                cast(p.purchased AT TIME ZONE ?2 AS date) as "day",
                coalesce(sum(p.amount), 0) as "sales",
                coalesce(sum(p.price * p.amount), 0) as "revenue",
                coalesce(sum(p.price * p.amount - p.cost), 0) as "profit"
            FROM purchase p
            WHERE p.purchased > ?1
            GROUP BY "day"
            ORDER BY "day" ASC
            """, nativeQuery = true)
    List<DayRow> getDailyStats(Instant after, String zone);

    @Query(value = """
            SELECT
                coalesce(c.id, 0) as "categoryId",
                c.name as "categoryName",
                coalesce(sum(p.amount), 0) as "sales",
                coalesce(sum(p.price * p.amount), 0) as "revenue"
            FROM purchase p
            JOIN product pr ON p.product_id = pr.id
            LEFT JOIN category c ON pr.category_id = c.id
            WHERE p.purchased > ?1
            GROUP BY c.id, c.name
            ORDER BY "revenue" DESC
            """, nativeQuery = true)
    List<CategorySalesRow> getSalesByCategory(Instant after);

    @Query(value = """
            SELECT
              pr.id as "productId",
              coalesce(sum(p.amount), 0) as "sales",
              coalesce(sum(p.price * p.amount), 0) as "revenue",
              coalesce(sum(p.price * p.amount - p.cost), 0) as "profit"
            FROM product pr
            LEFT JOIN purchase p ON p.product_id = pr.id AND p.purchased > ?1
            WHERE pr.active = true AND pr.deleted = false
            GROUP BY pr.id
            ORDER BY "sales" ASC, pr.name ASC, pr.id ASC
            """,
            countQuery = "SELECT count(*) FROM product pr WHERE pr.active = true AND pr.deleted = false",
            nativeQuery = true)
    Page<ProductSalesRow> getBottomSales(Instant after, Pageable pageable);

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
