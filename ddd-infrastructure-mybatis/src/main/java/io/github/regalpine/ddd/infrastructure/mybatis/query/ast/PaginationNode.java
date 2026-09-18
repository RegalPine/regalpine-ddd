package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.Pagination;

/**
 * AST node for pagination directives.
 *
 * <p>Phase XIII §36: wraps a {@link Pagination} instance (either
 * {@code OffsetPagination} or {@code CursorPagination}) as an AST node.
 * The actual SQL generation is delegated to the {@code PaginationDialect}.</p>
 *
 * @param pagination the pagination strategy
 * @author RegalPine
 */
public record PaginationNode(
        Pagination pagination
) implements QueryNode {
}
