package io.github.regalpine.ddd.infrastructure.jdbc;

import io.github.regalpine.ddd.core.event.EventId;
import io.github.regalpine.ddd.event.outbox.OutboxRecord;
import io.github.regalpine.ddd.event.outbox.OutboxStore;
import io.github.regalpine.ddd.infrastructure.exception.PersistenceAccessException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JDBC-based outbox store implementation.
 *
 * <p>Persists outbox records to a relational database table.
 * Requires a table named "outbox" with appropriate columns.</p>
 *
 * @author RegalPine
 */
public final class JdbcOutboxStore implements OutboxStore {

    private static final String INSERT_SQL =
            "INSERT INTO outbox (outbox_id, event_id, event_type, event_version, aggregate_type, aggregate_id, tenant_id, payload, occurred_at, created_at, status, attempts, published_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_PENDING_SQL =
            "SELECT outbox_id, event_id, event_type, event_version, aggregate_type, aggregate_id, tenant_id, payload, occurred_at, created_at, status, attempts, published_at " +
                    "FROM outbox WHERE status = 'PENDING' LIMIT ?";
    private static final String MARK_PUBLISHED_SQL =
            "UPDATE outbox SET status = 'PUBLISHED', published_at = ? WHERE event_id = ?";

    private final ConnectionProvider connectionProvider;

    public JdbcOutboxStore(ConnectionProvider connectionProvider) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider, "connectionProvider must not be null");
    }

    @Override
    public void append(OutboxRecord record) {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, record.outboxId());
            ps.setString(2, record.eventId());
            ps.setString(3, record.eventType());
            ps.setInt(4, record.eventVersion());
            ps.setString(5, record.aggregateType());
            ps.setString(6, record.aggregateId());
            ps.setString(7, record.tenantId());
            ps.setString(8, record.payload());
            ps.setTimestamp(9, Timestamp.from(record.occurredAt()));
            ps.setTimestamp(10, Timestamp.from(record.createdAt()));
            ps.setString(11, record.status());
            ps.setInt(12, record.attempts());
            ps.setTimestamp(13, record.publishedAt() != null ? Timestamp.from(record.publishedAt()) : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceAccessException("Failed to append outbox record", e);
        }
    }

    @Override
    public List<OutboxRecord> loadPending(int limit) {
        List<OutboxRecord> records = new ArrayList<>();
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PENDING_SQL)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp publishedAtTs = rs.getTimestamp("published_at");
                    records.add(new OutboxRecord(
                            rs.getString("outbox_id"),
                            rs.getString("event_id"),
                            rs.getString("event_type"),
                            rs.getInt("event_version"),
                            rs.getString("aggregate_type"),
                            rs.getString("aggregate_id"),
                            rs.getString("tenant_id"),
                            rs.getString("payload"),
                            rs.getTimestamp("occurred_at").toInstant(),
                            rs.getTimestamp("created_at").toInstant(),
                            rs.getString("status"),
                            rs.getInt("attempts"),
                            publishedAtTs != null ? publishedAtTs.toInstant() : null
                    ));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceAccessException("Failed to load pending outbox records", e);
        }
        return records;
    }

    @Override
    public void markPublished(EventId eventId) {
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(MARK_PUBLISHED_SQL)) {
            ps.setTimestamp(1, Timestamp.from(java.time.Instant.now()));
            ps.setString(2, eventId.value());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceAccessException("Failed to mark outbox record as published", e);
        }
    }
}
