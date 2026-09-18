package io.github.regalpine.ddd.core.identifier;

/**
 * Base abstraction for all domain identifiers.
 *
 * <p>An Identifier is an immutable, stable value that uniquely identifies
 * a domain entity. Implementations must provide value-based equality.</p>
 *
 * <p>Recommended implementations: {@code record} types wrapping a String,
 * UUID, or other stable value.</p>
 */
public interface Identifier {

    /**
     * Returns the string representation of this identifier.
     *
     * @return the identifier value, never {@code null}
     */
    String value();
}
