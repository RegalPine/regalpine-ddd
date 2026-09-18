package io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper;

/**
 * Static factory for creating {@link QueryWrapper} instances.
 *
 * <p>Phase XIII §21: the entry point for building type-safe queries.</p>
 *
 * <p>Example:</p>
 * <pre>
 * var wrapper = Wrappers.&lt;OrderRecord&gt;query()
 *     .eq(OrderFields.STATUS, "PAID")
 *     .orderByDesc(OrderFields.CREATED_AT);
 * </pre>
 *
 * @author RegalPine
 */
public final class Wrappers {

    private Wrappers() {
    }

    /**
     * Creates a new query wrapper.
     *
     * @param <T> the result type marker
     * @return a new query wrapper
     */
    public static <T> QueryWrapper<T> query() {
        return new DefaultQueryWrapper<>();
    }
}
