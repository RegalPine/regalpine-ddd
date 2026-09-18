package io.github.regalpine.ddd.core.aggregate;

import io.github.regalpine.ddd.core.entity.Entity;
import io.github.regalpine.ddd.core.event.DomainEventSource;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;

/**
 * Root entity of an aggregate, serving as the sole entry point
 * for external access to the aggregate's internal state and behavior.
 *
 * <p>An Aggregate Root combines identity (from {@link Entity}),
 * event production (from {@link DomainEventSource}), and optimistic
 * concurrency control (via {@link #version()}).</p>
 *
 * <p>External code (Application Layer, other aggregates) may only
 * reference and operate on the aggregate through its root.</p>
 *
 * @param <I> the identifier type
 */
public interface AggregateRoot<I extends Identifier> extends Entity<I>, DomainEventSource {

    /**
     * Returns the current optimistic concurrency version of this aggregate.
     *
     * <p>The version is managed by the persistence adapter and incremented
     * on each successful save. It is not a business concept.</p>
     *
     * @return the current version, never {@code null}
     */
    Version version();
}
