package io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper;

import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.ComparisonNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.NestedNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.LogicalNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QueryWrapper - nested AND/OR (§19, §70)")
class NestedQueryTest {

    static final QueryField<Integer> A = QueryFields.of("a", Integer.class);
    static final QueryField<Integer> B = QueryFields.of("b", Integer.class);
    static final QueryField<Integer> C = QueryFields.of("c", Integer.class);

    @Test
    @DisplayName("and(Consumer) creates NestedNode with AND operator")
    void andNested() {
        var wrapper = Wrappers.<Object>query()
                .eq(A, 1)
                .and(w -> {
                    w.eq(B, 2);
                    w.eq(C, 3);
                });

        assertEquals(2, wrapper.nodes().size());
        assertInstanceOf(ComparisonNode.class, wrapper.nodes().get(0));
        assertInstanceOf(NestedNode.class, wrapper.nodes().get(1));

        var nested = (NestedNode) wrapper.nodes().get(1);
        assertEquals(LogicalNode.LogicalOperator.AND, nested.operator());
        assertEquals(2, nested.conditions().size());
    }

    @Test
    @DisplayName("or(Consumer) creates NestedNode with OR operator")
    void orNested() {
        var wrapper = Wrappers.<Object>query()
                .eq(A, 1)
                .or(w -> w.eq(B, 2));

        var nested = (NestedNode) wrapper.nodes().get(1);
        assertEquals(LogicalNode.LogicalOperator.OR, nested.operator());
    }
}
