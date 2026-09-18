package io.github.regalpine.ddd.infrastructure.mybatis.dialect;

import io.github.regalpine.ddd.infrastructure.mybatis.pagination.CursorPaginationDialect;
import io.github.regalpine.ddd.infrastructure.mybatis.pagination.OffsetPaginationDialect;

/**
 * Oracle pagination dialect.
 *
 * <p>Phase XII §25: uses {@code OFFSET ? ROWS FETCH NEXT ? ROWS ONLY} syntax.
 * Supports offset and keyset, but not standalone LIMIT.</p>
 *
 * @author RegalPine
 */
public final class OraclePaginationDialect
        implements OffsetPaginationDialect, CursorPaginationDialect {

    @Override
    public String name() {
        return "oracle";
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
