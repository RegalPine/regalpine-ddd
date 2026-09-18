package io.github.regalpine.ddd.infrastructure.mybatis.query.field;

/**
 * A type-safe reference to a database column.
 *
 * <p>Phase XIII §5: prevents arbitrary SQL strings from entering the query builder.
 * All field references must go through {@link QueryField} instances, which carry
 * both the column name and the Java type for compile-time safety.</p>
 *
 * @param <T> the Java type of the field value
 * @author RegalPine
 */
public interface QueryField<T> {

    /**
     * Returns the database column name.
     */
    String column();

    /**
     * Returns the Java type of this field.
     */
    Class<T> javaType();
}
