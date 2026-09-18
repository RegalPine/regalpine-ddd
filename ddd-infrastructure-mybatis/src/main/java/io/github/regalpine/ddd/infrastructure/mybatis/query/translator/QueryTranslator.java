package io.github.regalpine.ddd.infrastructure.mybatis.query.translator;

import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.QueryWrapper;

/**
 * Translates a {@link QueryWrapper} into a SQL fragment with parameter bindings.
 *
 * <p>Phase XIII §29: the translator walks the query AST and produces
 * a {@link TranslationResult} containing the SQL fragment and all
 * {@code ParameterBinding} instances.</p>
 *
 * @author RegalPine
 */
public interface QueryTranslator {

    /**
     * Translates the given query wrapper into a SQL fragment.
     *
     * @param wrapper the query wrapper to translate
     * @return the translation result containing SQL and parameters
     */
    TranslationResult translate(QueryWrapper<?> wrapper);
}
