package io.github.regalpine.ddd.infrastructure.mybatis.query.field;

/**
 * Convenience factory methods for {@code QueryField<Boolean>}.
 *
 * <p>Phase XIII §5/§7: provides a concise way to define boolean-typed field constants.</p>
 *
 * @author RegalPine
 */
public final class BooleanField {

    private BooleanField() {
    }

    /**
     * Creates a boolean-typed query field.
     *
     * @param column the database column name
     * @return a {@code QueryField<Boolean>}
     */
    public static QueryField<Boolean> of(String column) {
        return QueryFields.of(column, Boolean.class);
    }
}
