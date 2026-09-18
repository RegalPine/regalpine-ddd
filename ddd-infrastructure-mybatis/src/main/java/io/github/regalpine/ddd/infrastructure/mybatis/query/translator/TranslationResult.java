package io.github.regalpine.ddd.infrastructure.mybatis.query.translator;

import io.github.regalpine.ddd.infrastructure.mybatis.query.parameter.ParameterBinding;

import java.util.List;

/**
 * The result of translating a {@code QueryWrapper} into SQL.
 *
 * <p>Phase XIII §29: contains the generated SQL fragment and the list of
 * parameter bindings. All values are bound as prepared parameters (§31),
 * never concatenated into the SQL string.</p>
 *
 * @param sqlFragment the generated SQL fragment (WHERE, ORDER BY, etc.)
 * @param parameters  the parameter bindings
 * @author RegalPine
 */
public record TranslationResult(
        String sqlFragment,
        List<ParameterBinding> parameters
) {

    public TranslationResult {
        parameters = List.copyOf(parameters);
    }
}
