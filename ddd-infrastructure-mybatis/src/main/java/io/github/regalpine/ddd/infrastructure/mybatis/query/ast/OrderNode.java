package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;

/**
 * AST node for ORDER BY clauses.
 *
 * <p>Phase XIII §26: represents a single ORDER BY column with direction.
 * Sort fields are type-safe {@link QueryField} instances, providing
 * built-in whitelist protection (§27).</p>
 *
 * @param field     the query field to order by
 * @param direction the sort direction
 * @author RegalPine
 */
public record OrderNode(
        QueryField<?> field,
        Direction direction
) implements QueryNode {

    /**
     * Sort direction.
     */
    public enum Direction {
        ASC, DESC
    }
}
