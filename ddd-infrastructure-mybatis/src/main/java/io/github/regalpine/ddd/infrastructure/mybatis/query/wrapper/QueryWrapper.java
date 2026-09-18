package io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper;

import io.github.regalpine.ddd.core.pagination.CountMode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.QueryNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.Pagination;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Chainable query builder that produces an immutable Query AST.
 *
 * <p>Phase XIII §20: the core API for building queries in a type-safe, fluent manner.
 * All methods return {@code this} for chaining. The wrapper is mutable (§62) but
 * produces immutable AST nodes and can be frozen into a {@link QueryPlan}.</p>
 *
 * <p>Phase XIII §22: conditional variants (e.g. {@code eq(boolean, QueryField, Object)})
 * allow skipping conditions when the boolean is false, avoiding if/else blocks.</p>
 *
 * <p>Phase XIII §56-§57: QueryWrapper belongs to Query Infrastructure only.
 * It must NOT enter the Domain Model.</p>
 *
 * @param <T> the result type marker
 * @author RegalPine
 */
public interface QueryWrapper<T> {

    // --- Comparison operations (§20) ---

    QueryWrapper<T> eq(QueryField<?> field, Object value);

    QueryWrapper<T> ne(QueryField<?> field, Object value);

    QueryWrapper<T> gt(QueryField<?> field, Object value);

    QueryWrapper<T> ge(QueryField<?> field, Object value);

    QueryWrapper<T> lt(QueryField<?> field, Object value);

    QueryWrapper<T> le(QueryField<?> field, Object value);

    // --- Conditional comparison operations (§22) ---

    QueryWrapper<T> eq(boolean condition, QueryField<?> field, Object value);

    QueryWrapper<T> ne(boolean condition, QueryField<?> field, Object value);

    QueryWrapper<T> gt(boolean condition, QueryField<?> field, Object value);

    QueryWrapper<T> ge(boolean condition, QueryField<?> field, Object value);

    QueryWrapper<T> lt(boolean condition, QueryField<?> field, Object value);

    QueryWrapper<T> le(boolean condition, QueryField<?> field, Object value);

    // --- Null checks (§20) ---

    QueryWrapper<T> isNull(QueryField<?> field);

    QueryWrapper<T> isNotNull(QueryField<?> field);

    // --- IN / NOT IN (§13-§14) ---

    QueryWrapper<T> in(QueryField<?> field, Collection<?> values);

    QueryWrapper<T> notIn(QueryField<?> field, Collection<?> values);

    // --- BETWEEN (§15) ---

    <V> QueryWrapper<T> between(QueryField<V> field, V lower, V upper);

    // --- LIKE (§16) ---

    QueryWrapper<T> like(QueryField<String> field, String value);

    // --- Logical grouping (§19) ---

    QueryWrapper<T> and(Consumer<QueryWrapper<T>> consumer);

    QueryWrapper<T> or(Consumer<QueryWrapper<T>> consumer);

    // --- ORDER BY (§26) ---

    QueryWrapper<T> orderByAsc(QueryField<?> field);

    QueryWrapper<T> orderByDesc(QueryField<?> field);

    // --- SELECT (§28) ---

    QueryWrapper<T> select(QueryField<?>... fields);

    // --- Pagination (§36) ---

    QueryWrapper<T> page(int page, int size);

    QueryWrapper<T> cursor(String cursor, int size);

    // --- Count mode ---

    QueryWrapper<T> countMode(CountMode mode);

    // --- AST access ---

    /**
     * Returns the current list of AST nodes.
     */
    List<QueryNode> nodes();

    /**
     * Returns the current pagination, or null if none set.
     */
    Pagination pagination();

    /**
     * Returns the current count mode.
     */
    CountMode countMode();

    /**
     * Freezes this wrapper into an immutable {@link QueryPlan}.
     *
     * <p>Phase XIII §62: the plan can be cached, audited, or tested independently.</p>
     */
    QueryPlan freeze();
}
