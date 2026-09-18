package io.github.regalpine.ddd.core.value;

/**
 * Base abstraction for all value objects.
 *
 * <p>A Value Object is an immutable object defined solely by its attribute values,
 * rather than by a conceptual identity. Two value objects with the same values
 * are considered equal.</p>
 *
 * <p>Recommended implementation: Java {@code record}.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public record Money(BigDecimal amount, Currency currency)
 *         implements ValueObject {
 * }
 * }</pre>
 */
public interface ValueObject {
}
