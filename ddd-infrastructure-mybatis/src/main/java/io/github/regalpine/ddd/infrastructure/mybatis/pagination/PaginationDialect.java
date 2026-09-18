package io.github.regalpine.ddd.infrastructure.mybatis.pagination;

/**
 * Database-specific pagination dialect.
 *
 * <p>Phase XII §22: each supported database provides an implementation
 * that knows how to append OFFSET/LIMIT clauses to SQL statements.</p>
 *
 * @author RegalPine
 */
public interface PaginationDialect {

    /**
     * Returns the dialect name (e.g. "postgresql", "mysql").
     */
    String name();

    /**
     * Applies offset and limit pagination to the given SQL.
     *
     * @param sql    the base SQL query
     * @param offset the row offset
     * @param limit  the maximum number of rows
     * @return the paginated SQL
     */
    String applyOffsetLimit(String sql, long offset, long limit);

    /**
     * Whether this dialect supports OFFSET-based pagination.
     */
    boolean supportsOffset();

    /**
     * Whether this dialect supports LIMIT-based pagination.
     */
    boolean supportsLimit();

    /**
     * Whether this dialect supports keyset/cursor pagination.
     */
    boolean supportsKeyset();
}
