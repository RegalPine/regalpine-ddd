package io.github.regalpine.ddd.infrastructure.transaction;

import io.github.regalpine.ddd.core.aggregate.AggregateRoot;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.transaction.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation of {@link UnitOfWork} for testing and development.
 *
 * @author RegalPine
 */
public final class InMemoryUnitOfWork implements UnitOfWork {

    private final String id;
    private UnitOfWorkStatus status;
    private final List<AggregateRoot<?>> tracked = new CopyOnWriteArrayList<>();

    public InMemoryUnitOfWork(String id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.status = UnitOfWorkStatus.NEW;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public UnitOfWorkStatus status() {
        return status;
    }

    @Override
    public <A extends AggregateRoot<I>, I extends Identifier> void register(A aggregate) {
        Objects.requireNonNull(aggregate, "aggregate must not be null");
        if (status != UnitOfWorkStatus.NEW && status != UnitOfWorkStatus.ACTIVE) {
            throw new IllegalStateException("Cannot register aggregates in status: " + status);
        }
        status = UnitOfWorkStatus.ACTIVE;
        tracked.add(aggregate);
    }

    @Override
    public boolean contains(Object aggregate) {
        return tracked.contains(aggregate);
    }

    @Override
    public List<AggregateRoot<?>> trackedAggregates() {
        return Collections.unmodifiableList(tracked);
    }

    @Override
    public void commit() {
        if (status != UnitOfWorkStatus.ACTIVE) {
            throw new IllegalStateException("Cannot commit in status: " + status);
        }
        status = UnitOfWorkStatus.COMMITTING;
        // commit logic
        status = UnitOfWorkStatus.COMMITTED;
    }

    @Override
    public void rollback() {
        if (status == UnitOfWorkStatus.COMMITTED || status == UnitOfWorkStatus.ROLLED_BACK
                || status == UnitOfWorkStatus.CLOSED) {
            throw new IllegalStateException("Cannot rollback in status: " + status);
        }
        status = UnitOfWorkStatus.ROLLING_BACK;
        tracked.clear();
        status = UnitOfWorkStatus.ROLLED_BACK;
    }

    @Override
    public boolean isActive() {
        return status == UnitOfWorkStatus.NEW || status == UnitOfWorkStatus.ACTIVE;
    }
}
