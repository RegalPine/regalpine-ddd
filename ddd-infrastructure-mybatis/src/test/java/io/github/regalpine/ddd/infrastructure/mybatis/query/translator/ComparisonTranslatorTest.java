package io.github.regalpine.ddd.infrastructure.mybatis.query.translator;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.Wrappers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SqlQueryTranslator - comparison operations (§32)")
class ComparisonTranslatorTest {

    final QueryField<String> STATUS = QueryFields.of("status", String.class);
    final QueryField<Integer> AGE = QueryFields.of("age", Integer.class);
    final SqlQueryTranslator translator = new SqlQueryTranslator();

    @Test
    @DisplayName("eq generates column = #{pN}")
    void eqTranslation() {
        var wrapper = Wrappers.<Object>query().eq(STATUS, "PAID");
        var result = translator.translate(wrapper);

        assertTrue(result.sqlFragment().contains("status ="));
        assertTrue(result.sqlFragment().contains("#{p1}"));
        assertEquals(1, result.parameters().size());
        assertEquals("PAID", result.parameters().get(0).value());
    }

    @Test
    @DisplayName("ne generates column != #{pN}")
    void neTranslation() {
        var result = translator.translate(Wrappers.<Object>query().ne(STATUS, "CANCELLED"));
        assertTrue(result.sqlFragment().contains("status !="));
    }

    @Test
    @DisplayName("gt generates column > #{pN}")
    void gtTranslation() {
        var result = translator.translate(Wrappers.<Object>query().gt(AGE, 18));
        assertTrue(result.sqlFragment().contains("age >"));
    }

    @Test
    @DisplayName("ge generates column >= #{pN}")
    void geTranslation() {
        var result = translator.translate(Wrappers.<Object>query().ge(AGE, 18));
        assertTrue(result.sqlFragment().contains("age >="));
    }

    @Test
    @DisplayName("lt generates column < #{pN}")
    void ltTranslation() {
        var result = translator.translate(Wrappers.<Object>query().lt(AGE, 65));
        assertTrue(result.sqlFragment().contains("age <"));
    }

    @Test
    @DisplayName("le generates column <= #{pN}")
    void leTranslation() {
        var result = translator.translate(Wrappers.<Object>query().le(AGE, 65));
        assertTrue(result.sqlFragment().contains("age <="));
    }

    @Test
    @DisplayName("multiple conditions joined by AND")
    void multipleConditions() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .eq(STATUS, "PAID")
                        .gt(AGE, 18));

        assertTrue(result.sqlFragment().contains("AND"));
        assertEquals(2, result.parameters().size());
    }

    @Test
    @DisplayName("isNull generates IS NULL")
    void isNullTranslation() {
        var result = translator.translate(Wrappers.<Object>query().isNull(STATUS));
        assertTrue(result.sqlFragment().contains("status IS NULL"));
        assertEquals(0, result.parameters().size());
    }

    @Test
    @DisplayName("isNotNull generates IS NOT NULL")
    void isNotNullTranslation() {
        var result = translator.translate(Wrappers.<Object>query().isNotNull(STATUS));
        assertTrue(result.sqlFragment().contains("status IS NOT NULL"));
    }
}
