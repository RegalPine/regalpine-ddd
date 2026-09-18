package io.github.regalpine.ddd.infrastructure.mybatis.dialect;

import io.github.regalpine.ddd.infrastructure.mybatis.pagination.PaginationDialect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all 5 pagination dialect implementations (Phase XII §23-§26).
 */
@DisplayName("Pagination Dialects")
class PaginationDialectTest {

    @Test
    @DisplayName("PostgreSQL: LIMIT ? OFFSET ? syntax")
    void postgresql() {
        var d = new PostgreSqlPaginationDialect();
        assertEquals("postgresql", d.name());
        assertTrue(d.supportsOffset());
        assertTrue(d.supportsLimit());
        assertTrue(d.supportsKeyset());

        String sql = d.applyOffsetLimit("SELECT * FROM orders", 20, 10);
        assertTrue(sql.contains("LIMIT 10"));
        assertTrue(sql.contains("OFFSET 20"));
    }

    @Test
    @DisplayName("MySQL: LIMIT ? OFFSET ? syntax")
    void mysql() {
        var d = new MySqlPaginationDialect();
        assertEquals("mysql", d.name());
        assertTrue(d.supportsOffset());
        assertTrue(d.supportsLimit());
        assertTrue(d.supportsKeyset());

        String sql = d.applyOffsetLimit("SELECT * FROM orders", 0, 25);
        assertTrue(sql.contains("LIMIT 25"));
        assertTrue(sql.contains("OFFSET 0"));
    }

    @Test
    @DisplayName("Oracle: OFFSET ? ROWS FETCH NEXT ? ROWS ONLY syntax")
    void oracle() {
        var d = new OraclePaginationDialect();
        assertEquals("oracle", d.name());
        assertTrue(d.supportsOffset());
        assertFalse(d.supportsLimit());
        assertTrue(d.supportsKeyset());

        String sql = d.applyOffsetLimit("SELECT * FROM orders", 10, 5);
        assertTrue(sql.contains("OFFSET 10 ROWS"));
        assertTrue(sql.contains("FETCH NEXT 5 ROWS ONLY"));
    }

    @Test
    @DisplayName("SQL Server: OFFSET ? ROWS FETCH NEXT ? ROWS ONLY syntax")
    void sqlServer() {
        var d = new SqlServerPaginationDialect();
        assertEquals("sqlserver", d.name());
        assertTrue(d.supportsOffset());
        assertFalse(d.supportsLimit());
        assertTrue(d.supportsKeyset());

        String sql = d.applyOffsetLimit("SELECT * FROM orders ORDER BY id", 30, 10);
        assertTrue(sql.contains("OFFSET 30 ROWS"));
        assertTrue(sql.contains("FETCH NEXT 10 ROWS ONLY"));
    }

    @Test
    @DisplayName("DB2: OFFSET ? ROWS FETCH NEXT ? ROWS ONLY syntax")
    void db2() {
        var d = new Db2PaginationDialect();
        assertEquals("db2", d.name());
        assertTrue(d.supportsOffset());
        assertFalse(d.supportsLimit());
        assertTrue(d.supportsKeyset());

        String sql = d.applyOffsetLimit("SELECT * FROM orders", 0, 50);
        assertTrue(sql.contains("OFFSET 0 ROWS"));
        assertTrue(sql.contains("FETCH NEXT 50 ROWS ONLY"));
    }

    @Test
    @DisplayName("All dialects preserve original SQL")
    void allDialectsPreserveOriginalSql() {
        PaginationDialect[] dialects = {
                new PostgreSqlPaginationDialect(),
                new MySqlPaginationDialect(),
                new OraclePaginationDialect(),
                new SqlServerPaginationDialect(),
                new Db2PaginationDialect()
        };

        String baseSql = "SELECT id, name FROM orders WHERE status = 'ACTIVE'";
        for (var d : dialects) {
            String result = d.applyOffsetLimit(baseSql, 0, 10);
            assertTrue(result.startsWith(baseSql),
                    d.name() + " should preserve original SQL prefix");
        }
    }
}
