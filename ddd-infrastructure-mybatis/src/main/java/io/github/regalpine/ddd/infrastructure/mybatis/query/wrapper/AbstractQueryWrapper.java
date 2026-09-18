package io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper;

import io.github.regalpine.ddd.core.pagination.CountMode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.*;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.LogicalNode.LogicalOperator;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.OrderNode.Direction;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.CursorPagination;
import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.OffsetPagination;
import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.Pagination;

import java.util.*;
import java.util.function.Consumer;

/**
 * Abstract base implementation of {@link QueryWrapper} that builds an AST node list.
 *
 * <p>Phase XIII §20/§62: this mutable builder accumulates {@link QueryNode} instances.
 * Call {@link #freeze()} to produce an immutable {@link QueryPlan}.</p>
 *
 * <p>Phase XIII §14: empty IN produces a FALSE sentinel; empty NOT IN is a no-op (TRUE).</p>
 *
 * @param <T> the result type marker
 * @author RegalPine
 */
public abstract class AbstractQueryWrapper<T> implements QueryWrapper<T> {

    /**
     * Sentinel value used for empty IN clauses (§14).
     * The translator recognizes this and outputs "1 = 0".
     */
    public static final Object EMPTY_IN_FALSE = new Object();

    protected final List<QueryNode> nodeList = new ArrayList<>();
    protected Pagination pagination;
    protected CountMode currentCountMode = CountMode.EXACT;

    @Override
    public QueryWrapper<T> eq(QueryField<?> field, Object value) {
        nodeList.add(new ComparisonNode<>(field, ComparisonNode.Operator.EQ, value));
        return this;
    }

    @Override
    public QueryWrapper<T> ne(QueryField<?> field, Object value) {
        nodeList.add(new ComparisonNode<>(field, ComparisonNode.Operator.NE, value));
        return this;
    }

    @Override
    public QueryWrapper<T> gt(QueryField<?> field, Object value) {
        nodeList.add(new ComparisonNode<>(field, ComparisonNode.Operator.GT, value));
        return this;
    }

    @Override
    public QueryWrapper<T> ge(QueryField<?> field, Object value) {
        nodeList.add(new ComparisonNode<>(field, ComparisonNode.Operator.GE, value));
        return this;
    }

    @Override
    public QueryWrapper<T> lt(QueryField<?> field, Object value) {
        nodeList.add(new ComparisonNode<>(field, ComparisonNode.Operator.LT, value));
        return this;
    }

    @Override
    public QueryWrapper<T> le(QueryField<?> field, Object value) {
        nodeList.add(new ComparisonNode<>(field, ComparisonNode.Operator.LE, value));
        return this;
    }

    // --- Conditional variants (§22) ---

    @Override
    public QueryWrapper<T> eq(boolean condition, QueryField<?> field, Object value) {
        if (condition) eq(field, value);
        return this;
    }

    @Override
    public QueryWrapper<T> ne(boolean condition, QueryField<?> field, Object value) {
        if (condition) ne(field, value);
        return this;
    }

    @Override
    public QueryWrapper<T> gt(boolean condition, QueryField<?> field, Object value) {
        if (condition) gt(field, value);
        return this;
    }

    @Override
    public QueryWrapper<T> ge(boolean condition, QueryField<?> field, Object value) {
        if (condition) ge(field, value);
        return this;
    }

    @Override
    public QueryWrapper<T> lt(boolean condition, QueryField<?> field, Object value) {
        if (condition) lt(field, value);
        return this;
    }

    @Override
    public QueryWrapper<T> le(boolean condition, QueryField<?> field, Object value) {
        if (condition) le(field, value);
        return this;
    }

    // --- Null checks ---

    @Override
    public QueryWrapper<T> isNull(QueryField<?> field) {
        nodeList.add(new NullNode(field, false));
        return this;
    }

    @Override
    public QueryWrapper<T> isNotNull(QueryField<?> field) {
        nodeList.add(new NullNode(field, true));
        return this;
    }

    // --- IN / NOT IN (§13-§14) ---

