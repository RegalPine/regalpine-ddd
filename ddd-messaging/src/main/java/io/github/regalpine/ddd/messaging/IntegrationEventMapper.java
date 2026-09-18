package io.github.regalpine.ddd.messaging;

/**
 * Maps domain events to integration events for cross-boundary communication.
 *
 * <p>Phase IX §6: Direct auto-exposure of all Domain Events as Integration Events
 * is prohibited. An explicit mapper must translate between the two models.</p>
 *
 * <p>Reasons (per §6):
 * <ol>
 *   <li>Domain Event belongs to internal model.</li>
 *   <li>Integration Event belongs to stable external contract.</li>
 *   <li>Domain Model can evolve independently.</li>
 *   <li>Integration Contract should evolve independently.</li>
 *   <li>Prevents internal model leakage.</li>
 * </ol>
 * </p>
 *
 * @param <D> the domain event type
 * @param <I> the integration event type
 * @author RegalPine
 */
public interface IntegrationEventMapper<D, I> {

    /**
     * Maps a domain event to an integration event.
     *
     * @param domainEvent the source domain event
     * @return the mapped integration event
     */
    I map(D domainEvent);
}
