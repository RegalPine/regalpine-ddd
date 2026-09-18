package io.github.regalpine.ddd.integration;

import io.github.regalpine.ddd.event.outbox.LeasedOutboxStore;
import io.github.regalpine.ddd.event.outbox.OutboxRecord;
import io.github.regalpine.ddd.infrastructure.jdbc.ConnectionProvider;
import io.github.regalpine.ddd.infrastructure.jdbc.JdbcOutboxStore;
import io.github.regalpine.ddd.infrastructure.jdbc.JdbcTransactionAdapter;
import io.github.regalpine.ddd.transaction.TransactionAdapter;
import io.github.regalpine.ddd.transaction.TransactionManager;
import io.github.regalpine.ddd.transaction.DefaultTransactionManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.postgresql.ds.PGSimpleDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Outbox 租约机制集成测试。
 *
 * <p>验证 PostgreSQL 环境下的 Outbox 租约竞争、指数退避和死信处理。</p>
 */
@Testcontainers
@Tag("integration")
@DisplayName("Outbox 租约机制集成测试")
class OutboxLeaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("ddd_test")
            .withUsername("test")
            .withPassword("test");

    private ConnectionProvider connectionProvider;
    private LeasedOutboxStore store;
    private TransactionManager transactions;

    @BeforeEach
    void setUp() throws Exception {
        PGSimpleDataSource pgDataSource = new PGSimpleDataSource();
        pgDataSource.setUrl(postgres.getJdbcUrl());
        pgDataSource.setUser(postgres.getUsername());
        pgDataSource.setPassword(postgres.getPassword());
        
        this.connectionProvider = () -> pgDataSource.getConnection();

        // 初始化表结构
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
        this.store = new JdbcOutboxStore((io.github.regalpine.ddd.infrastructure.jdbc.JdbcTransactionAdapter) adapter);
    }

    @Test
    @DisplayName("应能成功插入和领取 Outbox 记录")
    void shouldClaimPendingRecords() {
        // 插入待发布记录
        OutboxRecord record = OutboxRecord.create(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "OrderCreated",
                1,
                "Order",
                "order-123",
                "{\"orderId\":\"123\"}",
                Instant.now()
        );

        transactions.execute(() -> store.append(record));

        // 领取记录
        String workerId = "worker-1";
        Duration lease = Duration.ofMinutes(1);
        var claimed = transactions.execute(() -> store.claim(10, workerId, Instant.now(), lease));

        assertEquals(1, claimed.size());
        assertEquals(record.outboxId(), claimed.get(0).outboxId());
        assertEquals("IN_FLIGHT", claimed.get(0).status());
        assertEquals(workerId, claimed.get(0).leaseToken());
    }

    @Test
    @DisplayName("应能确认已发布记录")
    void shouldAcknowledgePublishedRecord() {
        OutboxRecord record = OutboxRecord.create(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "OrderPaid",
                1,
                "Order",
                "order-456",
                "{\"orderId\":\"456\"}",
                Instant.now()
        );

        transactions.execute(() -> store.append(record));

        String workerId = "worker-2";
        Duration lease = Duration.ofMinutes(1);
        var claimed = transactions.execute(() -> store.claim(10, workerId, Instant.now(), lease));

        // 确认发布
        transactions.execute(() -> store.acknowledge(claimed.get(0).outboxId(), workerId, Instant.now()));

        // 验证状态
        var acknowledged = transactions.execute(() -> store.claim(10, "worker-3", Instant.now(), lease));
        assertTrue(acknowledged.isEmpty());
    }

    @Test
    @DisplayName("应能处理失败记录并计算退避")
    void shouldHandleFailureWithBackoff() {
        OutboxRecord record = OutboxRecord.create(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "OrderShipped",
                1,
                "Order",
                "order-789",
                "{\"orderId\":\"789\"}",
                Instant.now()
        );

        transactions.execute(() -> store.append(record));

        String workerId = "worker-4";
        Duration lease = Duration.ofMinutes(1);
        var claimed = transactions.execute(() -> store.claim(10, workerId, Instant.now(), lease));

        // 标记失败
        Instant nextAttempt = Instant.now().plusSeconds(5);
        transactions.execute(() -> store.fail(claimed.get(0).outboxId(), workerId, nextAttempt, "Broker unavailable", 10));

        // 立即领取应返回空（未到下次尝试时间）
        var immediateClaim = transactions.execute(() -> store.claim(10, "worker-5", Instant.now(), lease));
        assertTrue(immediateClaim.isEmpty());

        // 等待后领取
        var laterClaim = transactions.execute(() -> store.claim(10, "worker-5", Instant.now().plusSeconds(10), lease));
        assertEquals(1, laterClaim.size());
    }

    @Test
    @DisplayName("应支持租约竞争（FOR UPDATE SKIP LOCKED）")
    void shouldSupportLeaseContention() throws Exception {
        OutboxRecord record = OutboxRecord.create(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "OrderCancelled",
                1,
                "Order",
                "order-999",
                "{\"orderId\":\"999\"}",
                Instant.now()
        );

        transactions.execute(() -> store.append(record));

        // 模拟两个 worker 同时领取
        String worker1 = "worker-A";
        String worker2 = "worker-B";
        Duration lease = Duration.ofMinutes(1);

        var claimed1 = transactions.execute(() -> store.claim(10, worker1, Instant.now(), lease));
        var claimed2 = transactions.execute(() -> store.claim(10, worker2, Instant.now(), lease));

        // 只有一个 worker 能成功领取
        assertEquals(1, claimed1.size() + claimed2.size());
    }
}
