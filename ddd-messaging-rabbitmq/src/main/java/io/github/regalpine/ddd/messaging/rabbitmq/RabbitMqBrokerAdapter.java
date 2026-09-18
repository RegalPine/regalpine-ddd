package io.github.regalpine.ddd.messaging.rabbitmq;

import com.rabbitmq.client.*;
import io.github.regalpine.ddd.messaging.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** RabbitMQ Broker 适配器；支持 publisher confirms 和 manual ACK。 */
public final class RabbitMqBrokerAdapter implements BrokerAdapter, AutoCloseable {
    private final Connection connection;
    private final Channel publishChannel;
    private final boolean ownsConnection;
    private final Map<String, SubscriptionHandle> subscriptions = new ConcurrentHashMap<>();

    public RabbitMqBrokerAdapter(ConnectionFactory factory) throws Exception {
        this(factory.newConnection(), true);
    }

    public RabbitMqBrokerAdapter(Connection connection) throws Exception {
        this(connection, false);
    }

    private RabbitMqBrokerAdapter(Connection connection, boolean ownsConnection) throws Exception {
        this.connection = Objects.requireNonNull(connection, "connection");
        this.publishChannel = connection.createChannel();
        this.publishChannel.confirmSelect();
        this.ownsConnection = ownsConnection;
    }

    @Override
    public PublishResult publish(MessageEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope");
        try {
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .messageId(envelope.messageId())
                    .type(envelope.messageType())
                    .headers(new HashMap<>(envelope.headers()))
                    .deliveryMode(2) // persistent
                    .build();
            publishChannel.basicPublish("", envelope.messageType(), props, envelope.payload());
            boolean confirmed = publishChannel.waitForConfirms(10_000);
            return confirmed ? PublishResult.success(envelope.messageId())
                    : PublishResult.failure(envelope.messageId(), "Publisher confirm timeout");
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
        try {
            Channel channel = connection.createChannel();
            channel.basicQos(10); // prefetch
            channel.queueDeclare(subscription.destination(), true, false, false, Map.of());
            channel.queueBind(subscription.destination(), subscription.destination(), "");
            String consumerTag = channel.basicConsume(subscription.destination(), false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {
                    MessageEnvelope msgEnvelope = MessageEnvelope.of(
                            properties.getMessageId(), properties.getType(), body);
                    try {
                        consumer.consume(msgEnvelope);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (Exception e) {
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                    }
                }
            });
            SubscriptionHandle handle = new RabbitMqSubscriptionHandle(
                    subscription.subscriptionId(), channel, consumerTag);
            subscriptions.put(subscription.subscriptionId(), handle);
            return handle;
        } catch (Exception e) {
            throw new RuntimeException("订阅失败", e);
        }
    }

    @Override
    public void close() {
        subscriptions.values().forEach(SubscriptionHandle::stop);
        subscriptions.clear();
        try {
            publishChannel.close();
        } catch (Exception e) {
            // ignore
        }
        if (ownsConnection) {
            try {
                connection.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private static final class RabbitMqSubscriptionHandle implements SubscriptionHandle {
        private final String id;
        private final Channel channel;
        private final String consumerTag;
        private final AtomicBoolean stopped = new AtomicBoolean();

        RabbitMqSubscriptionHandle(String id, Channel channel, String consumerTag) {
            this.id = id;
            this.channel = channel;
            this.consumerTag = consumerTag;
        }

        @Override
        public void stop() {
            if (stopped.compareAndSet(false, true)) {
                try {
                    channel.basicCancel(consumerTag);
                    channel.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        @Override
        public String subscriptionId() {
            return id;
        }
    }
}
