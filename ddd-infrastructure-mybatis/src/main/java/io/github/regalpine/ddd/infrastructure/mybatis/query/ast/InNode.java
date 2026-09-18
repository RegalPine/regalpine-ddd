package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;

import java.util.List;

/**
 * AST node for IN / NOT IN operations.
 *
 * <p>Phase XIII §13: values are defensively copied via {@code List.copyOf}.
 * Empty IN (§14) must NOT generate illegal SQL {@code IN ()}.</p>
 *
 * @param <T>      the field type
 * @param field    the query field
 * @param values   the IN values (immutable)
 * @param negated  false for IN, true for NOT IN
 * @author RegalPine
 */
public record InNode<T>(
        QueryField<T> field,
        List<T> values,
        boolean negated
) implements ConditionNode {

    public InNode {
        values = List.copyOf(values);
    }
}
