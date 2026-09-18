package io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper;

import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.OrderNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.SelectNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QueryWrapper - ORDER BY and SELECT (§26, §28)")
class OrderByTest {

    static final QueryField<String> STATUS = QueryFields.of("status", String.class);
    static final QueryField<Long> CREATED_AT = QueryFields.of("created_at", Long.class);
    static final QueryField<String> ID = QueryFields.of("id", String.class);

    @Test
    @DisplayName("orderByAsc creates OrderNode with ASC direction")
    void orderByAsc() {
        var wrapper = Wrappers.<Object>query().orderByAsc(CREATED_AT);
        var node = (OrderNode) wrapper.nodes().get(0);
        assertEquals("created_at", node.field().column());
        assertEquals(OrderNode.Direction.ASC, node.direction());
    }

    @Test
    @DisplayName("orderByDesc creates OrderNode with DESC direction")
    void orderByDesc() {
        var wrapper = Wrappers.<Object>query().orderByDesc(CREATED_AT);
        var node = (OrderNode) wrapper.nodes().get(0);
        assertEquals(OrderNode.Direction.DESC, node.direction());
    }

    @Test
    @DisplayName("multiple orderBy produce multiple OrderNodes")
    void multipleOrderBy() {
        var wrapper = Wrappers.<Object>query()
                .orderByDesc(CREATED_AT)
                .orderByDesc(ID);

        assertEquals(2, wrapper.nodes().size());
        assertInstanceOf(OrderNode.class, wrapper.nodes().get(0));
        assertInstanceOf(OrderNode.class, wrapper.nodes().get(1));
    }

    @Test
    @DisplayName("select creates SelectNode with field list")
    void selectCreatesNode() {
        var wrapper = Wrappers.<Object>query()
                .select(ID, STATUS, CREATED_AT);

        var node = (SelectNode) wrapper.nodes().get(0);
        assertEquals(3, node.fields().size());
        assertEquals("id", node.fields().get(0).column());
        assertEquals("status", node.fields().get(1).column());
    }
}
