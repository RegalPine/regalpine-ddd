package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;

/**
 * AST node for IS NULL / IS NOT NULL checks.
 *
 * <p>Phase XIII §12:</p>
 * <ul>
 *   <li>{@code negated=false} → IS NULL</li>
 *   <li>{@code negated=true} → IS NOT NULL</li>
 * </ul>
 *
 * @param field    the query field
 * @param negated  false for IS NULL, true for IS NOT NULL
 * @author RegalPine
 */
public record NullNode(
        QueryField<?> field,
        boolean negated
) implements ConditionNode {
}
