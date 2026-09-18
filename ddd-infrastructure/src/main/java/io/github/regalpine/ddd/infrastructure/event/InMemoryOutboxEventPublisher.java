package io.github.regalpine.ddd.infrastructure.event;

import io.github.regalpine.ddd.event.outbox.OutboxEventPublisher;
import io.github.regalpine.ddd.event.outbox.OutboxRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * In-memory implementation of {@link OutboxEventPublisher} for testing.
 * <p>
 * Captures published records for verification.
 *
 * @author RegalPine
 */
public final class InMemoryOutboxEventPublisher implements OutboxEventPublisher {

    private final List<OutboxRecord> published = new ArrayList<>();

    @Override
    public void publish(OutboxRecord record) {
        Objects.requireNonNull(record, "record must not be null");
        published.add(record);
    }

    /**
     * Returns all published records (for testing).
     */
    public List<OutboxRecord> publishedRecords() {
        return Collections.unmodifiableList(published);
    }

    /**
     * Clears published records (for testing).
     */
    public void clear() {
        published.clear();
    }
}
