package io.github.regalpine.ddd.infrastructure.mybatis.mapping;

/**
 * Maps value objects to and from database column values.
 *
 * <p>Phase XII §50: value objects are persisted as their underlying
 * database representation (e.g. VARCHAR for string-based identifiers).
 * The mapping must preserve value equality semantics.</p>
 *
 * @param <V> the value object type
 * @param <D> the database value type
 * @author RegalPine
 */
public interface ValueObjectMapper<V, D> {

    /**
     * Converts a value object to its database representation.
     *
     * @param valueObject the value object
     * @return the database value
     */
    D toDatabase(V valueObject);

    /**
     * Converts a database value back to a value object.
     *
     * @param databaseValue the database value
     * @return the value object
     */
    V toValueObject(D databaseValue);
}
