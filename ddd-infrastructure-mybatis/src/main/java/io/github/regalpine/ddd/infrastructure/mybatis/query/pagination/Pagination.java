package io.github.regalpine.ddd.infrastructure.mybatis.query.pagination;

/**
 * Sealed interface representing a pagination strategy for query wrappers.
 *
 * <p>Phase XIII §33: pagination is either offset-based or cursor-based.
 * This is the query-wrapper-level pagination abstraction (user API),
 * distinct from Phase XII's {@code PaginationDialect} (SQL generation layer).</p>
 *
 * @author RegalPine
 */
public sealed interface Pagination
        permits OffsetPagination, CursorPagination {
}
