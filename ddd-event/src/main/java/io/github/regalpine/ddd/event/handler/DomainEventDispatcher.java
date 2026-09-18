package io.github.regalpine.ddd.event.handler;

import io.github.regalpine.ddd.core.event.DomainEvent;

import java.util.List;

/**
 * Dispatches domain events to their registered handlers.
 *
 * <p>Phase XI §43: supports both single-event and batch dispatch.</p>
 *
 * @author RegalPine
 */
public interface DomainEventDispatcher {

    /**
     * Dispatches a single domain event to its registered handlers.
     *
     * @param event the event to dispatch
     */
    void dispatch(DomainEvent event);

    /**
     * Dispatches a list of domain events to their registered handlers.
     *
     * <p>Phase XI §43: {@code dispatch(List<DomainEvent>)}.</p>
     *
     * @param events the events to dispatch
     */
    default void dispatch(List<DomainEvent> events) {
        events.forEach(this::dispatch);
    }
}
