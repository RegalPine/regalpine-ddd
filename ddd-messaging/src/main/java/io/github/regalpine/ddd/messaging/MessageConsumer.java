package io.github.regalpine.ddd.messaging;

/**
 * Consumes messages from messaging infrastructure.
 *
 * <p>Phase IX §30: Standard processing pipeline:
 * Receive → Deserialize → Validate → Inbox Check → Handler → Business Transaction → Inbox Commit.</p>
 *
 * @author RegalPine
 */
public interface MessageConsumer {

    /**
     * Consumes a message envelope.
     *
     * @param message the message envelope to consume
     */
    void consume(MessageEnvelope message);
}
