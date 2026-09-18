package io.github.regalpine.ddd.messaging;

/**
 * Represents an integration event that crosses bounded context boundaries.
 *
 * <p>Phase IX §5: Integration Event expresses a business fact that one boundary
 * has occurred and needs to be perceived by other boundaries.
 * It is distinct from Domain Event (MSG-001) and Transport Message.</p>
 *
 * <p>Integration Event must be versionable (§14, MSG-003).</p>
 *
 * @author RegalPine
 */
public interface IntegrationEvent {

    /**
     * Returns the stable event type name (e.g. "order.created").
     *
     * <p>Event type must be stable and not change with internal class names (§13).</p>
     *
     * @return the event type name
     */
    String eventType();

    /**
     * Returns the schema version of this integration event.
     *
     * <p>Version must be an integer that increments on schema changes (§14).</p>
     *
     * @return the schema version, defaults to 1
     */
    default int eventVersion() {
        return 1;
    }
}
