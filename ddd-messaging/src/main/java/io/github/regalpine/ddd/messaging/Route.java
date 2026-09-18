package io.github.regalpine.ddd.messaging;

import java.util.Objects;

/**
 * A route defining the target topic, queue, and consumer group for a message.
 *
 * <p>Phase IX §22: Route specifies the messaging destination topology.</p>
 *
 * @param topic         the target topic name (§19)
 * @param queue         the target queue name (§20)
 * @param consumerGroup the consumer group name (§21)
 * @author RegalPine
 */
public record Route(String topic, String queue, String consumerGroup) {

    public Route {
        Objects.requireNonNull(topic, "topic must not be null");
    }

    /**
     * Creates a route with only a topic.
     */
    public static Route ofTopic(String topic) {
        return new Route(topic, null, null);
    }

    /**
     * Creates a route with topic and queue.
     */
    public static Route of(String topic, String queue) {
        return new Route(topic, queue, null);
    }

    /**
     * Creates a route with topic, queue, and consumer group.
     */
    public static Route of(String topic, String queue, String consumerGroup) {
        return new Route(topic, queue, consumerGroup);
    }
}
