package io.github.regalpine.ddd.runtime.messaging;

import io.github.regalpine.ddd.event.outbox.LeasedOutboxStore;
import io.github.regalpine.ddd.messaging.BrokerAdapter;
import io.github.regalpine.ddd.runtime.RuntimeComponent;
import io.github.regalpine.ddd.runtime.RuntimeComponentDescriptor;
import io.github.regalpine.ddd.runtime.RuntimeContext;
import io.github.regalpine.ddd.transaction.TransactionManager;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Outbox Worker 运行时组件。
 *
 * <p>封装 OutboxWorker 的生命周期，使其可被 DefaultDddRuntime 管理。</p>
 */
public final class OutboxWorkerComponent implements RuntimeComponent, RuntimeComponentDescriptor {
    private final LeasedOutboxStore store;
    private final BrokerAdapter broker;
    private final TransactionManager transactions;
    private final Duration lease;
    private final Duration pollInterval;
    private ScheduledExecutorService executor;
    private OutboxWorker worker;

    public OutboxWorkerComponent(LeasedOutboxStore store, BrokerAdapter broker,
                                  TransactionManager transactions, Duration lease, Duration pollInterval) {
        this.store = Objects.requireNonNull(store, "store");
        this.broker = Objects.requireNonNull(broker, "broker");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.lease = Objects.requireNonNull(lease, "lease");
        this.pollInterval = Objects.requireNonNull(pollInterval, "pollInterval");
    }

    @Override
    public String name() {
        return "outbox-worker";
    }

    @Override
    public Set<String> dependencies() {
        return Set.of("transaction-manager", "broker-adapter");
    }

    @Override
    public void start(RuntimeContext context) {
        LeasedOutboxDispatcher dispatcher = new LeasedOutboxDispatcher(store, broker, transactions, lease);
        this.worker = new OutboxWorker(
                store,
                record -> dispatcher.dispatchBatch(1).dispatched() > 0,
                transactions,
                lease,
                Duration.ofSeconds(30),
                10,
                Duration.ofSeconds(1),
                Duration.ofMinutes(5)
        );
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "outbox-worker");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleWithFixedDelay(worker, 0, pollInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop(RuntimeContext context) {
        if (worker != null) {
            worker.close();
        }
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
