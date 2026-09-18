package io.github.regalpine.ddd.infrastructure.mybatis.query.field;

/**
 * Convenience factory methods for {@code QueryField<String>}.
 *
 * <p>Phase XIII §5/§7: provides a concise way to define string-typed field constants.</p>
 *
 * @author RegalPine
 */
public final class StringField {

    private StringField() {
    }

    /**
     * Creates a string-typed query field.
     *
     * @param column the database column name
     * @return a {@code QueryField<String>}
     */
    public static QueryField<String> of(String column) {
        return QueryFields.of(column, String.class);
    }
}
