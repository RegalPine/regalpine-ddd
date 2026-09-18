package io.github.regalpine.ddd.event.projection;

import io.github.regalpine.ddd.core.event.DomainEvent;

/**
 * A projection that builds read models from domain events.
 * <p>
 * Projections are event handlers that transform domain events into
 * query-optimized read models for the CQRS query side.
 *
 * @param <E> the domain event type
 * @author RegalPine
 */
public interface Projection<E extends DomainEvent> {

    /**
     * Projects the given event into the read model.
     *
     * @param event the domain event
     */
    void project(E event);

    /**
     * Returns the event type this projection handles.
     */
    Class<E> eventType();
}
