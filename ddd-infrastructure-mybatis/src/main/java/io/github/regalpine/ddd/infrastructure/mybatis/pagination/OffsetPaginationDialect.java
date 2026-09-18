package io.github.regalpine.ddd.infrastructure.mybatis.pagination;

/**
 * Marker interface for dialects that support offset-based pagination.
 *
 * <p>Phase XII §5: dialects implementing this interface can use
 * {@code OFFSET ? ROWS} or {@code OFFSET ?} syntax.</p>
 *
 * @author RegalPine
 */
public interface OffsetPaginationDialect extends PaginationDialect {
}
