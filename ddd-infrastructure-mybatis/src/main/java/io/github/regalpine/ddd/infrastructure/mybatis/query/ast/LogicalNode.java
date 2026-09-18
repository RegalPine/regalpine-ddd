package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

import java.util.List;

/**
 * AST node for logical AND/OR combinations of conditions.
 *
 * <p>Phase XIII §18: represents a flat logical combination of condition nodes.
 * The top-level conditions in a wrapper are implicitly ANDed.</p>
 *
 * @param operator the logical operator (AND or OR)
 * @param children the child condition nodes
 * @author RegalPine
 */
public record LogicalNode(
        LogicalOperator operator,
        List<ConditionNode> children
) implements QueryNode {

    /**
     * Logical operators.
     */
    public enum LogicalOperator {
        AND, OR
    }

    public LogicalNode {
        children = List.copyOf(children);
    }
}
