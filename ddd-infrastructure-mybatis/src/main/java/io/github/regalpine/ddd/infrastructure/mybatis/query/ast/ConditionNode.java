package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

/**
 * Sealed interface for condition (predicate) AST nodes.
 *
 * <p>Phase XIII §10: condition nodes represent individual predicates
 * such as comparisons, null checks, IN, BETWEEN, and LIKE.</p>
 *
 * @author RegalPine
 */
public sealed interface ConditionNode extends QueryNode
        permits ComparisonNode, NullNode, InNode, BetweenNode, LikeNode {
}
