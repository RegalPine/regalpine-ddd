package io.github.regalpine.ddd.messaging.rabbitmq;

/**
 * Builder for RabbitMQ broker adapter configuration.
 *
 * @author RegalPine
 */
public final class RabbitMqBrokerAdapterBuilder {

    private String host = "localhost";
    private int port = 5672;
    private String virtualHost = "/";

    public RabbitMqBrokerAdapterBuilder host(String host) {
        this.host = host;
        return this;
    }

    public RabbitMqBrokerAdapterBuilder port(int port) {
        this.port = port;
        return this;
    }

    public RabbitMqBrokerAdapterBuilder virtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
        return this;
    }

    public RabbitMqBrokerAdapter build() {
        return new RabbitMqBrokerAdapter(host, port, virtualHost);
    }
}
