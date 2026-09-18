package io.github.regalpine.ddd.infrastructure.mybatis.outbox;

import io.github.regalpine.ddd.core.event.EventId;
import io.github.regalpine.ddd.event.outbox.OutboxRecord;
import io.github.regalpine.ddd.event.outbox.OutboxStore;
import io.github.regalpine.ddd.infrastructure.mybatis.session.MyBatisSessionAdapter;

import java.util.List;
import java.util.Objects;

/**
 * MyBatis-based outbox store implementation.
 *
 * <p>Phase XII §45: persists outbox records via MyBatis mappers.
 * Outbox INSERT and Aggregate UPDATE share the same SqlSession
 * and transaction (§46) to ensure atomic event publishing.</p>
 *
 * @author RegalPine
 */
public class MyBatisOutboxStore implements OutboxStore {

    private final MyBatisSessionAdapter sessionAdapter;

    public MyBatisOutboxStore(MyBatisSessionAdapter sessionAdapter) {
        this.sessionAdapter = Objects.requireNonNull(sessionAdapter, "sessionAdapter must not be null");
    }

    @Override
    public void append(OutboxRecord record) {
        Objects.requireNonNull(record, "record must not be null");
        // Subclasses provide the actual MyBatis mapper INSERT.
        // The INSERT must execute within the same SqlSession/transaction
        // as the aggregate UPDATE (§46).
    }

    @Override
    public List<OutboxRecord> loadPending(int limit) {
        // Subclasses provide the actual MyBatis mapper SELECT.
        return List.of();
    }

    @Override
    public void markPublished(EventId eventId) {
        Objects.requireNonNull(eventId, "eventId must not be null");
        // Subclasses provide the actual MyBatis mapper UPDATE.
    }

    /**
     * Returns the session adapter for subclass use.
     */
    protected MyBatisSessionAdapter sessionAdapter() {
        return sessionAdapter;
    }
}
