package io.github.regalpine.ddd.core.event;

import java.util.List;

/**
 * Source of domain events produced by an aggregate root.
 *
 * <p>Implemented by {@link io.github.regalpine.ddd.core.aggregate.AggregateRoot}
 * to collect domain events produced during aggregate behavior execution.
 * Pending events are collected until the aggregate is persisted and
 * the transaction commits.</p>
 *
 * <p>Per Phase II §42/§47, method names are {@code domainEvents()} and
 * {@code clearDomainEvents()}.</p>
 */
public interface DomainEventSource {

    /**
     * Returns the list of pending domain events that have not yet been
     * dispatched or cleared.
     *
     * <p>The returned list is an unmodifiable snapshot. Callers cannot
     * modify the internal event collection.</p>
     *
     * @return an unmodifiable list of pending events, never {@code null}
     */
    List<DomainEvent> domainEvents();

    /**
     * Clears all pending domain events.
     *
     * <p>This is typically called by the repository or unit of work
     * after events have been successfully collected for dispatch.</p>
     */
    void clearDomainEvents();
}
