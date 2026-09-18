package io.github.regalpine.ddd.infrastructure.mybatis.dialect;

import io.github.regalpine.ddd.infrastructure.mybatis.pagination.DatabaseMetadata;
import io.github.regalpine.ddd.infrastructure.mybatis.pagination.PaginationDialect;
import io.github.regalpine.ddd.infrastructure.mybatis.pagination.PaginationDialectResolver;

/**
 * Default dialect resolver that matches database product name to a dialect.
 *
 * <p>Phase XII §28: uses case-insensitive product name matching
 * to select the appropriate {@link PaginationDialect}.</p>
 *
 * @author RegalPine
 */
public final class DefaultPaginationDialectResolver
        implements PaginationDialectResolver {

    @Override
    public PaginationDialect resolve(DatabaseMetadata metadata) {
        String name = metadata.productName().toLowerCase();

        if (name.contains("postgresql")) {
            return new PostgreSqlPaginationDialect();
        }
        if (name.contains("mysql")) {
            return new MySqlPaginationDialect();
        }
        if (name.contains("oracle")) {
            return new OraclePaginationDialect();
        }
        if (name.contains("sql server") || name.contains("microsoft")) {
            return new SqlServerPaginationDialect();
        }
        if (name.contains("db2")) {
            return new Db2PaginationDialect();
        }

        throw new IllegalStateException(
                "Unsupported database: " + metadata.productName());
    }
}
