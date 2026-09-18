package io.github.regalpine.ddd.messaging;

/**
 * Handles integration events of a specific type.
 *
 * <p>Phase IX §31: Handler should not directly depend on Broker API.
 * It operates at the integration abstraction level.</p>
 *
 * @param <E> the integration event type
 * @author RegalPine
 */
public interface IntegrationEventHandler<E> {

    /**
     * Handles the given integration event.
     *
     * @param event the integration event
     */
    void handle(E event);
}
