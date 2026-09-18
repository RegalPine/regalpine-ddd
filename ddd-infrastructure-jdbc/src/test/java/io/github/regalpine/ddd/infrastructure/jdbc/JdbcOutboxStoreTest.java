package io.github.regalpine.ddd.infrastructure.jdbc;

import io.github.regalpine.ddd.event.outbox.OutboxRecord;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JdbcOutboxStore")
class JdbcOutboxStoreTest {

    private JdbcTransactionAdapter adapter;
    private JdbcOutboxStore store;

    @BeforeEach
    void setUp() throws Exception {
        ConnectionProvider provider = () -> DriverManager.getConnection(
                "jdbc:h2:mem:testOutbox;DB_CLOSE_DELAY=-1", "sa", "");

        try (Connection conn = provider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS outbox (
                        outbox_id VARCHAR(128) NOT NULL,
                        event_id VARCHAR(128) NOT NULL UNIQUE,
                        event_type VARCHAR(255) NOT NULL,
                        event_version INTEGER NOT NULL,
                        aggregate_type VARCHAR(255) NOT NULL,
                        aggregate_id VARCHAR(255) NOT NULL,
                        tenant_id VARCHAR(255),
                        payload TEXT NOT NULL,
                        occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        status VARCHAR(32) NOT NULL,
                        attempts INTEGER NOT NULL DEFAULT 0,
                        published_at TIMESTAMP WITH TIME ZONE,
                        destination VARCHAR(512) NOT NULL,
                        next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        lease_token VARCHAR(128),
                        lease_until TIMESTAMP WITH TIME ZONE,
                        last_error TEXT,
                        PRIMARY KEY (outbox_id)
                    )
                    """);
        }

        adapter = new JdbcTransactionAdapter(provider);
        store = new JdbcOutboxStore(adapter);
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection conn = adapter.currentConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        } catch (Exception e) {
            // ignore
        }
        if (adapter.isActive()) {
            adapter.rollback();
            adapter.close();
        }
    }

    private OutboxRecord createRecord() {
        return OutboxRecord.create(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "TestEvent",
                1,
                "TestAggregate",
                "test-id-1",
                "{\"test\": true}",
                Instant.now()
        );
    }

    @Test
    @DisplayName("append and loadPending round-trip")
    void appendAndLoadPending() {
        adapter.begin(io.github.regalpine.ddd.transaction.TransactionDefinition.DEFAULT);
        OutboxRecord record = createRecord();
        store.append(record);
        adapter.commit();
        adapter.close();

        adapter.begin(io.github.regalpine.ddd.transaction.TransactionDefinition.DEFAULT);
        List<OutboxRecord> pending = store.loadPending(10);
        assertEquals(1, pending.size());
        assertEquals(record.outboxId(), pending.get(0).outboxId());
        assertEquals("PENDING", pending.get(0).status());
        adapter.commit();
        adapter.close();
    }

    @Test
    @DisplayName("claim and acknowledge")
    @Disabled("需要 PostgreSQL 支持 FOR UPDATE SKIP LOCKED 和 RETURNING")
    void claimAndAcknowledge() {
        adapter.begin(io.github.regalpine.ddd.transaction.TransactionDefinition.DEFAULT);
        OutboxRecord record = createRecord();
        store.append(record);
        adapter.commit();
        adapter.close();

        adapter.begin(io.github.regalpine.ddd.transaction.TransactionDefinition.DEFAULT);
        var claimed = store.claim(10, "worker-1", Instant.now(), Duration.ofSeconds(60));
        assertEquals(1, claimed.size());
        adapter.commit();
        adapter.close();

        adapter.begin(io.github.regalpine.ddd.transaction.TransactionDefinition.DEFAULT);
        assertTrue(store.acknowledge(claimed.get(0).outboxId(), "worker-1", Instant.now()));
        adapter.commit();
        adapter.close();
    }
}
