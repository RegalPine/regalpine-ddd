package io.github.regalpine.ddd.messaging;

/**
 * 可关闭的订阅句柄；应用层负责在不再需要时停止订阅。
 *
 * @author RegalPine
 */
public interface SubscriptionHandle extends AutoCloseable {
    /** 停止订阅并释放资源。 */
    void stop();

    /** 返回订阅标识。 */
    String subscriptionId();

    @Override
    default void close() {
        stop();
    }
}
