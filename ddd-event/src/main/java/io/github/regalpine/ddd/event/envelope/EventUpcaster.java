package io.github.regalpine.ddd.event.envelope;

/**
 * Transforms an {@link EventEnvelope} from an older schema version to a newer one.
 *
 * <p>Event upcasting enables schema evolution: when the shape of an event payload
 * changes, upcasters bridge old serialized forms to the current version without
 * requiring migration of stored events.</p>
 *
 * @author RegalPine
 */
public interface EventUpcaster {

    /**
     * Upcasts the given event envelope to a newer schema version if needed.
     *
     * @param event the event envelope to upcast
     * @return the upcasted event envelope, or the same instance if no upcasting was needed
     */
    EventEnvelope upcast(EventEnvelope event);
}
