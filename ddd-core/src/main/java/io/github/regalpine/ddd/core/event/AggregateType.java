package io.github.regalpine.ddd.core.event;

/**
 * Identifies the type of aggregate that produced a domain event.
 *
 * <p>Example values: {@code "order"}, {@code "customer"}, {@code "product"}.</p>
 *
 * @param value the aggregate type name, must not be blank
 */
public record AggregateType(String value) {

    public AggregateType {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Aggregate type must not be blank");
        }
    }

    /**
     * Factory method for creating an AggregateType.
     *
     * @param value the aggregate type name
     * @return a new AggregateType
     */
    public static AggregateType of(String value) {
        return new AggregateType(value);
    }
}
