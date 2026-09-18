package io.github.regalpine.ddd.infrastructure.mybatis.query.security;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.DefaultQueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import io.github.regalpine.ddd.infrastructure.mybatis.query.translator.SqlQueryTranslator;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.Wrappers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Security - sort field injection (§71, WRAPPER-005)")
class SortInjectionTest {

    final SqlQueryTranslator translator = new SqlQueryTranslator();

    @Test
    @DisplayName("QueryField with SQL injection in column name is rejected at construction")
    void sqlInjectionInColumnRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                QueryFields.of("created_at; DROP TABLE orders", String.class));
    }

    @Test
    @DisplayName("QueryField with blank column is rejected")
    void blankColumnRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                QueryFields.of("", String.class));
    }

    @Test
    @DisplayName("QueryField with null column is rejected")
    void nullColumnRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                QueryFields.of(null, String.class));
    }

    @Test
    @DisplayName("QueryField with null type is rejected")
    void nullTypeRejected() {
        assertThrows(NullPointerException.class, () ->
                new DefaultQueryField<>("column", null));
    }

    @Test
    @DisplayName("valid QueryField passes through translator safely")
    void validFieldSafe() {
        var field = QueryFields.of("created_at", String.class);
        var result = translator.translate(
                Wrappers.<Object>query().orderByDesc(field));

        assertTrue(result.sqlFragment().contains("created_at DESC"));
    }
}