    @SuppressWarnings("unchecked")
    @Override
    public QueryWrapper<T> in(QueryField<?> field, Collection<?> values) {
        Objects.requireNonNull(values, "values must not be null");
        if (values.isEmpty()) {
            // §14: empty IN → FALSE sentinel
            nodeList.add(new ComparisonNode<>(field, ComparisonNode.Operator.EQ, EMPTY_IN_FALSE));
        } else {
            nodeList.add(new InNode(field, (List<Object>) new ArrayList<>(values), false));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public QueryWrapper<T> notIn(QueryField<?> field, Collection<?> values) {
        Objects.requireNonNull(values, "values must not be null");
        if (values.isEmpty()) {
            // §14: empty NOT IN → TRUE (no-op, all rows match)
            return this;
        }
        nodeList.add(new InNode(field, (List<Object>) new ArrayList<>(values), true));
        return this;
    }

    // --- BETWEEN (§15) ---

    @SuppressWarnings("unchecked")
    @Override
    public <V> QueryWrapper<T> between(QueryField<V> field, V lower, V upper) {
        nodeList.add(new BetweenNode<>(field, lower, upper, false));
        return this;
    }

    // --- LIKE (§16) ---

    @Override
    public QueryWrapper<T> like(QueryField<String> field, String value) {
        nodeList.add(new LikeNode(field, value, LikeNode.LikeMode.ANYWHERE, false));
        return this;
    }

    // --- Logical grouping (§19) ---

    @Override
    public QueryWrapper<T> and(Consumer<QueryWrapper<T>> consumer) {
        var nested = createNested();
        consumer.accept(nested);
        nodeList.add(new NestedNode(LogicalOperator.AND, nested.nodeList.stream()
                .filter(ConditionNode.class::isInstance)
                .map(ConditionNode.class::cast)
                .toList()));
        return this;
    }

    @Override
    public QueryWrapper<T> or(Consumer<QueryWrapper<T>> consumer) {
        var nested = createNested();
        consumer.accept(nested);
        nodeList.add(new NestedNode(LogicalOperator.OR, nested.nodeList.stream()
                .filter(ConditionNode.class::isInstance)
                .map(ConditionNode.class::cast)
                .toList()));
        return this;
    }

    // --- ORDER BY (§26) ---

    @Override
    public QueryWrapper<T> orderByAsc(QueryField<?> field) {
        nodeList.add(new OrderNode(field, Direction.ASC));
        return this;
    }

    @Override
    public QueryWrapper<T> orderByDesc(QueryField<?> field) {
        nodeList.add(new OrderNode(field, Direction.DESC));
        return this;
    }

    // --- SELECT (§28) ---

    @Override
    public QueryWrapper<T> select(QueryField<?>... fields) {
        nodeList.add(new SelectNode(List.of(fields)));
        return this;
    }

    // --- Pagination (§36) ---

    @Override
    public QueryWrapper<T> page(int page, int size) {
        var offsetPag = new OffsetPagination(page, size);
        this.pagination = offsetPag;
        nodeList.add(new PaginationNode(offsetPag));
        return this;
    }

    @Override
    public QueryWrapper<T> cursor(String cursor, int size) {
        var cursorPag = new CursorPagination(cursor, size);
        this.pagination = cursorPag;
        nodeList.add(new PaginationNode(cursorPag));
        return this;
    }

    // --- Count mode ---

    @Override
    public QueryWrapper<T> countMode(CountMode mode) {
        this.currentCountMode = Objects.requireNonNull(mode, "countMode must not be null");
        return this;
    }

    // --- AST access ---

    @Override
    public List<QueryNode> nodes() {
        return Collections.unmodifiableList(nodeList);
    }

    @Override
    public Pagination pagination() {
        return pagination;
    }

    @Override
    public CountMode countMode() {
        return currentCountMode;
    }

    @Override
    public QueryPlan freeze() {
        return new QueryPlan(nodeList, pagination, currentCountMode);
    }

    /**
     * Creates a nested wrapper for sub-query building.
     */
    protected abstract NestedQueryWrapper<T> createNested();
}
