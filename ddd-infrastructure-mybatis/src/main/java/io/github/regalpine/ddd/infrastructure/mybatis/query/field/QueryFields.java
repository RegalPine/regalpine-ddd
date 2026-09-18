package io.github.regalpine.ddd.infrastructure.mybatis.query.field;

/**
 * Factory for {@link QueryField} instances.
 *
 * <p>Phase XIII §6: the recommended way to create type-safe field references.</p>
 *
 * <p>Example:</p>
 * <pre>
 * public static final QueryField&lt;String&gt; STATUS =
 *     QueryFields.of("status", String.class);
 * </pre>
 *
 * @author RegalPine
 */
public final class QueryFields {

    private QueryFields() {
    }

    /**
     * Creates a new {@link QueryField} for the given column and type.
     *
     * @param column the database column name
     * @param type   the Java type of the column
     * @param <T>    the Java type
     * @return a new query field
     */
    public static <T> QueryField<T> of(String column, Class<T> type) {
        return new DefaultQueryField<>(column, type);
    }
}
