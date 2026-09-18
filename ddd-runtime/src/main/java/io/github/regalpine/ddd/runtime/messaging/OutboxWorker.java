package io.github.regalpine.ddd.runtime.messaging;

import io.github.regalpine.ddd.event.outbox.*;
import io.github.regalpine.ddd.transaction.TransactionManager;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/** 可恢复的 Outbox 发布 worker；支持租约竞争、指数退避和死信处理。 */
public final class OutboxWorker implements Runnable, AutoCloseable {
    private final LeasedOutboxStore store;
    private final Function<OutboxRecord, Boolean> publisher;
    private final TransactionManager transactions;
    private final Duration lease;
    private final Duration publishTimeout;
    private final int maxAttempts;
    private final Duration initialBackoff;
    private final Duration maxBackoff;
    private final AtomicBoolean running = new AtomicBoolean();
    private final String workerId = UUID.randomUUID().toString();

    public OutboxWorker(LeasedOutboxStore store, Function<OutboxRecord, Boolean> publisher,
                        TransactionManager transactions, Duration lease, Duration publishTimeout,
                        int maxAttempts, Duration initialBackoff, Duration maxBackoff) {
        this.store = Objects.requireNonNull(store, "store");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.lease = Objects.requireNonNull(lease, "lease");
        this.publishTimeout = Objects.requireNonNull(publishTimeout, "publishTimeout");
        if (maxAttempts <= 0) throw new IllegalArgumentException("maxAttempts 必须为正数");
        this.maxAttempts = maxAttempts;
        this.initialBackoff = Objects.requireNonNull(initialBackoff, "initialBackoff");
        this.maxBackoff = Objects.requireNonNull(maxBackoff, "maxBackoff");
        if (lease.minus(publishTimeout).isNegative() || lease.minus(publishTimeout).isZero()) {
            throw new IllegalArgumentException("租约必须大于发布超时");
        }
    }

    @Override
    public void run() {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Worker 已在运行");
        }
        try {
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                processBatch();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            running.set(false);
        }
    }

    private void processBatch() {
        Instant now = Instant.now();
        var claimed = transactions.execute(() -> store.claim(10, workerId, now, lease));
        for (var record : claimed) {
            try {
                boolean success = publisher.apply(record);
                if (success) {
                    transactions.execute(() -> store.acknowledge(record.outboxId(), workerId, Instant.now()));
                } else {
                    handleFailure(record, "发布返回 false");
                }
            } catch (Exception e) {
                handleFailure(record, e.getMessage());
            }
        }
    }

    private void handleFailure(OutboxRecord record, String error) {
        Instant next = computeBackoff(record.attempts());
        transactions.execute(() -> store.fail(record.outboxId(), workerId, next, error, maxAttempts));
    }

    private Instant computeBackoff(int attempts) {
        long backoffMs = initialBackoff.toMillis() * (1L << Math.min(attempts, 20));
        return Instant.now().plusMillis(Math.min(backoffMs, maxBackoff.toMillis()));
    }

    @Override
    public void close() {
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    public String workerId() {
        return workerId;
    }
}
