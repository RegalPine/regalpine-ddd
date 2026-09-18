package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;

/**
 * AST node for BETWEEN / NOT BETWEEN operations.
 *
 * <p>Phase XIII §15: generates {@code field BETWEEN ? AND ?} or
 * {@code field NOT BETWEEN ? AND ?}.</p>
 *
 * @param <T>      the field type
 * @param field    the query field
 * @param lower    the lower bound (inclusive)
 * @param upper    the upper bound (inclusive)
 * @param negated  false for BETWEEN, true for NOT BETWEEN
 * @author RegalPine
 */
public record BetweenNode<T>(
        QueryField<T> field,
        T lower,
        T upper,
        boolean negated
) implements ConditionNode {
}
