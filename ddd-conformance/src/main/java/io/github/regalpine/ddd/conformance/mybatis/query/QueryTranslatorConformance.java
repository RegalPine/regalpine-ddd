package io.github.regalpine.ddd.conformance.mybatis.query;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import io.github.regalpine.ddd.infrastructure.mybatis.query.translator.SqlQueryTranslator;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.Wrappers;

import java.util.List;

/**
 * Phase XIII §76: conformance for SQL translation and parameter binding.
 *
 * <p>Verifies that the translator produces parameterized SQL (no value concatenation),
 * handles all AST node types, and generates correct parameter bindings.</p>
 *
 * @author RegalPine
 */
public final class QueryTranslatorConformance {

    private QueryTranslatorConformance() {
    }

    private static final SqlQueryTranslator TRANSLATOR = new SqlQueryTranslator();

    /**
     * WRAPPER-003: Wrapper must not generate SQL with user values directly.
     */
    public static boolean noSqlConcatenation() {
        var field = QueryFields.of("status", String.class);
        var result = TRANSLATOR.translate(
                Wrappers.<Object>query().eq(field, "PAID"));
        return !result.sqlFragment().contains("PAID")
                && result.sqlFragment().contains("#{p1}");
    }

    /**
     * WRAPPER-004: All query values must use parameter binding.
     */
    public static boolean allValuesParameterized() {
        var status = QueryFields.of("status", String.class);
        var age = QueryFields.of("age", Integer.class);
        var result = TRANSLATOR.translate(
                Wrappers.<Object>query()
                        .eq(status, "PAID")
                        .gt(age, 18));
        return result.parameters().size() == 2
                && result.sqlFragment().contains("#{p1}")
                && result.sqlFragment().contains("#{p2}");
    }

    /**
     * §14: Empty IN produces FALSE, not illegal SQL.
     */
    public static boolean emptyInProducesFalse() {
        var field = QueryFields.of("status", String.class);
        var result = TRANSLATOR.translate(
                Wrappers.<Object>query().in(field, List.of()));
        return result.sqlFragment().contains("1 = 0")
                && !result.sqlFragment().contains("IN ()");
    }

    /**
     * §17: LIKE values are properly escaped.
     */
    public static boolean likeEscaped() {
        var field = QueryFields.of("name", String.class);
        var result = TRANSLATOR.translate(
                Wrappers.<Object>query().like(field, "100%"));
        String param = (String) result.parameters().get(0).value();
        return param.contains("\\%");
    }

    /**
     * §19: Nested conditions produce parenthesized SQL.
     */
    public static boolean nestedParenthesized() {
        var a = QueryFields.of("a", Integer.class);
        var b = QueryFields.of("b", Integer.class);
        var result = TRANSLATOR.translate(
                Wrappers.<Object>query()
                        .eq(a, 1)
                        .and(w -> w.eq(b, 2)));
        return result.sqlFragment().contains("(")
                && result.sqlFragment().contains(")");
    }
}
