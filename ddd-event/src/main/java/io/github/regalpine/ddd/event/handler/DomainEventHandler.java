package io.github.regalpine.ddd.event.handler;

import io.github.regalpine.ddd.core.event.DomainEvent;

/**
 * Handler for a specific type of domain event.
 *
 * @param <E> the domain event type
 * @author RegalPine
 */
public interface DomainEventHandler<E extends DomainEvent> {

    /**
     * Handles the given domain event.
     *
     * @param event the event to handle
     */
    void handle(E event);
}
