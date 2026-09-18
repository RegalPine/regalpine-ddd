package io.github.regalpine.ddd.messaging;

/**
 * 消息 Broker 适配器接口；支持发布和订阅。
 *
 * @author RegalPine
 */
public interface BrokerAdapter extends AutoCloseable {
    /** 发布消息信封。 */
    PublishResult publish(MessageEnvelope envelope);

    /** 订阅消息并返回可关闭的句柄。 */
    SubscriptionHandle subscribe(Subscription subscription, MessageConsumer consumer);

    /** 关闭适配器并释放资源。 */
    @Override
    void close();
}
