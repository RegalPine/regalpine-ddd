package io.github.regalpine.ddd.infrastructure.mybatis.query.translator;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.Wrappers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SqlQueryTranslator - logical AND/OR/nested (§18, §19, §70)")
class LogicalTranslatorTest {

    final QueryField<Integer> A = QueryFields.of("a", Integer.class);
    final QueryField<Integer> B = QueryFields.of("b", Integer.class);
    final QueryField<Integer> C = QueryFields.of("c", Integer.class);
    final SqlQueryTranslator translator = new SqlQueryTranslator();

    @Test
    @DisplayName("nested AND produces parenthesized SQL")
    void nestedAnd() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .eq(A, 1)
                        .and(w -> w.eq(B, 2).eq(C, 3)));

        String sql = result.sqlFragment();
        assertTrue(sql.contains("a ="));
        assertTrue(sql.contains("AND ("));
        assertTrue(sql.contains("b ="));
        assertTrue(sql.contains("AND"));
        assertTrue(sql.contains("c ="));
        assertTrue(sql.contains(")"));
    }

    @Test
    @DisplayName("nested OR produces parenthesized SQL")
    void nestedOr() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .eq(A, 1)
                        .or(w -> w.eq(B, 2)));

        String sql = result.sqlFragment();
        assertTrue(sql.contains("(b ="));
        assertTrue(sql.contains(")"));
    }
}
