package io.github.regalpine.ddd.messaging;

/**
 * Port for adapting to specific messaging infrastructure (Kafka, RabbitMQ, etc.).
 *
 * <p>Phase IX §24: BrokerAdapter provides the publish/subscribe contract
 * that concrete infrastructure adapters must implement.
 * Adapter implementations must reside in infrastructure modules (§96).</p>
 *
 * @author RegalPine
 */
public interface BrokerAdapter {

    /**
     * Publishes a message envelope to the broker.
     *
     * @param message the message envelope to publish
     * @return the publish result
     */
    PublishResult publish(MessageEnvelope message);

    /**
     * Subscribes to messages using the given subscription configuration.
     *
     * @param subscription the subscription to establish
     */
    void subscribe(Subscription subscription);

    /**
     * Closes the broker adapter and releases resources.
     */
    void close();
}
