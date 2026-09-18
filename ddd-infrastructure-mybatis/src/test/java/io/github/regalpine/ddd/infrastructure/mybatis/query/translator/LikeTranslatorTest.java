package io.github.regalpine.ddd.infrastructure.mybatis.query.translator;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.Wrappers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SqlQueryTranslator - LIKE + escape (§16, §17)")
class LikeTranslatorTest {

    final QueryField<String> NAME = QueryFields.of("name", String.class);
    final SqlQueryTranslator translator = new SqlQueryTranslator();

    @Test
    @DisplayName("LIKE ANYWHERE wraps value with %")
    void likeAnywhere() {
        var result = translator.translate(
                Wrappers.<Object>query().like(NAME, "abc"));

        assertTrue(result.sqlFragment().contains("name LIKE"));
        assertEquals(1, result.parameters().size());
        assertEquals("%abc%", result.parameters().get(0).value());
    }

    @Test
    @DisplayName("LIKE escapes % in user input")
    void likeEscapesPercent() {
        var result = translator.translate(
                Wrappers.<Object>query().like(NAME, "100%"));

        String paramValue = (String) result.parameters().get(0).value();
        assertTrue(paramValue.contains("\\%"), "Should escape % character");
        assertFalse(paramValue.equals("%100%%"), "Should not have unescaped %");
    }

    @Test
    @DisplayName("LIKE escapes _ in user input")
    void likeEscapesUnderscore() {
        var result = translator.translate(
                Wrappers.<Object>query().like(NAME, "a_b"));

        String paramValue = (String) result.parameters().get(0).value();
        assertTrue(paramValue.contains("\\_"), "Should escape _ character");
    }

    @Test
    @DisplayName("LIKE escapes backslash in user input")
    void likeEscapesBackslash() {
        var result = translator.translate(
                Wrappers.<Object>query().like(NAME, "a\\b"));

        String paramValue = (String) result.parameters().get(0).value();
        assertTrue(paramValue.contains("\\\\"), "Should escape \\ character");
    }
}
