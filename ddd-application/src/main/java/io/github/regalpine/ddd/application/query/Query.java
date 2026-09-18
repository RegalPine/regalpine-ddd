package io.github.regalpine.ddd.application.query;

/**
 * Represents a query requesting information from the system.
 *
 * <p>A Query does not change business state. It is handled by
 * a {@link QueryHandler} and typically reads from a read model
 * rather than loading aggregates.</p>
 *
 * <p>Recommended implementation: Java {@code record}.</p>
 *
 * @param <R> the result type
 */
public interface Query<R> {
}
