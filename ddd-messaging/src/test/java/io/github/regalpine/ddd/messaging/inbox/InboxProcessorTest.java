package io.github.regalpine.ddd.messaging.inbox;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;

class InboxProcessorTest {

    static class TestInboxStore implements InboxStore {
        final Map<String, InboxRecord> records = new ConcurrentHashMap<>();

        String key(String consumerId, String eventId) {
            return consumerId + "::" + eventId;
        }

        @Override
        public boolean exists(String consumerId, String messageId) {
            var r = records.get(key(consumerId, messageId));
            return r != null && "PROCESSED".equals(r.status());
        }

        @Override
        public void record(String consumerId, String messageId) {
            records.putIfAbsent(key(consumerId, messageId),
                    InboxRecord.create(messageId, consumerId, "unknown"));
        }

        @Override
        public void save(InboxRecord record) {
            records.put(key(record.consumerId(), record.eventId()), record);
        }

        @Override
        public void markProcessed(String consumerId, String eventId) {
            records.computeIfPresent(key(consumerId, eventId), (k, r) -> r.markProcessed());
        }

        @Override
        public Optional<InboxRecord> findBy(String consumerId, String eventId) {
            return Optional.ofNullable(records.get(key(consumerId, eventId)));
        }
    }

    @Test
    void processShouldInvokeHandlerForNewEvent() {
        var store = new TestInboxStore();
        var processor = new InboxProcessor(store);
        var handled = new ArrayList<>();

        processor.process("consumer-1", "event-1", "TestEvent", r -> handled.add(r.eventId()));

        assertThat(handled).containsExactly("event-1");
    }

    @Test
    void processShouldSkipDuplicateEvent() {
        var store = new TestInboxStore();
        var processor = new InboxProcessor(store);
        var handled = new ArrayList<>();

        processor.process("consumer-1", "event-1", "TestEvent", r -> handled.add(r.eventId()));
        processor.process("consumer-1", "event-1", "TestEvent", r -> handled.add(r.eventId()));

        assertThat(handled).hasSize(1);
    }

    @Test
    void differentConsumersShouldProcessSameEvent() {
        var store = new TestInboxStore();
        var processor = new InboxProcessor(store);
        var handled = new ArrayList<>();

        processor.process("consumer-A", "event-1", "TestEvent", r -> handled.add("A"));
        processor.process("consumer-B", "event-1", "TestEvent", r -> handled.add("B"));

        assertThat(handled).containsExactly("A", "B");
    }

    @Test
    void processShouldRejectNullConsumerId() {
        var store = new TestInboxStore();
        var processor = new InboxProcessor(store);

        assertThatThrownBy(() -> processor.process(null, "e1", "T", r -> {}))
                .isInstanceOf(NullPointerException.class);
    }
}
