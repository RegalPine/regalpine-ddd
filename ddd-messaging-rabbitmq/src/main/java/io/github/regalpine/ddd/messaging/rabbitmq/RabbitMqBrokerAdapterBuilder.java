package io.github.regalpine.ddd.messaging.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;

/** RabbitMQ 适配器构建器。 */
public final class RabbitMqBrokerAdapterBuilder {
    private String host = "localhost";
    private int port = 5672;
    private String virtualHost = "/";
    private String username = "guest";
    private String password = "guest";

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

    public RabbitMqBrokerAdapterBuilder username(String username) {
        this.username = username;
        return this;
    }

    public RabbitMqBrokerAdapterBuilder password(String password) {
        this.password = password;
        return this;
    }

    public RabbitMqBrokerAdapter build() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setVirtualHost(virtualHost);
        factory.setUsername(username);
        factory.setPassword(password);
        return new RabbitMqBrokerAdapter(factory);
    }
}
