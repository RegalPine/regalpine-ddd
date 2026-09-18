package io.github.regalpine.ddd.infrastructure.inbox;

import io.github.regalpine.ddd.messaging.inbox.InboxRecord;
import io.github.regalpine.ddd.messaging.inbox.InboxStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link InboxStore} for testing and development.
 *
 * @author RegalPine
 */
public final class InMemoryInboxStore implements InboxStore {

    private final Map<String, InboxRecord> records = new ConcurrentHashMap<>();

    private static String compositeKey(String consumerId, String eventId) {
        return consumerId + "::" + eventId;
    }

    @Override
    public boolean exists(String consumerId, String messageId) {
        InboxRecord record = records.get(compositeKey(consumerId, messageId));
        return record != null && "PROCESSED".equals(record.status());
    }

    @Override
    public void record(String consumerId, String messageId) {
        Objects.requireNonNull(consumerId, "consumerId must not be null");
        Objects.requireNonNull(messageId, "messageId must not be null");
        String key = compositeKey(consumerId, messageId);
        records.putIfAbsent(key, InboxRecord.create(messageId, consumerId, "unknown"));
    }

    @Override
    public void save(InboxRecord record) {
        Objects.requireNonNull(record, "record must not be null");
        records.put(compositeKey(record.consumerId(), record.eventId()), record);
    }

    @Override
    public void markProcessed(String consumerId, String eventId) {
        Objects.requireNonNull(consumerId, "consumerId must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");
        String key = compositeKey(consumerId, eventId);
        records.computeIfPresent(key, (k, record) -> record.markProcessed());
    }

    @Override
    public Optional<InboxRecord> findBy(String consumerId, String eventId) {
        return Optional.ofNullable(records.get(compositeKey(consumerId, eventId)));
    }

    /**
     * Returns all records (for testing).
     */
    public Map<String, InboxRecord> allRecords() {
        return Collections.unmodifiableMap(records);
    }

    /**
     * Clears all records (for testing).
     */
    public void clear() {
        records.clear();
    }
}
