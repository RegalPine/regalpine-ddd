package io.github.regalpine.ddd.infrastructure.messaging;

import io.github.regalpine.ddd.messaging.MessageBus;
import io.github.regalpine.ddd.messaging.MessageEnvelope;
import io.github.regalpine.ddd.messaging.PublishResult;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation of {@link MessageBus}.
 *
 * <p>Phase X §33: InMemoryMessageBus is required for unit tests, integration tests,
 * conformance tests, demos, and prototypes.</p>
 *
 * <p>Stores published messages in memory for later inspection.
 * Thread-safe for concurrent access.</p>
 *
 * @author RegalPine
 */
public final class InMemoryMessageBus implements MessageBus {

    private final List<MessageEnvelope> published = new CopyOnWriteArrayList<>();
    private boolean failOnNextPublish;

    @Override
    public PublishResult publish(MessageEnvelope message) {
        Objects.requireNonNull(message, "message must not be null");
        if (failOnNextPublish) {
            failOnNextPublish = false;
            return PublishResult.failure(message.messageId(), "simulated failure");
        }
        published.add(message);
        return PublishResult.success(message.messageId());
    }

    /**
     * Returns all published messages.
     *
     * @return an unmodifiable list of published messages
     */
    public List<MessageEnvelope> published() {
        return List.copyOf(published);
    }

    /**
     * Returns the number of published messages.
     *
     * @return the message count
     */
    public int size() {
        return published.size();
    }

    /**
     * Causes the next {@link #publish} call to fail with a simulated error.
     * Useful for testing failure scenarios.
     */
    public void failOnNextPublish() {
        this.failOnNextPublish = true;
    }

    /**
     * Clears all published messages.
     */
    public void clear() {
        published.clear();
    }
}
