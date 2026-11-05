package dev.chojo.simplepos.configuration;

import dev.chojo.simplepos.TestcontainersConfiguration;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs the migrations on a database that holds data written by the version before them, the way the production
 * database is upgraded: money stored as floating point, unit prices that do not divide into cents.
 */
class MigrationTest {
    private static final String DATABASE = "migration_check";
    private static JdbcTemplate jdbc;
    private static Flyway flyway;

    @BeforeAll
    static void upgradeOldData() {
        JdbcTemplate admin = new JdbcTemplate(dataSource(TestcontainersConfiguration.databaseName()));
        admin.execute("DROP DATABASE IF EXISTS " + DATABASE);
        admin.execute("CREATE DATABASE " + DATABASE);
        DriverManagerDataSource dataSource = dataSource(DATABASE);
        jdbc = new JdbcTemplate(dataSource);
        flyway = Flyway.configure().dataSource(dataSource).target("2").load();
        flyway.migrate();

        jdbc.update("INSERT INTO category (id, name) VALUES (1, 'Drinks')");
        jdbc.update("""
                INSERT INTO ingredient (id, container_size, min_stock, name, pledge, pledge_container, price, category_id)
                VALUES (1, 20, 0, 'Bottle', 0.08, 1.5, 0.877, 1)""");
        jdbc.update("INSERT INTO storage (id, amount, price, purchased, sold, ingredient_id) VALUES (1, 12, 0.877, now(), 0, 1)");
        jdbc.update("INSERT INTO users (id, enabled, username, role) VALUES (1, true, 'staff', 'STAFF')");
        jdbc.update("INSERT INTO cash (id, amount, type, user_id) VALUES (1, -10.524000000000001, 'purchase', 1)");
        jdbc.update("INSERT INTO cash (id, amount, type, user_id) VALUES (2, 0.30000000000000004, 'other', 1)");
        jdbc.update("INSERT INTO account (id, name, deleted) VALUES (1, 'Guest', false)");
        jdbc.update("INSERT INTO balance (id, amount, account_id) VALUES (1, 0.8000000000000398, 1)");
        jdbc.update("INSERT INTO product (id, active, name, price, category_id) VALUES (1, true, 'Lemonade', 2.0, 1)");
        jdbc.update("INSERT INTO purchase (id, amount, price, purchased, account_id, product_id) VALUES (1, 5, 2.0, now(), 1, 1)");
        jdbc.update("INSERT INTO purchase (id, amount, price, purchased, account_id, product_id) VALUES (2, 7, 2.0, now(), 1, 1)");
        for (int id = 1; id <= 5; id++) {
            jdbc.update("INSERT INTO purchase_part (id, purchase_id, storage_id) VALUES (?, 1, 1)", id);
        }
        for (int id = 6; id <= 12; id++) {
            jdbc.update("INSERT INTO purchase_part (id, purchase_id, storage_id) VALUES (?, 2, 1)", id);
        }
        jdbc.update("INSERT INTO purchase_part (id, purchase_id, storage_id) VALUES (13, NULL, 1)");
        jdbc.update("INSERT INTO storage (id, amount, price, purchased, sold, ingredient_id) VALUES (2, 7, 0.14285714285714285, now(), 6, 1)");
        jdbc.update("INSERT INTO purchase (id, amount, price, purchased, account_id, product_id) VALUES (3, 2, 2.0, now(), 1, 1)");
        jdbc.update("INSERT INTO purchase (id, amount, price, purchased, account_id, product_id) VALUES (4, 1, 2.0, now(), 1, 1)");
        jdbc.update("INSERT INTO purchase_part (id, purchase_id, storage_id) VALUES (14, 3, 2)");
        jdbc.update("INSERT INTO purchase_part (id, purchase_id, storage_id) VALUES (15, 4, 2)");
        jdbc.update("INSERT INTO purchase_part (id, purchase_id, storage_id) VALUES (16, 3, 2)");
        jdbc.update("INSERT INTO purchase_part (id, purchase_id, storage_id) VALUES (17, 2, 2)");
        jdbc.update("INSERT INTO storage (id, amount, price, purchased, sold, ingredient_id) VALUES (3, 3, 0.3333333333333333, now(), 3, 1)");
        for (int id = 5; id <= 7; id++) {
            jdbc.update("INSERT INTO purchase (id, amount, price, purchased, account_id, product_id) VALUES (?, 1, 2.0, now(), 1, 1)", id);
        }
        jdbc.update("INSERT INTO purchase_part (id, purchase_id, storage_id) VALUES (18, 7, 3)");
        jdbc.update("INSERT INTO purchase_part (id, purchase_id, storage_id) VALUES (19, 5, 3)");
        jdbc.update("INSERT INTO purchase_part (id, purchase_id, storage_id) VALUES (20, 6, 3)");

        Flyway.configure().dataSource(dataSource).load().migrate();
    }

