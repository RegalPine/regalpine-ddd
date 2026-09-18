package io.github.regalpine.ddd.infrastructure.outbox;

import io.github.regalpine.ddd.core.event.EventId;
import io.github.regalpine.ddd.event.outbox.OutboxRecord;
import io.github.regalpine.ddd.event.outbox.OutboxStore;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation of {@link OutboxStore} for testing and development.
 *
 * @author RegalPine
 */
public final class InMemoryOutboxStore implements OutboxStore {

    private final List<OutboxRecord> records = new CopyOnWriteArrayList<>();

    @Override
    public void append(OutboxRecord record) {
        Objects.requireNonNull(record, "record must not be null");
        records.add(record);
    }

    @Override
    public List<OutboxRecord> loadPending(int limit) {
        return records.stream()
                .filter(r -> "PENDING".equals(r.status()))
                .limit(limit)
                .toList();
    }

    @Override
    public void markPublished(EventId eventId) {
        Objects.requireNonNull(eventId, "eventId must not be null");
        for (int i = 0; i < records.size(); i++) {
            OutboxRecord r = records.get(i);
            if (r.eventId().equals(eventId.value())) {
                records.set(i, r.markPublished());
                return;
            }
        }
    }

    /**
     * Returns all records (for testing).
     */
    public List<OutboxRecord> allRecords() {
        return Collections.unmodifiableList(records);
    }

    /**
     * Clears all records (for testing).
     */
    public void clear() {
        records.clear();
    }
}
