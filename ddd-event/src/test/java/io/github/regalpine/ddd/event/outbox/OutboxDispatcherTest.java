package io.github.regalpine.ddd.event.outbox;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OutboxDispatcherTest {

    @Test
    void dispatchPendingShouldPublishAndMarkPublished() {
        var published = new ArrayList<OutboxRecord>();
        var markedIds = new ArrayList<String>();

        OutboxStore store = new OutboxStore() {
            @Override
            public void append(OutboxRecord record) {}

            @Override
            public List<OutboxRecord> loadPending(int limit) {
                return List.of(OutboxRecord.create(
                        "ob-1", "ev-1", "TestEvent", 1,
                        "Order", "order-1", "{}", Instant.now()));
            }

            @Override
            public void markPublished(io.github.regalpine.ddd.core.event.EventId eventId) {
                markedIds.add(eventId.value());
            }
        };

        OutboxEventPublisher publisher = published::add;

        var dispatcher = new OutboxDispatcher(store, publisher);
        int count = dispatcher.dispatchPending(10);

        assertThat(count).isEqualTo(1);
        assertThat(published).hasSize(1);
        assertThat(markedIds).containsExactly("ev-1");
    }

    @Test
    void dispatchPendingShouldReturnZeroWhenNoRecords() {
        OutboxStore store = new OutboxStore() {
            @Override
            public void append(OutboxRecord record) {}

            @Override
            public List<OutboxRecord> loadPending(int limit) {
                return List.of();
            }

            @Override
            public void markPublished(io.github.regalpine.ddd.core.event.EventId eventId) {}
        };

        var dispatcher = new OutboxDispatcher(store, r -> {});
        int count = dispatcher.dispatchPending(10);

        assertThat(count).isZero();
    }
}
