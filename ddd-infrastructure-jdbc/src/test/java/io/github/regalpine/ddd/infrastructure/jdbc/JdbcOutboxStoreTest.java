package io.github.regalpine.ddd.infrastructure.jdbc;

import io.github.regalpine.ddd.core.event.EventId;
import io.github.regalpine.ddd.event.outbox.OutboxRecord;
import io.github.regalpine.ddd.infrastructure.exception.PersistenceAccessException;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link JdbcOutboxStore} using H2 in-memory database.
 */
@DisplayName("JdbcOutboxStore")
class JdbcOutboxStoreTest {

    private ConnectionProvider connectionProvider;
    private JdbcOutboxStore store;

    @BeforeEach
    void setUp() throws Exception {
        connectionProvider = () -> DriverManager.getConnection(
                "jdbc:h2:mem:testOutbox;DB_CLOSE_DELAY=-1", "sa", "");

        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS outbox (
                        outbox_id      VARCHAR(128) NOT NULL,
                        event_id       VARCHAR(255) NOT NULL,
                        event_type     VARCHAR(255) NOT NULL,
                        event_version  INTEGER NOT NULL,
                        aggregate_type VARCHAR(255) NOT NULL,
                        aggregate_id   VARCHAR(255) NOT NULL,
                        tenant_id      VARCHAR(128) NULL,
                        payload        TEXT NOT NULL,
                        occurred_at    TIMESTAMP NOT NULL,
                        created_at     TIMESTAMP NOT NULL,
                        status         VARCHAR(32) NOT NULL,
                        attempts       INTEGER NOT NULL,
                        published_at   TIMESTAMP NULL,
                        PRIMARY KEY (outbox_id)
                    )
                    """);
        }

        store = new JdbcOutboxStore(connectionProvider);
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
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
        OutboxRecord record = createRecord();
        store.append(record);

        List<OutboxRecord> pending = store.loadPending(10);
        assertEquals(1, pending.size());
        assertEquals(record.outboxId(), pending.get(0).outboxId());
        assertEquals("PENDING", pending.get(0).status());
    }

    @Test
    @DisplayName("markPublished removes from pending")
    void markPublishedRemovesFromPending() {
        OutboxRecord record = createRecord();
        store.append(record);

        store.markPublished(EventId.of(record.eventId()));

        List<OutboxRecord> pending = store.loadPending(10);
        assertTrue(pending.isEmpty());
    }

    @Test
    @DisplayName("SQL error maps to PersistenceAccessException")
    void sqlErrorMapsToPersistenceAccessException() {
        // Drop the table to force a SQL error
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE outbox");
        } catch (SQLException e) {
            fail("setup failure", e);
        }

        assertThrows(PersistenceAccessException.class, () -> store.append(createRecord()));
    }
}
