package io.github.regalpine.ddd.infrastructure.mybatis.pagination;

/**
 * Marker interface for dialects that support cursor/keyset pagination.
 *
 * <p>Phase XII §5/§37: dialects implementing this interface can use
 * cursor-based pagination with stable sort predicates.</p>
 *
 * @author RegalPine
 */
public interface CursorPaginationDialect extends PaginationDialect {
}
