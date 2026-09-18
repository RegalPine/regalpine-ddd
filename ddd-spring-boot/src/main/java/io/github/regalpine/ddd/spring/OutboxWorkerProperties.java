package io.github.regalpine.ddd.spring;

import java.time.Duration;

/**
 * Outbox Worker 配置属性。
 *
 * @param lease 租约时长
 * @param pollInterval 轮询间隔
 */
public record OutboxWorkerProperties(
        Duration lease,
        Duration pollInterval
) {
    public OutboxWorkerProperties {
        if (lease == null) lease = Duration.ofMinutes(1);
        if (pollInterval == null) pollInterval = Duration.ofSeconds(5);
    }

    public OutboxWorkerProperties() {
        this(Duration.ofMinutes(1), Duration.ofSeconds(5));
    }
}
