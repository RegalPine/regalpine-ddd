package io.github.regalpine.ddd.messaging.inbox;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Processes incoming messages with idempotency checking via {@link InboxStore}.
 *
 * <p>Phase IX §30: Standard consumer processing pipeline:
 * Receive → Deserialize → Validate → Inbox Check → Handler → Business Transaction → Inbox Commit.</p>
 *
 * <p>If a message has already been processed by the given consumer (duplicate delivery),
 * it is silently skipped (MSG-007, §87). Uses the composite key {@code consumerId + messageId}
 * to allow different consumers to independently process the same message.</p>
 *
 * @author RegalPine
 */
public final class InboxProcessor {

    private final InboxStore inboxStore;

    public InboxProcessor(InboxStore inboxStore) {
        this.inboxStore = Objects.requireNonNull(inboxStore, "inboxStore must not be null");
    }

    /**
     * Processes a message with idempotency guarantee.
     *
     * @param consumerId the consumer/group identifier
     * @param messageId  the unique message/event ID
     * @param eventType  the event type name
     * @param handler    the actual message handler, receiving the created inbox record
     */
    public void process(String consumerId, String messageId, String eventType, Consumer<InboxRecord> handler) {
        Objects.requireNonNull(consumerId, "consumerId must not be null");
        Objects.requireNonNull(messageId, "messageId must not be null");
        Objects.requireNonNull(handler, "handler must not be null");

        if (inboxStore.exists(consumerId, messageId)) {
            return; // Idempotent skip: already processed (§87)
        }

        InboxRecord record = InboxRecord.create(messageId, consumerId, eventType);
        inboxStore.save(record);

        handler.accept(record);

        inboxStore.markProcessed(consumerId, messageId);
    }
}
