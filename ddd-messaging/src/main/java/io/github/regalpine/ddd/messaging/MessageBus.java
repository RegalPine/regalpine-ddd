package io.github.regalpine.ddd.messaging;

/**
 * Integration-level message bus for publishing messages.
 *
 * <p>Phase IX §89/§90: MessageBus is an Integration abstraction.
 * Its scope is Process / Integration / Infrastructure — it must NOT
 * become a Domain Event Bus. The two must remain separate.</p>
 *
 * @author RegalPine
 */
public interface MessageBus {

    /**
     * Publishes a message envelope.
     *
     * @param message the message envelope
     * @return the publish result
     */
    PublishResult publish(MessageEnvelope message);
}
