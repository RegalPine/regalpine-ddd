package io.github.regalpine.ddd.infrastructure.mybatis.dialect;

import io.github.regalpine.ddd.infrastructure.mybatis.pagination.CursorPaginationDialect;
import io.github.regalpine.ddd.infrastructure.mybatis.pagination.OffsetPaginationDialect;

/**
 * DB2 pagination dialect.
 *
 * <p>Phase XII §5: uses {@code OFFSET ? ROWS FETCH NEXT ? ROWS ONLY} syntax,
 * similar to Oracle and SQL Server.</p>
 *
 * @author RegalPine
 */
public final class Db2PaginationDialect
        implements OffsetPaginationDialect, CursorPaginationDialect {

    @Override
    public String name() {
        return "db2";
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
