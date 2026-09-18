package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.OffsetPagination;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Query AST - immutability and sealed hierarchy (§9-§28, §62)")
class QueryAstTest {

    @Test
    @DisplayName("InNode values are defensively copied")
    void inNodeDefensiveCopy() {
        var field = QueryFields.of("status", String.class);
        var mutableList = new java.util.ArrayList<>(List.of("A", "B"));
        var node = new InNode<>(field, mutableList, false);

        mutableList.add("C");
        assertEquals(2, node.values().size(), "InNode should be immutable");
    }

    @Test
    @DisplayName("SelectNode fields are defensively copied")
    void selectNodeDefensiveCopy() {
        var f1 = QueryFields.of("id", String.class);
        var f2 = QueryFields.of("name", String.class);
        var mutableList = new java.util.ArrayList<QueryField<?>>(List.of(f1));
        var node = new SelectNode(mutableList);

        mutableList.add(f2);
        assertEquals(1, node.fields().size(), "SelectNode should be immutable");
    }

    @Test
    @DisplayName("LogicalNode children are defensively copied")
    void logicalNodeDefensiveCopy() {
        var field = QueryFields.of("a", Integer.class);
        ConditionNode cond = new ComparisonNode<>(field, ComparisonNode.Operator.EQ, 1);
        var mutableList = new java.util.ArrayList<ConditionNode>(List.of(cond));
        var node = new LogicalNode(LogicalNode.LogicalOperator.AND, mutableList);

        assertEquals(1, node.children().size());
    }

    @Test
    @DisplayName("QueryNode sealed hierarchy is correct")
    void sealedHierarchy() {
        assertTrue(ConditionNode.class.isAssignableFrom(ComparisonNode.class));
        assertTrue(ConditionNode.class.isAssignableFrom(NullNode.class));
        assertTrue(ConditionNode.class.isAssignableFrom(InNode.class));
        assertTrue(ConditionNode.class.isAssignableFrom(BetweenNode.class));
        assertTrue(ConditionNode.class.isAssignableFrom(LikeNode.class));

        assertTrue(QueryNode.class.isAssignableFrom(ConditionNode.class));
        assertTrue(QueryNode.class.isAssignableFrom(LogicalNode.class));
        assertTrue(QueryNode.class.isAssignableFrom(NestedNode.class));
        assertTrue(QueryNode.class.isAssignableFrom(OrderNode.class));
        assertTrue(QueryNode.class.isAssignableFrom(SelectNode.class));
        assertTrue(QueryNode.class.isAssignableFrom(PaginationNode.class));
    }

    @Test
    @DisplayName("PaginationNode wraps Pagination")
    void paginationNode() {
        var pag = new OffsetPagination(0, 20);
        var node = new PaginationNode(pag);
        assertSame(pag, node.pagination());
    }
}
