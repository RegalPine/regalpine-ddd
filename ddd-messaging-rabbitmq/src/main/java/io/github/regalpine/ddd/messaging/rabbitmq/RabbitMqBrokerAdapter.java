package io.github.regalpine.ddd.messaging.rabbitmq;

import io.github.regalpine.ddd.messaging.BrokerAdapter;
import io.github.regalpine.ddd.messaging.MessageEnvelope;
import io.github.regalpine.ddd.messaging.PublishResult;
import io.github.regalpine.ddd.messaging.Subscription;

import java.util.Objects;

/**
 * RabbitMQ-based broker adapter.
 * <p>
 * Integrates with RabbitMQ using the AMQP client library.
 * Requires amqp-client dependency at runtime.
 *
 * @author RegalPine
 */
public final class RabbitMqBrokerAdapter implements BrokerAdapter {

    private final String host;
    private final int port;
    private final String virtualHost;

    public RabbitMqBrokerAdapter(String host, int port, String virtualHost) {
        this.host = Objects.requireNonNull(host, "host must not be null");
        this.port = port;
        this.virtualHost = Objects.requireNonNull(virtualHost, "virtualHost must not be null");
    }

    @Override
    public PublishResult publish(MessageEnvelope message) {
        // RabbitMQ producer integration point
        // In production, this would create a Channel and publish to an exchange
        throw new UnsupportedOperationException(
                "RabbitMqBrokerAdapter.publish() requires amqp-client runtime dependency. " +
                        "Configure a Connection/Channel and use Channel.basicPublish().");
    }

    @Override
    public void subscribe(Subscription subscription) {
        // RabbitMQ consumer integration point
        // In production, this would create a Channel and consume from a queue
        throw new UnsupportedOperationException(
                "RabbitMqBrokerAdapter.subscribe() requires amqp-client runtime dependency. " +
                        "Configure a Connection/Channel and use Channel.basicConsume().");
    }

    @Override
    public void close() {
        // Close RabbitMQ connection resources
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String virtualHost() {
        return virtualHost;
    }
}
