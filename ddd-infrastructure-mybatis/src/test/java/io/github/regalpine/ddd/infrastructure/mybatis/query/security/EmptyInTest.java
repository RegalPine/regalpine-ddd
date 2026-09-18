package io.github.regalpine.ddd.infrastructure.mybatis.query.security;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import io.github.regalpine.ddd.infrastructure.mybatis.query.translator.SqlQueryTranslator;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.AbstractQueryWrapper;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.Wrappers;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.ComparisonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Security - empty IN handling (§14, WRAPPER-009)")
class EmptyInTest {

    final QueryField<String> STATUS = QueryFields.of("status", String.class);
    final SqlQueryTranslator translator = new SqlQueryTranslator();

    @Test
    @DisplayName("empty IN produces FALSE sentinel (1 = 0)")
    void emptyInProducesFalse() {
        var wrapper = Wrappers.<Object>query()
                .in(STATUS, List.of());

        assertEquals(1, wrapper.nodes().size());
        var node = (ComparisonNode<?>) wrapper.nodes().get(0);
        assertSame(AbstractQueryWrapper.EMPTY_IN_FALSE, node.value());

        var result = translator.translate(wrapper);
        assertTrue(result.sqlFragment().contains("1 = 0"),
                "Empty IN should produce FALSE: " + result.sqlFragment());
    }

    @Test
    @DisplayName("empty NOT IN is a no-op (TRUE semantics)")
    void emptyNotInIsNoOp() {
        var wrapper = Wrappers.<Object>query()
                .notIn(STATUS, List.of());

        assertEquals(0, wrapper.nodes().size(),
                "Empty NOT IN should produce no nodes (TRUE)");
    }

    @Test
    @DisplayName("empty IN does NOT generate illegal SQL 'IN ()'")
    void noIllegalInSql() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .in(STATUS, List.of()));

        assertFalse(result.sqlFragment().contains("IN ()"),
                "Must not generate illegal SQL 'IN ()'");
    }

    @Test
    @DisplayName("non-empty IN works normally")
    void nonEmptyInWorks() {
        var result = translator.translate(
                Wrappers.<Object>query()
                        .in(STATUS, List.of("A", "B")));

        assertTrue(result.sqlFragment().contains("IN ("));
        assertEquals(2, result.parameters().size());
    }
}
