package io.github.regalpine.ddd.messaging.kafka;

import java.util.Properties;

/**
 * Builder for Kafka broker adapter configuration.
 *
 * @author RegalPine
 */
public final class KafkaBrokerAdapterBuilder {

    private final Properties producerConfig = new Properties();
    private final Properties consumerConfig = new Properties();

    public KafkaBrokerAdapterBuilder bootstrapServers(String servers) {
        producerConfig.setProperty("bootstrap.servers", servers);
        consumerConfig.setProperty("bootstrap.servers", servers);
        return this;
    }

    public KafkaBrokerAdapterBuilder groupId(String groupId) {
        consumerConfig.setProperty("group.id", groupId);
        return this;
    }

    public KafkaBrokerAdapterBuilder producerProperty(String key, String value) {
        producerConfig.setProperty(key, value);
        return this;
    }

    public KafkaBrokerAdapterBuilder consumerProperty(String key, String value) {
        consumerConfig.setProperty(key, value);
        return this;
    }

    public KafkaBrokerAdapter build() {
        return new KafkaBrokerAdapter(producerConfig, consumerConfig);
    }
}
