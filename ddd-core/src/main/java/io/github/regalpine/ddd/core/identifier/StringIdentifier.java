package io.github.regalpine.ddd.core.identifier;

/**
 * Reference implementation of {@link Identifier} wrapping a non-blank String.
 *
 * <p>Phase XI §9: String-based identifier with validation.
 * Immutable, stable, comparable by value.</p>
 *
 * @param value the identifier value, must not be null or blank
 * @author RegalPine
 */
public record StringIdentifier(String value) implements Identifier {

    public StringIdentifier {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Identifier value must not be blank");
        }
    }
}
