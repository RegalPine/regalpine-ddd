package io.github.regalpine.ddd.infrastructure.event;

import io.github.regalpine.ddd.core.event.DomainEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory event store for domain events.
 *
 * <p>Phase X §33: InMemoryEventStore is required for unit tests, integration tests,
 * conformance tests, demos, and prototypes.</p>
 *
 * <p>Stores domain events in memory, indexed by aggregate type and aggregate ID.
 * Supports append and query operations.</p>
 *
 * @author RegalPine
 */
public final class InMemoryEventStore {

    private final List<DomainEvent> events = new CopyOnWriteArrayList<>();
    private final Map<String, List<DomainEvent>> byAggregateId = new ConcurrentHashMap<>();

    /**
     * Appends a domain event to the store.
     *
     * @param event the domain event to append
     */
    public void append(DomainEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        events.add(event);
        String aggId = event.aggregateId().value();
        byAggregateId.computeIfAbsent(aggId, k -> new CopyOnWriteArrayList<>()).add(event);
    }

    /**
     * Appends multiple domain events to the store.
     *
     * @param domainEvents the domain events to append
     */
    public void appendAll(Collection<? extends DomainEvent> domainEvents) {
        Objects.requireNonNull(domainEvents, "events must not be null");
        domainEvents.forEach(this::append);
    }

    /**
     * Finds all events for the given aggregate identifier.
     *
     * @param aggregateId the aggregate identifier value
     * @return an unmodifiable list of events, empty if none found
     */
    public List<DomainEvent> findByAggregateId(String aggregateId) {
        Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        List<DomainEvent> found = byAggregateId.get(aggregateId);
        return found != null ? Collections.unmodifiableList(found) : List.of();
    }

    /**
     * Finds all events for the given aggregate type.
     *
     * @param aggregateType the aggregate type name
     * @return an unmodifiable list of matching events
     */
    public List<DomainEvent> findByAggregateType(String aggregateType) {
        Objects.requireNonNull(aggregateType, "aggregateType must not be null");
        return events.stream()
                .filter(e -> aggregateType.equals(e.aggregateType().value()))
                .toList();
    }

    /**
     * Returns all stored events.
     *
     * @return an unmodifiable list of all events
     */
    public List<DomainEvent> findAll() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Returns the total number of stored events.
     *
     * @return the event count
     */
    public int size() {
        return events.size();
    }

    /**
     * Clears all stored events.
     */
    public void clear() {
        events.clear();
        byAggregateId.clear();
    }
}
