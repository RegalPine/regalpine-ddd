package io.github.regalpine.ddd.integration;

import io.github.regalpine.ddd.event.outbox.LeasedOutboxStore;
import io.github.regalpine.ddd.event.outbox.OutboxRecord;
import io.github.regalpine.ddd.infrastructure.jdbc.ConnectionProvider;
import io.github.regalpine.ddd.infrastructure.jdbc.JdbcOutboxStore;
import io.github.regalpine.ddd.infrastructure.jdbc.JdbcTransactionAdapter;
import io.github.regalpine.ddd.messaging.*;
import io.github.regalpine.ddd.runtime.messaging.LeasedOutboxDispatcher;
import io.github.regalpine.ddd.transaction.DefaultTransactionManager;
import io.github.regalpine.ddd.transaction.TransactionAdapter;
import io.github.regalpine.ddd.transaction.TransactionManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.postgresql.ds.PGSimpleDataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Outbox Worker 端到端集成测试。
 *
 * <p>验证 OutboxWorker + LeasedOutboxDispatcher + BrokerAdapter 的完整流程。</p>
 */
@Testcontainers
@Tag("integration")
@DisplayName("Outbox Worker 端到端集成测试")
class OutboxWorkerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("ddd_test")
            .withUsername("test")
            .withPassword("test");

    private ConnectionProvider connectionProvider;
    private LeasedOutboxStore store;
    private TransactionManager transactions;
    private InMemoryBrokerAdapter broker;

    @BeforeEach
    void setUp() throws Exception {
        PGSimpleDataSource pgDataSource = new PGSimpleDataSource();
        pgDataSource.setUrl(postgres.getJdbcUrl());
        pgDataSource.setUser(postgres.getUsername());
        pgDataSource.setPassword(postgres.getPassword());
        
        this.connectionProvider = () -> pgDataSource.getConnection();

        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS outbox (
                    outbox_id TEXT PRIMARY KEY,
                    event_id TEXT NOT NULL,
                    event_type TEXT NOT NULL,
                    event_version INT NOT NULL DEFAULT 1,
                    aggregate_type TEXT NOT NULL,
                    aggregate_id TEXT NOT NULL,
                    tenant_id TEXT,
                    payload TEXT NOT NULL,
                    occurred_at TIMESTAMPTZ NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    attempts INT NOT NULL DEFAULT 0,
                    published_at TIMESTAMPTZ,
                    destination TEXT,
                    next_attempt_at TIMESTAMPTZ,
                    lease_token TEXT,
                    lease_until TIMESTAMPTZ,
                    last_error TEXT
                )
                """);
        }

        TransactionAdapter adapter = new JdbcTransactionAdapter(connectionProvider);
        this.transactions = new DefaultTransactionManager(adapter);
        this.store = new JdbcOutboxStore((JdbcTransactionAdapter) adapter);
        this.broker = new InMemoryBrokerAdapter();
    }

    @Test
    @DisplayName("应能完整发布 Outbox 记录到 Broker")
    void shouldDispatchOutboxRecordsToBroker() {
        // 插入 3 条记录
        for (int i = 0; i < 3; i++) {
            OutboxRecord record = OutboxRecord.create(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    "OrderEvent",
                    1,
                    "Order",
                    "order-" + i,
                    "{\"index\":" + i + "}",
                    Instant.now()
            );
            transactions.execute(() -> store.append(record));
        }

        // 创建调度器并发布
        LeasedOutboxDispatcher dispatcher = new LeasedOutboxDispatcher(
                store, broker, transactions, Duration.ofMinutes(1));

        var result = dispatcher.dispatchBatch(10);

        assertEquals(3, result.dispatched());
        assertEquals(0, result.failed());
        assertEquals(3, broker.publishedMessages().size());
    }

    @Test
    @DisplayName("应在 Broker 失败时保留记录并重试")
    void shouldRetryOnBrokerFailure() {
        OutboxRecord record = OutboxRecord.create(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "OrderFailed",
                1,
                "Order",
                "order-fail",
                "{\"shouldFail\":true}",
                Instant.now()
        );

        transactions.execute(() -> store.append(record));

        // 让 Broker 失败
        broker.setFailNext(true);

        LeasedOutboxDispatcher dispatcher = new LeasedOutboxDispatcher(
                store, broker, transactions, Duration.ofMinutes(1));

        var result = dispatcher.dispatchBatch(10);

        assertEquals(0, result.dispatched());
        assertEquals(1, result.failed());

        // 恢复 Broker
        broker.setFailNext(false);

        // 重试应成功
        var retryResult = dispatcher.dispatchBatch(10);
        assertEquals(1, retryResult.dispatched());
    }

    /**
     * 内存 Broker 适配器，用于测试。
     */
    private static class InMemoryBrokerAdapter implements BrokerAdapter {
        private final List<MessageEnvelope> published = new ArrayList<>();
        private final AtomicBoolean failNext = new AtomicBoolean();

        @Override
        public PublishResult publish(MessageEnvelope envelope) {
            if (failNext.compareAndSet(true, false)) {
                return PublishResult.failure(envelope.messageId(), "Simulated broker failure");
            }
            published.add(envelope);
            return PublishResult.success(envelope.messageId());
        }

        @Override
        public SubscriptionHandle subscribe(Subscription subscription, MessageConsumer consumer) {
            throw new UnsupportedOperationException("Not implemented for test");
        }

        @Override
        public void close() {
            // No-op
        }

        public List<MessageEnvelope> publishedMessages() {
            return List.copyOf(published);
        }

        public void setFailNext(boolean fail) {
            failNext.set(fail);
        }
    }
}
