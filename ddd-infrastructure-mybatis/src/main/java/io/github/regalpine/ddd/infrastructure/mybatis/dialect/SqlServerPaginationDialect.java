package io.github.regalpine.ddd.infrastructure.mybatis.dialect;

import io.github.regalpine.ddd.infrastructure.mybatis.pagination.CursorPaginationDialect;
import io.github.regalpine.ddd.infrastructure.mybatis.pagination.OffsetPaginationDialect;

/**
 * SQL Server pagination dialect.
 *
 * <p>Phase XII §26: uses {@code OFFSET ? ROWS FETCH NEXT ? ROWS ONLY} syntax.
 * Requires ORDER BY clause for OFFSET/FETCH to work.</p>
 *
 * @author RegalPine
 */
public final class SqlServerPaginationDialect
        implements OffsetPaginationDialect, CursorPaginationDialect {

    @Override
    public String name() {
        return "sqlserver";
    }

    @Override
    public String applyOffsetLimit(String sql, long offset, long limit) {
        return sql + " OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
    }

    @Override
    public boolean supportsOffset() {
        return true;
    }

    @Override
    public boolean supportsLimit() {
        return false;
    }

    @Override
    public boolean supportsKeyset() {
        return true;
    }
}
