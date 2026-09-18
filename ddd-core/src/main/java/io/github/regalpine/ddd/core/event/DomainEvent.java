package io.github.regalpine.ddd.core.event;

import io.github.regalpine.ddd.core.identifier.Identifier;

import java.time.Instant;

/**
 * Represents a business fact that has occurred within the domain.
 *
 * <p>A Domain Event is an immutable record of something that happened
 * in the domain. It is produced by an Aggregate Root and collected
 * as pending events until the aggregate is persisted and the
 * transaction commits.</p>
 *
 * <p>Per Phase II §22, {@code aggregateId()} returns {@link Identifier}
 * (not {@code String}) to maintain type safety across the framework.</p>
 *
 * <p>Domain Events are not transport messages. They belong to the
 * domain model and must not depend on any infrastructure concerns.</p>
 */
public interface DomainEvent {

    /**
     * Returns the unique identifier of this event.
     *
     * @return the event ID, never {@code null}
     */
    EventId eventId();

    /**
     * Returns the type of aggregate that produced this event.
     *
     * @return the aggregate type, never {@code null}
     */
    AggregateType aggregateType();

    /**
     * Returns the identifier of the aggregate that produced this event.
     *
     * <p>Returns {@link Identifier} to preserve type safety — callers
     * may narrow to the concrete identifier type if needed.</p>
     *
     * @return the aggregate identifier, never {@code null}
     */
    Identifier aggregateId();

    /**
     * Returns the aggregate version at the time this event was recorded.
     *
     * @return the aggregate version, never {@code null}
     */
    io.github.regalpine.ddd.core.version.Version aggregateVersion();

    /**
     * Returns the instant when this event occurred in the domain.
     *
     * <p>This represents the time the business fact occurred,
     * not the time it was published or persisted.</p>
     *
     * @return the occurrence timestamp, never {@code null}
     */
    Instant occurredAt();
}
