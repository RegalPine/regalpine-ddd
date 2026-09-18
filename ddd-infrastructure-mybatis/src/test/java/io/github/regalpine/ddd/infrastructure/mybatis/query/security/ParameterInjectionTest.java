package io.github.regalpine.ddd.infrastructure.mybatis.query.security;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import io.github.regalpine.ddd.infrastructure.mybatis.query.translator.SqlQueryTranslator;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.Wrappers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Security - parameter injection (§71, WRAPPER-003, WRAPPER-004)")
class ParameterInjectionTest {

    final QueryField<String> STATUS = QueryFields.of("status", String.class);
    final SqlQueryTranslator translator = new SqlQueryTranslator();

    @Test
    @DisplayName("SQL injection in value is parameterized, not concatenated")
    void sqlInjectionInParameterized() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .eq(STATUS, "PAID' OR '1'='1"));

        String sql = result.sqlFragment();
        assertFalse(sql.contains("PAID"), "Value must not appear in SQL");
        assertFalse(sql.contains("OR"), "Injection must not appear in SQL");
        assertTrue(sql.contains("#{p1}"));
        assertEquals("PAID' OR '1'='1", result.parameters().get(0).value());
    }

    @Test
    @DisplayName("values with semicolons are parameterized")
    void semicolonParameterized() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .eq(STATUS, "PAID; DROP TABLE orders"));

        assertFalse(result.sqlFragment().contains("DROP"));
        assertEquals("PAID; DROP TABLE orders", result.parameters().get(0).value());
    }

    @Test
    @DisplayName("values with quotes are parameterized")
    void quotesParameterized() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .eq(STATUS, "it's a test"));

        assertFalse(result.sqlFragment().contains("it's"));
        assertTrue(result.sqlFragment().contains("#{p1}"));
    }
}
