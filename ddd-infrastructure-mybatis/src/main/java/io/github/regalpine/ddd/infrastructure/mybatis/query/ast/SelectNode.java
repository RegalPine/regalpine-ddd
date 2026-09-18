package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;

import java.util.List;

/**
 * AST node for explicit SELECT column lists.
 *
 * <p>Phase XIII §28: when present, generates {@code SELECT col1, col2, ...}
 * instead of the default {@code SELECT *}.</p>
 *
 * @param fields the fields to select (immutable)
 * @author RegalPine
 */
public record SelectNode(
        List<QueryField<?>> fields
) implements QueryNode {

    public SelectNode {
        fields = List.copyOf(fields);
    }
}
