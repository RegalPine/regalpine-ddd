package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

import java.util.List;

/**
 * AST node for nested (parenthesized) condition groups.
 *
 * <p>Phase XIII §19: represents a sub-query wrapper that produces
 * parenthesized SQL. For example:</p>
 * <pre>
 * wrapper.eq(A, 1).and(w -> w.eq(B, 2).or().eq(C, 3))
 * </pre>
 * <p>Produces:</p>
 * <pre>
 * A = ? AND (B = ? OR C = ?)
 * </pre>
 *
 * @param operator   the logical operator connecting the nested conditions
 * @param conditions the nested condition nodes
 * @author RegalPine
 */
public record NestedNode(
        LogicalNode.LogicalOperator operator,
        List<ConditionNode> conditions
) implements QueryNode {

    public NestedNode {
        conditions = List.copyOf(conditions);
    }
}
