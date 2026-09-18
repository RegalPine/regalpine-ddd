package io.github.regalpine.ddd.infrastructure.jdbc;

import io.github.regalpine.ddd.core.event.EventId;
import io.github.regalpine.ddd.event.outbox.*;
import java.sql.*;
import java.time.*;
import java.util.*;

/** PostgreSQL Outbox；借用业务事务连接，不关闭或提交该连接。 */
public final class JdbcOutboxStore implements LeasedOutboxStore {
    public static final String COLUMNS = "outbox_id,event_id,event_type,event_version,aggregate_type,aggregate_id,tenant_id,payload,occurred_at,created_at,status,attempts,published_at,destination,next_attempt_at,lease_token,lease_until,last_error";
    private final JdbcConnectionAccess connections;

    public JdbcOutboxStore(JdbcConnectionAccess connections) {
        this.connections = Objects.requireNonNull(connections, "connections");
    }

    @Override public void append(OutboxRecord r) {
        Objects.requireNonNull(r, "record");
        update("INSERT INTO outbox (" + COLUMNS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                r.outboxId(), r.eventId(), r.eventType(), r.eventVersion(), r.aggregateType(), r.aggregateId(),
                r.tenantId(), r.payload(), r.occurredAt(), r.createdAt(), r.status(), r.attempts(), r.publishedAt(),
                r.destination(), r.nextAttemptAt(), r.leaseToken(), r.leaseUntil(), r.lastError());
    }

    @Override public List<OutboxRecord> loadPending(int limit) {
        positive(limit);
        return query(false, "SELECT " + COLUMNS + " FROM outbox WHERE status='PENDING' ORDER BY created_at,outbox_id LIMIT ?", limit);
    }

    @Override public void markPublished(EventId eventId) {
        throw new UnsupportedOperationException("发布完成必须使用 outboxId 和租约令牌 acknowledge");
    }

    @Override public List<OutboxRecord> claim(int limit, String token, Instant now, Duration lease) {
        positive(limit);
        Objects.requireNonNull(token, "token");
        if (lease.isZero() || lease.isNegative()) throw new IllegalArgumentException("lease 必须为正数");
        return query(true, "UPDATE outbox SET status='IN_FLIGHT',lease_token=?,lease_until=? WHERE outbox_id IN " +
                "(SELECT outbox_id FROM outbox WHERE (status='PENDING' AND next_attempt_at<=?) OR " +
                "(status='IN_FLIGHT' AND lease_until<=?) ORDER BY created_at,outbox_id LIMIT ? FOR UPDATE SKIP LOCKED) RETURNING " + COLUMNS,
                token, now.plus(lease), now, now, limit);
    }

    @Override public boolean acknowledge(String id, String token, Instant now) {
        return update("UPDATE outbox SET status='PUBLISHED',published_at=?,lease_token=NULL,lease_until=NULL " +
                "WHERE outbox_id=? AND lease_token=? AND status='IN_FLIGHT'", now, id, token) == 1;
    }
    @Override public boolean fail(String id, String token, Instant next, String error, int maxAttempts) {
        positive(maxAttempts);
        return update("UPDATE outbox SET status=CASE WHEN attempts+1>=? THEN 'DEAD' ELSE 'PENDING' END," +
                "attempts=attempts+1,next_attempt_at=?,last_error=?,lease_token=NULL,lease_until=NULL " +
                "WHERE outbox_id=? AND lease_token=? AND status='IN_FLIGHT'", maxAttempts, next, error, id, token) == 1;
    }
    @Override public boolean requeue(String id, Instant now) {
        return update("UPDATE outbox SET status='PENDING',attempts=0,next_attempt_at=?,last_error=NULL " +
                "WHERE outbox_id=? AND status='DEAD'", now, id) == 1;
    }

    private int update(String sql, Object... values) {
        return connections.execute(true, connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, values);
                return statement.executeUpdate();
            }
        });
    }
    private List<OutboxRecord> query(boolean transactional, String sql, Object... values) {
        return connections.execute(transactional, connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, values);
                try (ResultSet rs = statement.executeQuery()) {
                    List<OutboxRecord> records = new ArrayList<>();
                    while (rs.next()) records.add(read(rs));
                    return records;
                }
            }
        });
    }
    static void bind(PreparedStatement statement, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            statement.setObject(i + 1, value instanceof Instant instant ? Timestamp.from(instant) : value);
        }
    }
    private OutboxRecord read(ResultSet rs) throws SQLException {
        return new OutboxRecord(rs.getString("outbox_id"), rs.getString("event_id"), rs.getString("event_type"),
                rs.getInt("event_version"), rs.getString("aggregate_type"), rs.getString("aggregate_id"),
                rs.getString("tenant_id"), rs.getString("payload"), instant(rs, "occurred_at"), instant(rs, "created_at"),
                rs.getString("status"), rs.getInt("attempts"), instant(rs, "published_at"), rs.getString("destination"),
                instant(rs, "next_attempt_at"), rs.getString("lease_token"), instant(rs, "lease_until"), rs.getString("last_error"));
    }
    static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }
    private void positive(int value) {
        if (value <= 0) throw new IllegalArgumentException("参数必须为正数");
    }
}
