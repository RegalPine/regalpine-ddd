package io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper;

import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.ComparisonNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.NullNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.InNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.BetweenNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.LikeNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QueryWrapper - basic chain API (§20, §69)")
class QueryWrapperTest {

    static final QueryField<String> STATUS = QueryFields.of("status", String.class);
    static final QueryField<String> CUSTOMER_ID = QueryFields.of("customer_id", String.class);

    @Test
    @DisplayName("eq creates ComparisonNode with EQ operator")
    void eqCreatesComparisonNode() {
        var wrapper = Wrappers.<Object>query().eq(STATUS, "PAID");

        assertEquals(1, wrapper.nodes().size());
        var node = (ComparisonNode<?>) wrapper.nodes().get(0);
        assertEquals(ComparisonNode.Operator.EQ, node.operator());
        assertEquals("PAID", node.value());
        assertEquals("status", node.field().column());
    }

    @Test
    @DisplayName("chaining multiple conditions")
    void chainingMultipleConditions() {
        var wrapper = Wrappers.<Object>query()
                .eq(STATUS, "PAID")
                .eq(CUSTOMER_ID, "C001")
                .gt(STATUS, "A");

        assertEquals(3, wrapper.nodes().size());
        assertInstanceOf(ComparisonNode.class, wrapper.nodes().get(0));
        assertInstanceOf(ComparisonNode.class, wrapper.nodes().get(1));
        assertInstanceOf(ComparisonNode.class, wrapper.nodes().get(2));
    }

    @Test
    @DisplayName("isNull creates NullNode with negated=false")
    void isNullCreatesNode() {
        var wrapper = Wrappers.<Object>query().isNull(STATUS);
        var node = (NullNode) wrapper.nodes().get(0);
        assertFalse(node.negated());
    }

    @Test
    @DisplayName("isNotNull creates NullNode with negated=true")
    void isNotNullCreatesNode() {
        var wrapper = Wrappers.<Object>query().isNotNull(STATUS);
        var node = (NullNode) wrapper.nodes().get(0);
        assertTrue(node.negated());
    }

    @Test
    @DisplayName("in creates InNode with values")
    void inCreatesNode() {
        var wrapper = Wrappers.<Object>query().in(STATUS, java.util.List.of("A", "B"));
        var node = (InNode<?>) wrapper.nodes().get(0);
        assertEquals(2, node.values().size());
        assertFalse(node.negated());
    }

    @Test
    @DisplayName("notIn creates InNode with negated=true")
    void notInCreatesNode() {
        var wrapper = Wrappers.<Object>query().notIn(STATUS, java.util.List.of("A"));
        var node = (InNode<?>) wrapper.nodes().get(0);
        assertTrue(node.negated());
    }

    @Test
    @DisplayName("between creates BetweenNode")
    void betweenCreatesNode() {
        var wrapper = Wrappers.<Object>query().between(STATUS, "A", "Z");
        var node = (BetweenNode<?>) wrapper.nodes().get(0);
        assertEquals("A", node.lower());
        assertEquals("Z", node.upper());
        assertFalse(node.negated());
    }

    @Test
    @DisplayName("like creates LikeNode with ANYWHERE mode")
    void likeCreatesNode() {
        var wrapper = Wrappers.<Object>query().like(STATUS, "test");
        var node = (LikeNode) wrapper.nodes().get(0);
        assertEquals("test", node.value());
        assertEquals(LikeNode.LikeMode.ANYWHERE, node.mode());
        assertFalse(node.negated());
    }

    @Test
    @DisplayName("freeze produces immutable QueryPlan")
    void freezeProducesPlan() {
        var wrapper = Wrappers.<Object>query()
                .eq(STATUS, "PAID")
                .page(0, 20);

        var plan = wrapper.freeze();
        assertNotNull(plan);
        assertEquals(2, plan.nodes().size()); // ComparisonNode + PaginationNode
        assertNotNull(plan.pagination());
    }
}
