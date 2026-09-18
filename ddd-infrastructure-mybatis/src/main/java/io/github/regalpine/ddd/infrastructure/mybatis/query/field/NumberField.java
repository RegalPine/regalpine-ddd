package io.github.regalpine.ddd.infrastructure.mybatis.query.field;

/**
 * Convenience factory methods for numeric {@link QueryField} types.
 *
 * <p>Phase XIII §5/§7: provides factory methods for Integer, Long, Double, etc.</p>
 *
 * @author RegalPine
 */
public final class NumberField {

    private NumberField() {
    }

    public static QueryField<Integer> integer(String column) {
        return QueryFields.of(column, Integer.class);
    }

    public static QueryField<Long> longOf(String column) {
        return QueryFields.of(column, Long.class);
    }

    public static QueryField<Double> doubleOf(String column) {
        return QueryFields.of(column, Double.class);
    }

    public static QueryField<Float> floatOf(String column) {
        return QueryFields.of(column, Float.class);
    }

    public static QueryField<Short> shortOf(String column) {
        return QueryFields.of(column, Short.class);
    }
}
