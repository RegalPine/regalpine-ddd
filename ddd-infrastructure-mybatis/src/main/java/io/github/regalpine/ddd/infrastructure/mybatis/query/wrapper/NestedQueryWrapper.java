package io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper;

/**
 * Nested query wrapper used inside {@code and(Consumer)} / {@code or(Consumer)} blocks.
 *
 * <p>Phase XIII §19: collects conditions for a parenthesized sub-expression.
 * The parent wrapper wraps these conditions in a {@code NestedNode}.</p>
 *
 * @param <T> the result type marker
 * @author RegalPine
 */
public class NestedQueryWrapper<T> extends AbstractQueryWrapper<T> {

    @Override
    protected NestedQueryWrapper<T> createNested() {
        return new NestedQueryWrapper<>();
    }
}
