package io.github.regalpine.ddd.infrastructure.jdbc;

import io.github.regalpine.ddd.messaging.inbox.*;
import java.time.Instant;
import java.util.*;

/** 与业务写入借用同一事务连接的 PostgreSQL Inbox。 */
public final class JdbcInboxStore implements InboxStore {
    private final JdbcConnectionAccess connections;
    public JdbcInboxStore(JdbcConnectionAccess connections) {
        this.connections = Objects.requireNonNull(connections, "connections");
    }
    @Override public boolean tryInsert(InboxRecord r) {
        Objects.requireNonNull(r, "record");
        return connections.execute(true, c -> {
            try (var ps = c.prepareStatement("INSERT INTO inbox (message_id,consumer_id,event_type,received_at,processed_at,status,attempts,error) " +
                    "VALUES (?,?,?,?,?,?,?,?) ON CONFLICT (consumer_id,message_id) DO NOTHING")) {
                JdbcOutboxStore.bind(ps, r.eventId(), r.consumerId(), r.eventType(), r.receivedAt(), r.processedAt(), r.status(), r.attempts(), r.error());
                return ps.executeUpdate() == 1;
            }
        });
    }
    @Override public boolean exists(String consumer, String id) {
        return findBy(consumer, id).filter(r -> "PROCESSED".equals(r.status())).isPresent();
    }
    @Override public void record(String consumer, String id) {
        tryInsert(InboxRecord.create(id, consumer, "unknown").markProcessed());
    }
    @Override public void save(InboxRecord record) {
        if (!tryInsert(record)) throw new IllegalStateException("Inbox 已存在");
    }
    @Override public void markProcessed(String consumer, String id) {
        connections.execute(true, c -> {
            try (var ps = c.prepareStatement("UPDATE inbox SET status='PROCESSED',processed_at=? WHERE consumer_id=? AND message_id=?")) {
                JdbcOutboxStore.bind(ps, Instant.now(), consumer, id);
                if (ps.executeUpdate() != 1) throw new IllegalStateException("Inbox 记录不存在");
                return null;
            }
        });
    }
    @Override public Optional<InboxRecord> findBy(String consumer, String id) {
        Objects.requireNonNull(consumer, "consumer");
        Objects.requireNonNull(id, "id");
        return connections.execute(false, c -> {
            try (var ps = c.prepareStatement("SELECT message_id,consumer_id,event_type,received_at,processed_at,status,attempts,error FROM inbox WHERE consumer_id=? AND message_id=?")) {
                JdbcOutboxStore.bind(ps, consumer, id);
                try (var rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(new InboxRecord(rs.getString(1), rs.getString(2), rs.getString(3),
                            JdbcOutboxStore.instant(rs, "received_at"), JdbcOutboxStore.instant(rs, "processed_at"),
                            rs.getString(6), rs.getInt(7), rs.getString(8)));
                }
            }
        });
    }
}
