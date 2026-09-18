package io.github.regalpine.ddd.test;

import io.github.regalpine.ddd.core.aggregate.AggregateRoot;
import io.github.regalpine.ddd.core.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A Given/When/Then test fixture for aggregate root testing.
 * <p>
 * Provides a fluent API for testing aggregate behavior:
 * <pre>{@code
 * AggregateTestFixture.given(order)
 *     .when(agg -> agg.confirm())
 *     .thenEvents(OrderConfirmedEvent.class)
 *     .verify();
 * }</pre>
 *
 * @param <A> the aggregate root type
 * @author RegalPine
 */
public final class AggregateTestFixture<A extends AggregateRoot<?>> {

    private final A aggregate;
    private final List<DomainEvent> expectedEvents = new ArrayList<>();
    private List<DomainEvent> actualEvents;

    private AggregateTestFixture(A aggregate) {
        this.aggregate = Objects.requireNonNull(aggregate, "aggregate must not be null");
        aggregate.clearDomainEvents();
    }

    /**
     * Creates a test fixture for the given aggregate.
     */
    public static <A extends AggregateRoot<?>> AggregateTestFixture<A> given(A aggregate) {
        return new AggregateTestFixture<>(aggregate);
    }

    /**
     * Applies an action to the aggregate (the "when" step).
     *
     * @param action the action to apply
     * @return this fixture for chaining
     */
    public AggregateTestFixture<A> when(Function<A, ?> action) {
        Objects.requireNonNull(action, "action must not be null");
        action.apply(aggregate);
        actualEvents = new ArrayList<>(aggregate.domainEvents());
        return this;
    }

    /**
     * Applies a void action to the aggregate (the "when" step).
     *
     * @param action the action to apply
     * @return this fixture for chaining
     */
    public AggregateTestFixture<A> whenVoid(java.util.function.Consumer<A> action) {
        Objects.requireNonNull(action, "action must not be null");
        action.accept(aggregate);
        actualEvents = new ArrayList<>(aggregate.domainEvents());
        return this;
    }

    /**
     * Asserts that the aggregate produced events of the specified types.
     *
     * @param eventTypes the expected event types
     * @return this fixture for chaining
     */
    public AggregateTestFixture<A> thenEvents(Class<?>... eventTypes) {
        if (actualEvents == null) {
            throw new IllegalStateException("Must call when() before thenEvents()");
        }
        if (actualEvents.size() != eventTypes.length) {
            throw new AssertionError(
                    "Expected %d events but got %d".formatted(eventTypes.length, actualEvents.size()));
        }
        for (int i = 0; i < eventTypes.length; i++) {
            if (!eventTypes[i].isInstance(actualEvents.get(i))) {
                throw new AssertionError(
                        "Expected event[%d] to be %s but was %s".formatted(
                                i, eventTypes[i].getCanonicalName(),
                                actualEvents.get(i).getClass().getCanonicalName()));
            }
        }
        return this;
    }

    /**
     * Asserts that no events were produced.
     *
     * @return this fixture for chaining
     */
    public AggregateTestFixture<A> thenNoEvents() {
        if (actualEvents == null) {
            throw new IllegalStateException("Must call when() before thenEvents()");
        }
        if (!actualEvents.isEmpty()) {
            throw new AssertionError("Expected no events but got %d".formatted(actualEvents.size()));
        }
        return this;
    }

    /**
     * Returns the aggregate for further assertions.
     */
    public A aggregate() {
        return aggregate;
    }

    /**
     * Returns the actual events produced by the when step.
     */
    public List<DomainEvent> producedEvents() {
        return actualEvents != null ? Collections.unmodifiableList(actualEvents) : List.of();
    }

    /**
     * Completes the verification (terminal operation).
     */
    public void verify() {
        // No-op: assertions already perform in thenEvents/thenNoEvents
    }
}
