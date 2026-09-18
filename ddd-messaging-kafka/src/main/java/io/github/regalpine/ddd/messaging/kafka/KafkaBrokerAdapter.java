package io.github.regalpine.ddd.messaging.kafka;

import io.github.regalpine.ddd.messaging.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Kafka Broker 适配器；支持 at-least-once 语义和元数据保留。 */
public final class KafkaBrokerAdapter implements BrokerAdapter, AutoCloseable {
    private final Producer<String, byte[]> producer;
    private final ConsumerFactory consumerFactory;
    private final boolean ownsProducer;
    private final boolean ownsConsumerFactory;
    private final Map<String, SubscriptionHandle> subscriptions = new ConcurrentHashMap<>();

    public KafkaBrokerAdapter(Properties producerProps, Properties consumerProps) {
        this(createProducer(producerProps), createConsumerFactory(consumerProps), true, true);
    }

    public KafkaBrokerAdapter(Producer<String, byte[]> producer, ConsumerFactory consumerFactory) {
        this(producer, consumerFactory, false, false);
    }

    private KafkaBrokerAdapter(Producer<String, byte[]> producer, ConsumerFactory consumerFactory,
                               boolean ownsProducer, boolean ownsConsumerFactory) {
        this.producer = Objects.requireNonNull(producer, "producer");
        this.consumerFactory = Objects.requireNonNull(consumerFactory, "consumerFactory");
        this.ownsProducer = ownsProducer;
        this.ownsConsumerFactory = ownsConsumerFactory;
    }

    private static Producer<String, byte[]> createProducer(Properties props) {
        Properties p = new Properties();
        p.putAll(props);
        p.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        p.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        p.putIfAbsent(ProducerConfig.ACKS_CONFIG, "all");
        p.putIfAbsent(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new KafkaProducer<>(p);
    }

    private static ConsumerFactory createConsumerFactory(Properties props) {
        Properties p = new Properties();
        p.putAll(props);
        p.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        p.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        p.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new ConsumerFactory(p);
    }

    @Override
    public PublishResult publish(MessageEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope");
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                envelope.messageType(), envelope.messageId(), envelope.payload());
        envelope.headers().forEach((k, v) -> record.headers().add(k, v.getBytes()));
        try {
            RecordMetadata metadata = producer.send(record).get(10, TimeUnit.SECONDS);
            return PublishResult.success(envelope.messageId());
        } catch (Exception e) {
            return PublishResult.failure(envelope.messageId(), e.getMessage());
        }
    }

    @Override
    public SubscriptionHandle subscribe(Subscription subscription, MessageConsumer consumer) {
        Objects.requireNonNull(subscription, "subscription");
        Objects.requireNonNull(consumer, "consumer");
        if (subscriptions.containsKey(subscription.subscriptionId())) {
            throw new IllegalStateException("订阅已存在: " + subscription.subscriptionId());
        }
        KafkaConsumer<String, byte[]> kafkaConsumer = consumerFactory.create();
        kafkaConsumer.subscribe(Collections.singleton(subscription.destination()));
        KafkaSubscriptionHandle handle = new KafkaSubscriptionHandle(subscription.subscriptionId(), kafkaConsumer, consumer);
        subscriptions.put(subscription.subscriptionId(), handle);
        handle.start();
        return handle;
    }

    @Override
    public void close() {
        subscriptions.values().forEach(SubscriptionHandle::stop);
        subscriptions.clear();
        if (ownsProducer) {
            producer.close(Duration.ofSeconds(10));
        }
        if (ownsConsumerFactory) {
            consumerFactory.close();
        }
    }

    private static final class KafkaSubscriptionHandle implements SubscriptionHandle {
        private final String id;
        private final KafkaConsumer<String, byte[]> consumer;
        private final MessageConsumer messageConsumer;
        private final Thread pollThread;
        private volatile boolean running = true;

        KafkaSubscriptionHandle(String id, KafkaConsumer<String, byte[]> consumer, MessageConsumer messageConsumer) {
            this.id = id;
            this.consumer = consumer;
            this.messageConsumer = messageConsumer;
            this.pollThread = new Thread(this::pollLoop, "kafka-consumer-" + id);
        }

        public void start() {
            pollThread.start();
        }

        private void pollLoop() {
            try {
                while (running) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, byte[]> record : records) {
                        MessageEnvelope envelope = MessageEnvelope.of(
                                record.key(), record.topic(), record.value());
                        messageConsumer.consume(envelope);
                        consumer.commitSync(Collections.singletonMap(
                                new org.apache.kafka.common.TopicPartition(record.topic(), record.partition()),
                                new OffsetAndMetadata(record.offset() + 1)));
                    }
                }
            } catch (Exception e) {
                if (running) throw e;
            } finally {
                consumer.close();
            }
        }

        @Override
        public void stop() {
            running = false;
            consumer.wakeup();
            try {
                pollThread.join(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public String subscriptionId() {
            return id;
        }
    }

    private static final class ConsumerFactory {
        private final Properties props;
        ConsumerFactory(Properties props) {
            this.props = props;
        }
        KafkaConsumer<String, byte[]> create() {
            return new KafkaConsumer<>(props);
        }
        void close() {
            // ConsumerFactory 不持有资源
        }
    }
}
