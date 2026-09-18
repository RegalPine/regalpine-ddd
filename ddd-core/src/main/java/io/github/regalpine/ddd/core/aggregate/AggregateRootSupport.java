package io.github.regalpine.ddd.core.aggregate;

import io.github.regalpine.ddd.core.event.DomainEvent;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for aggregate root implementations.
 *
 * <p>Provides default implementations for event collection and the
 * {@link #raise(DomainEvent)} method for recording domain events
 * during aggregate behavior execution.</p>
 *
 * <p>Per Phase II §49, the event list is managed internally by this
 * base class. Subclasses do not need to implement any event-related
 * methods — {@link #domainEvents()} and {@link #clearDomainEvents()}
 * are {@code final}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public final class Order extends AggregateRootSupport<OrderId> {
 *
 *     private final OrderId orderId;
 *     private OrderStatus status;
 *     private Version version;
 *
 *     public void pay(PaymentId paymentId) {
 *         // business rules...
 *         raise(new OrderPaid(eventId, aggregateType(), id(), version, now()));
 *     }
 * }
 * }</pre>
 *
 * @param <I> the identifier type
 */
public abstract class AggregateRootSupport<I extends Identifier>
        implements AggregateRoot<I> {

    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * Records a domain event as a pending event of this aggregate.
     *
     * <p>The event will be included in {@link #domainEvents()} until
     * {@link #clearDomainEvents()} is called.</p>
     *
     * @param event the domain event to record, must not be {@code null}
     * @throws NullPointerException if event is {@code null}
     */
    protected final void raise(DomainEvent event) {
        events.add(Objects.requireNonNull(event, "Domain event must not be null"));
    }

    /**
     * Returns an unmodifiable snapshot of the pending domain events.
     *
     * @return an unmodifiable list of pending events, never {@code null}
     */
    @Override
    public final List<DomainEvent> domainEvents() {
        return List.copyOf(events);
    }

    /**
     * Clears all pending domain events.
     *
     * <p>This is typically called by the repository or unit of work
     * after events have been successfully collected for dispatch.</p>
     */
    @Override
    public final void clearDomainEvents() {
        events.clear();
    }

    /**
     * Two aggregate roots are equal if and only if they have the same identity.
     *
     * <p>This follows the DDD Entity identity semantics: equality is based
     * solely on the identifier, not on the current state or version.</p>
     *
     * @param o the other object
     * @return {@code true} if the other object is the same aggregate identity
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregateRootSupport<?> that = (AggregateRootSupport<?>) o;
        return Objects.equals(id(), that.id());
    }

    /**
     * Hash code is based solely on the aggregate identity.
     *
     * @return the hash code of the identifier
     */
    @Override
    public final int hashCode() {
        return Objects.hashCode(id());
    }
}
