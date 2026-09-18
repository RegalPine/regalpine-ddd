package io.github.regalpine.ddd.runtime.messaging;

import io.github.regalpine.ddd.event.outbox.LeasedOutboxStore;
import io.github.regalpine.ddd.event.outbox.OutboxRecord;
import io.github.regalpine.ddd.messaging.BrokerAdapter;
import io.github.regalpine.ddd.messaging.MessageEnvelope;
import io.github.regalpine.ddd.runtime.DispatchResult;
import io.github.regalpine.ddd.runtime.OutboxDispatcher;
import io.github.regalpine.ddd.transaction.TransactionManager;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 基于租约竞争的 Outbox 调度器实现。
 *
 * <p>从 OutboxStore 领取待发布消息，通过 BrokerAdapter 发布，
 * 成功后确认，失败后指数退避重试。</p>
 */
public final class LeasedOutboxDispatcher implements OutboxDispatcher {
    private final LeasedOutboxStore store;
    private final BrokerAdapter broker;
    private final TransactionManager transactions;
    private final String workerId;
    private final Duration lease;

    public LeasedOutboxDispatcher(LeasedOutboxStore store, BrokerAdapter broker,
                                   TransactionManager transactions, Duration lease) {
        this.store = Objects.requireNonNull(store, "store");
        this.broker = Objects.requireNonNull(broker, "broker");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.workerId = UUID.randomUUID().toString();
        this.lease = Objects.requireNonNull(lease, "lease");
    }

    @Override
    public DispatchResult dispatchBatch(int batchSize) {
        Instant now = Instant.now();
        var claimed = transactions.execute(() -> store.claim(batchSize, workerId, now, lease));
        int dispatched = 0;
        int failed = 0;
        for (var record : claimed) {
            try {
                MessageEnvelope envelope = toEnvelope(record);
                var result = broker.publish(envelope);
                if (result.success()) {
                    transactions.execute(() -> store.acknowledge(record.outboxId(), workerId, Instant.now()));
                    dispatched++;
                } else {
                    String reason = result.failureReason() != null ? result.failureReason() : "unknown";
                    transactions.execute(() -> store.fail(record.outboxId(), workerId,
                            now.plusSeconds(5), reason, 10));
                    failed++;
                }
            } catch (Exception e) {
                transactions.execute(() -> store.fail(record.outboxId(), workerId,
                        now.plusSeconds(5), e.getMessage(), 10));
                failed++;
            }
        }
        return new DispatchResult(dispatched, failed, 0);
    }

    private MessageEnvelope toEnvelope(OutboxRecord record) {
        byte[] payloadBytes = record.payload() != null 
                ? record.payload().getBytes(StandardCharsets.UTF_8) 
                : new byte[0];
        return MessageEnvelope.of(record.eventId(), record.eventType(), payloadBytes);
    }
}
