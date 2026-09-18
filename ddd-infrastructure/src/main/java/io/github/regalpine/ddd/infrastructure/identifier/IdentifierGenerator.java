package io.github.regalpine.ddd.infrastructure.identifier;

import io.github.regalpine.ddd.core.identifier.Identifier;

/**
 * SPI for generating domain identifiers.
 *
 * <p>Implementations may use UUID, ULID, sequential counters,
 * or any other strategy appropriate for the domain.</p>
 *
 * <p>Per Phase II §54/§58, IdentifierGenerator belongs to
 * {@code ddd-port} (or {@code ddd-infrastructure}), not
 * {@code ddd-core}, because ID generation is an
 * infrastructure capability.</p>
 *
 * @param <I> the identifier type
 */
@FunctionalInterface
public interface IdentifierGenerator<I extends Identifier> {

    /**
     * Generates a new identifier.
     *
     * @return a new identifier instance, never {@code null}
     */
    I generate();
}
