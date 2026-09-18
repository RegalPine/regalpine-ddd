package io.github.regalpine.ddd.messaging.routing;

import io.github.regalpine.ddd.messaging.MessageEnvelope;

/**
 * Provides the ordering key for a message.
 *
 * <p>Phase IX §42: Ordering key enables per-aggregate message ordering.
 * Recommended format: {@code aggregateType + ":" + aggregateId} (e.g. "Order:100").</p>
 *
 * <p>Phase IX §41/MSG-013/MSG-014: Framework defaults to no global ordering.
 * Only explicitly declared ordering keys are guaranteed.</p>
 *
 * @author RegalPine
 */
public interface OrderingKeyProvider {

    /**
     * Computes the ordering key for the given message.
     *
     * @param message the message envelope
     * @return the ordering key, or null if no ordering is required
     */
    String orderingKey(MessageEnvelope message);
}
