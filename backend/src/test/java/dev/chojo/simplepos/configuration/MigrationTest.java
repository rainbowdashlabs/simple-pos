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

    private static DriverManagerDataSource dataSource(String database) {
        return new DriverManagerDataSource(TestcontainersConfiguration.jdbcUrl(database),
                TestcontainersConfiguration.username(), TestcontainersConfiguration.password());
    }
}