    @Test
    void batchesKeepWhatWasPaidInCents() {
        assertEquals(new BigDecimal("10.52"), jdbc.queryForObject("SELECT cost FROM storage WHERE id = 1", BigDecimal.class));
    }

    @Test
    void unitPricesKeepFourDecimals() {
        assertEquals(new BigDecimal("0.8770"), jdbc.queryForObject("SELECT price FROM ingredient WHERE id = 1", BigDecimal.class));
    }

    @Test
    void amountsAreRoundedToCents() {
        assertEquals(new BigDecimal("-10.52"), jdbc.queryForObject("SELECT amount FROM cash WHERE id = 1", BigDecimal.class));
        assertEquals(new BigDecimal("0.30"), jdbc.queryForObject("SELECT amount FROM cash WHERE id = 2", BigDecimal.class));
        assertEquals(new BigDecimal("0.08"), jdbc.queryForObject("SELECT pledge FROM ingredient WHERE id = 1", BigDecimal.class));
        assertEquals(new BigDecimal("0.80"), jdbc.queryForObject("SELECT amount FROM balance WHERE id = 1", BigDecimal.class));
    }

    @Test
    void salePartsAreMergedPerPurchaseAndBatch() {
        assertEquals(8, jdbc.queryForObject("SELECT count(*) FROM purchase_part", Integer.class));
        assertEquals(5, part(1, 1, "amount", Integer.class));
        assertEquals(7, part(2, 1, "amount", Integer.class));
        assertEquals(2, part(3, 2, "amount", Integer.class));
        assertEquals(1, part(4, 2, "amount", Integer.class));
        assertEquals(1, part(2, 2, "amount", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT DISTINCT ingredient_id FROM purchase_part", Integer.class));
    }

    @Test
    void aSoldOutBatchIsChargedExactlyWhatItCost() {
        assertEquals(new BigDecimal("4.38"), part(1, 1, "cost", BigDecimal.class));
        assertEquals(new BigDecimal("6.14"), part(2, 1, "cost", BigDecimal.class));
        assertEquals(new BigDecimal("10.52"), jdbc.queryForObject("SELECT sum(cost) FROM purchase_part WHERE storage_id = 1", BigDecimal.class));
    }

    @Test
    void sharesAreCountedWithinEachBatch() {
        assertEquals(new BigDecimal("0.29"), part(3, 2, "cost", BigDecimal.class));
        assertEquals(new BigDecimal("0.14"), part(4, 2, "cost", BigDecimal.class));
        assertEquals(new BigDecimal("0.14"), part(2, 2, "cost", BigDecimal.class));
    }

    @Test
    void sharesFollowTheSaleOrderWithinEachBatch() {
        assertEquals(new BigDecimal("0.33"), part(7, 3, "cost", BigDecimal.class));
        assertEquals(new BigDecimal("0.34"), part(5, 3, "cost", BigDecimal.class));
        assertEquals(new BigDecimal("0.33"), part(6, 3, "cost", BigDecimal.class));
        assertEquals(new BigDecimal("1.00"), jdbc.queryForObject("SELECT sum(cost) FROM purchase_part WHERE storage_id = 3", BigDecimal.class));
    }

    private static <T> T part(int purchase, int storage, String column, Class<T> type) {
        return jdbc.queryForObject("SELECT " + column + " FROM purchase_part WHERE purchase_id = ? AND storage_id = ?",
                type, purchase, storage);
    }

    private static DriverManagerDataSource dataSource(String database) {
        return new DriverManagerDataSource(TestcontainersConfiguration.jdbcUrl(database),
                TestcontainersConfiguration.username(), TestcontainersConfiguration.password());
    }
}
