package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;

/**
 * AST node for comparison operations (=, !=, >, >=, <, <=).
 *
 * <p>Phase XIII §11: represents a binary comparison between a field and a value.</p>
 *
 * @param <T>      the field type
 * @param field    the query field
 * @param operator the comparison operator
 * @param value    the comparison value
 * @author RegalPine
 */
public record ComparisonNode<T>(
        QueryField<T> field,
        Operator operator,
        Object value
) implements ConditionNode {

    /**
     * Comparison operators.
     */
    public enum Operator {
        EQ, NE, GT, GE, LT, LE
    }
}
