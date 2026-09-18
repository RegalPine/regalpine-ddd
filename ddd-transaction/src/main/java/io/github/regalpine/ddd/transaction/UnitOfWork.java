package io.github.regalpine.ddd.transaction;

import io.github.regalpine.ddd.core.aggregate.AggregateRoot;
import io.github.regalpine.ddd.core.identifier.Identifier;

import java.util.List;

/**
 * Unit of Work that tracks aggregate changes within a single transaction boundary.
 * <p>
 * save() ≠ commit(): registering aggregates does not persist them.
 * The commit() method flushes all tracked changes.
 *
 * @author RegalPine
 */
public interface UnitOfWork {

    /**
     * Returns the unique identifier of this Unit of Work.
     */
    String id();

    /**
     * Returns the current status.
     */
    UnitOfWorkStatus status();

    /**
     * Registers an aggregate root for tracking.
     * <p>
     * This does NOT persist the aggregate. Call {@link #commit()} to flush changes.
     *
     * @param aggregate the aggregate root to track
     * @param <A>       the aggregate type
     * @param <I>       the identifier type
     */
    <A extends AggregateRoot<I>, I extends Identifier> void register(A aggregate);

    /**
     * Checks whether the given aggregate is tracked by this Unit of Work.
     *
     * @param aggregate the aggregate to check
     * @return {@code true} if the aggregate is tracked
     */
    boolean contains(Object aggregate);

    /**
     * Returns all tracked aggregates.
     */
    List<AggregateRoot<?>> trackedAggregates();

    /**
     * Commits all tracked changes.
     */
    void commit();

    /**
     * Rolls back all tracked changes.
     */
    void rollback();

    /**
     * Returns whether this Unit of Work is in an active state.
     *
     * @return {@code true} if the status is NEW or ACTIVE
     */
    boolean isActive();
}
