package io.github.regalpine.ddd.messaging;

import java.util.Objects;

/**
 * Represents a subscription to a message destination.
 *
 * @author RegalPine
 */
public record Subscription(
        String subscriptionId,
        String destination,
        String consumerGroup
) {

    public Subscription {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        Objects.requireNonNull(destination, "destination must not be null");
    }

    public static Subscription of(String subscriptionId, String destination, String consumerGroup) {
        return new Subscription(subscriptionId, destination, consumerGroup);
    }
}
