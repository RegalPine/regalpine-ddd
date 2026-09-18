package io.github.regalpine.ddd.infrastructure.persistence;

import io.github.regalpine.ddd.core.aggregate.AggregateRoot;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.domain.repository.AggregateRepository;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link AggregateRepository}.
 *
 * <p>Phase X §33: InMemoryRepository is required for unit tests, integration tests,
 * conformance tests, demos, and prototypes.</p>
 *
 * <p>Stores aggregates in a {@link ConcurrentHashMap} keyed by the string value
 * of the aggregate identifier. Thread-safe for concurrent access.</p>
 *
 * @param <A> the aggregate root type
 * @param <I> the identifier type
 * @author RegalPine
 */
public final class InMemoryRepository<A extends AggregateRoot<I>, I extends Identifier>
        implements AggregateRepository<A, I> {

    private final Map<String, A> store = new ConcurrentHashMap<>();

    @Override
    public Optional<A> findById(I id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(store.get(id.value()));
    }

    @Override
    public A save(A aggregate) {
        Objects.requireNonNull(aggregate, "aggregate must not be null");
        I id = aggregate.id();
        Objects.requireNonNull(id, "aggregate id must not be null");
        store.put(id.value(), aggregate);
        return aggregate;
    }

    @Override
    public void delete(A aggregate) {
        Objects.requireNonNull(aggregate, "aggregate must not be null");
        I id = aggregate.id();
        Objects.requireNonNull(id, "aggregate id must not be null");
        store.remove(id.value());
    }

    /**
     * Returns the number of aggregates currently stored.
     *
     * @return the aggregate count
     */
    public int size() {
        return store.size();
    }

    /**
     * Clears all stored aggregates.
     */
    public void clear() {
        store.clear();
    }
}
