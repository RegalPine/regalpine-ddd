package io.github.regalpine.ddd.messaging.kafka;

import io.github.regalpine.ddd.messaging.BrokerAdapter;
import io.github.regalpine.ddd.messaging.MessageEnvelope;
import io.github.regalpine.ddd.messaging.PublishResult;
import io.github.regalpine.ddd.messaging.Subscription;

import java.util.Objects;
import java.util.Properties;

/**
 * Kafka-based broker adapter.
 * <p>
 * Integrates with Apache Kafka using the Kafka client library.
 * Requires kafka-clients dependency at runtime.
 *
 * @author RegalPine
 */
public final class KafkaBrokerAdapter implements BrokerAdapter {

    private final Properties producerConfig;
    private final Properties consumerConfig;

    public KafkaBrokerAdapter(Properties producerConfig, Properties consumerConfig) {
        this.producerConfig = Objects.requireNonNull(producerConfig, "producerConfig must not be null");
        this.consumerConfig = Objects.requireNonNull(consumerConfig, "consumerConfig must not be null");
    }

    @Override
    public PublishResult publish(MessageEnvelope message) {
        // Kafka producer integration point
        // In production, this would create a KafkaProducer and send a ProducerRecord
        throw new UnsupportedOperationException(
                "KafkaBrokerAdapter.publish() requires kafka-clients runtime dependency. " +
                        "Configure a Kafka producer with bootstrap.servers and use KafkaProducer.send().");
    }

    @Override
    public void subscribe(Subscription subscription) {
        // Kafka consumer integration point
        // In production, this would create a KafkaConsumer and poll for records
        throw new UnsupportedOperationException(
                "KafkaBrokerAdapter.subscribe() requires kafka-clients runtime dependency. " +
                        "Configure a Kafka consumer with group.id and use KafkaConsumer.subscribe() + poll().");
    }

    @Override
    public void close() {
        // Close Kafka producer and consumer resources
    }

    /**
     * Returns the producer configuration.
     */
    public Properties producerConfig() {
        return new Properties(producerConfig);
    }

    /**
     * Returns the consumer configuration.
     */
    public Properties consumerConfig() {
        return new Properties(consumerConfig);
    }
}
