package io.github.regalpine.ddd.core.entity;

import io.github.regalpine.ddd.core.identifier.Identifier;

/**
 * Base abstraction for all domain entities.
 *
 * <p>An Entity is an object defined by its identity rather than its attributes.
 * Two entities with the same identity are considered equal, regardless of their
 * current state.</p>
 *
 * <p>Entity identity is immutable once assigned and must remain stable
 * throughout the entity's lifecycle.</p>
 *
 * @param <I> the identifier type
 */
public interface Entity<I extends Identifier> {

    /**
     * Returns the unique identity of this entity.
     *
     * @return the entity identifier, never {@code null}
     */
    I id();
}
