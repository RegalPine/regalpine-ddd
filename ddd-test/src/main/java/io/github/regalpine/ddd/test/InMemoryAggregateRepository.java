package io.github.regalpine.ddd.test;

import io.github.regalpine.ddd.core.aggregate.AggregateRoot;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.domain.repository.AggregateRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory aggregate repository for testing.
 *
 * @param <A> the aggregate root type
 * @param <I> the identifier type
 * @author RegalPine
 */
public final class InMemoryAggregateRepository<A extends AggregateRoot<I>, I extends Identifier>
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
        store.put(aggregate.id().value(), aggregate);
        return aggregate;
    }

    @Override
    public void delete(A aggregate) {
        Objects.requireNonNull(aggregate, "aggregate must not be null");
        store.remove(aggregate.id().value());
    }

    /**
     * Returns all stored aggregates (for testing).
     */
    public Collection<A> allAggregates() {
        return Collections.unmodifiableCollection(store.values());
    }

    /**
     * Clears all stored aggregates (for testing).
     */
    public void clear() {
        store.clear();
    }
}
