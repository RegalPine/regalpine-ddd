package io.github.regalpine.ddd.infrastructure.mybatis.dialect;

import io.github.regalpine.ddd.infrastructure.mybatis.pagination.CursorPaginationDialect;
import io.github.regalpine.ddd.infrastructure.mybatis.pagination.OffsetPaginationDialect;

/**
 * PostgreSQL pagination dialect.
 *
 * <p>Phase XII §23: uses {@code LIMIT ? OFFSET ?} syntax.
 * Supports offset, limit, and keyset pagination.</p>
 *
 * @author RegalPine
 */
public final class PostgreSqlPaginationDialect
        implements OffsetPaginationDialect, CursorPaginationDialect {

    @Override
    public String name() {
        return "postgresql";
    }

    @Override
    public String applyOffsetLimit(String sql, long offset, long limit) {
        return sql + " LIMIT " + limit + " OFFSET " + offset;
    }

    @Override
    public boolean supportsOffset() {
        return true;
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsKeyset() {
        return true;
    }
}
