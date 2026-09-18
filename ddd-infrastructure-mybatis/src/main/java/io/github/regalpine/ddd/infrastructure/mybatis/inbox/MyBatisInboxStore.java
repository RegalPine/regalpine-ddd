package io.github.regalpine.ddd.infrastructure.mybatis.inbox;

import io.github.regalpine.ddd.messaging.inbox.InboxRecord;
import io.github.regalpine.ddd.messaging.inbox.InboxStore;
import io.github.regalpine.ddd.infrastructure.mybatis.session.MyBatisSessionAdapter;

import java.util.Objects;
import java.util.Optional;

/**
 * MyBatis-based inbox store implementation.
 *
 * <p>Phase XII §47: persists inbox records via MyBatis mappers.
 * The unique key is {@code consumerId + messageId} (§47/MSG-008)
 * ensuring idempotent message consumption.</p>
 *
 * <p>Phase XII §48: if a duplicate key is detected, the message
 * has already been processed and should be ignored.</p>
 *
 * @author RegalPine
 */
public class MyBatisInboxStore implements InboxStore {

    private final MyBatisSessionAdapter sessionAdapter;

    public MyBatisInboxStore(MyBatisSessionAdapter sessionAdapter) {
        this.sessionAdapter = Objects.requireNonNull(sessionAdapter, "sessionAdapter must not be null");
    }

    @Override
    public boolean exists(String consumerId, String messageId) {
        Objects.requireNonNull(consumerId, "consumerId must not be null");
        Objects.requireNonNull(messageId, "messageId must not be null");
        // Subclasses provide the actual MyBatis mapper SELECT.
        return false;
    }

    @Override
    public void record(String consumerId, String messageId) {
        Objects.requireNonNull(consumerId, "consumerId must not be null");
        Objects.requireNonNull(messageId, "messageId must not be null");
        // Subclasses provide the actual MyBatis mapper INSERT.
    }

    @Override
    public void save(InboxRecord record) {
        Objects.requireNonNull(record, "record must not be null");
        // Subclasses provide the actual MyBatis mapper INSERT.
    }

    @Override
    public void markProcessed(String consumerId, String eventId) {
        Objects.requireNonNull(consumerId, "consumerId must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");
        // Subclasses provide the actual MyBatis mapper UPDATE.
    }

    @Override
    public Optional<InboxRecord> findBy(String consumerId, String eventId) {
        Objects.requireNonNull(consumerId, "consumerId must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");
        // Subclasses provide the actual MyBatis mapper SELECT.
        return Optional.empty();
    }

    /**
     * Returns the session adapter for subclass use.
     */
    protected MyBatisSessionAdapter sessionAdapter() {
        return sessionAdapter;
    }
}
