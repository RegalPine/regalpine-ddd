package io.github.regalpine.ddd.infrastructure.mybatis.mapping;

import io.github.regalpine.ddd.core.event.DomainEvent;

/**
 * Maps domain events to and from persistence records for outbox/inbox storage.
 *
 * <p>Phase XII §5/§52: domain events are serialized for outbox persistence
 * and deserialized for replay. The mapping must not lose event identity
 * or ordering information.</p>
 *
 * @param <E> the domain event type
 * @param <R> the persistence record type
 * @author RegalPine
 */
public interface DomainEventMapper<E extends DomainEvent, R> {

    /**
     * Converts a domain event to its persistence record.
     *
     * @param event the domain event
     * @return the persistence record
     */
    R toRecord(E event);

    /**
     * Converts a persistence record back to a domain event.
     *
     * @param record the persistence record
     * @return the reconstructed domain event
     */
    E toEvent(R record);
}
