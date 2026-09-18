package io.github.regalpine.ddd.messaging;

/**
 * Routes messages to their target destinations.
 *
 * <p>Phase IX §22: MessageRouter determines the Route (topic, queue, consumer group)
 * for a given message envelope. Routing can be based on messageType, tenant, source,
 * aggregateType, or headers — but business routing rules must not enter the Domain.</p>
 *
 * @author RegalPine
 */
public interface MessageRouter {

    /**
     * Determines the route for the given message.
     *
     * @param message the message envelope
     * @return the resolved route
     */
    Route route(MessageEnvelope message);
}
