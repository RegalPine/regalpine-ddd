package io.github.regalpine.ddd.infrastructure.mybatis.query.translator;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.Wrappers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SqlQueryTranslator - parameter binding (§30, §31)")
class ParameterBindingTest {

    final QueryField<String> STATUS = QueryFields.of("status", String.class);
    final QueryField<Integer> AGE = QueryFields.of("age", Integer.class);
    final SqlQueryTranslator translator = new SqlQueryTranslator();

    @Test
    @DisplayName("all values go through parameter binding, never SQL concatenation")
    void valuesAreParameterized() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .eq(STATUS, "PAID")
                        .gt(AGE, 18));

        String sql = result.sqlFragment();
        assertFalse(sql.contains("'PAID'"), "Values must not be in SQL");
        assertFalse(sql.contains("18"), "Numeric values must not be in SQL");
        assertTrue(sql.contains("#{p1}"));
        assertTrue(sql.contains("#{p2}"));
    }

    @Test
    @DisplayName("parameter names are sequential: p1, p2, ...")
    void sequentialParameterNames() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .eq(STATUS, "A")
                        .eq(STATUS, "B")
                        .eq(STATUS, "C"));

        var params = result.parameters();
        assertEquals("p1", params.get(0).name());
        assertEquals("p2", params.get(1).name());
        assertEquals("p3", params.get(2).name());
    }

    @Test
    @DisplayName("IN generates multiple parameter bindings")
    void inParameterBindings() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .in(STATUS, List.of("A", "B", "C")));

        assertEquals(3, result.parameters().size());
        assertTrue(result.sqlFragment().contains("IN (#{p1}, #{p2}, #{p3})"));
    }

    @Test
    @DisplayName("BETWEEN generates two parameter bindings")
    void betweenParameterBindings() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .between(AGE, 18, 65));

        assertEquals(2, result.parameters().size());
        assertTrue(result.sqlFragment().contains("BETWEEN #{p1} AND #{p2}"));
    }
}
