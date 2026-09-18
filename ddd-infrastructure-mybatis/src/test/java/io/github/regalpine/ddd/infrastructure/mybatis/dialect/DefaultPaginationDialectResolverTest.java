package io.github.regalpine.ddd.infrastructure.mybatis.dialect;

import io.github.regalpine.ddd.infrastructure.mybatis.pagination.DatabaseMetadata;
import io.github.regalpine.ddd.infrastructure.mybatis.pagination.PaginationDialect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DefaultPaginationDialectResolver} (Phase XII §28).
 */
@DisplayName("DefaultPaginationDialectResolver")
class DefaultPaginationDialectResolverTest {

    private final DefaultPaginationDialectResolver resolver = new DefaultPaginationDialectResolver();

    @Test
    @DisplayName("resolves PostgreSQL")
    void resolvesPostgresql() {
        var meta = new DatabaseMetadata("PostgreSQL", "15.2", 15, 2);
        PaginationDialect dialect = resolver.resolve(meta);
        assertInstanceOf(PostgreSqlPaginationDialect.class, dialect);
    }

    @Test
    @DisplayName("resolves MySQL")
    void resolvesMysql() {
        var meta = new DatabaseMetadata("MySQL", "8.0.33", 8, 0);
        PaginationDialect dialect = resolver.resolve(meta);
        assertInstanceOf(MySqlPaginationDialect.class, dialect);
    }

    @Test
    @DisplayName("resolves Oracle")
    void resolvesOracle() {
        var meta = new DatabaseMetadata("Oracle Database", "19c", 19, 0);
        PaginationDialect dialect = resolver.resolve(meta);
        assertInstanceOf(OraclePaginationDialect.class, dialect);
    }

    @Test
    @DisplayName("resolves SQL Server via 'Microsoft SQL Server'")
    void resolvesSqlServer() {
        var meta = new DatabaseMetadata("Microsoft SQL Server", "16.0", 16, 0);
        PaginationDialect dialect = resolver.resolve(meta);
        assertInstanceOf(SqlServerPaginationDialect.class, dialect);
    }

    @Test
    @DisplayName("resolves DB2")
    void resolvesDb2() {
        var meta = new DatabaseMetadata("DB2", "11.5", 11, 5);
        PaginationDialect dialect = resolver.resolve(meta);
        assertInstanceOf(Db2PaginationDialect.class, dialect);
    }

    @Test
    @DisplayName("throws for unsupported database")
    void throwsForUnsupported() {
        var meta = new DatabaseMetadata("UnknownDB", "1.0", 1, 0);
        assertThrows(IllegalStateException.class, () -> resolver.resolve(meta));
    }
}
