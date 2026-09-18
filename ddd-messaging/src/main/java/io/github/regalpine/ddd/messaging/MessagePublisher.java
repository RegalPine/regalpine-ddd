package io.github.regalpine.ddd.messaging;

/**
 * Port for publishing messages to messaging infrastructure.
 *
 * <p>Phase IX §23: Publisher is responsible only for Message → Broker.
 * It must not contain Domain Logic, Transaction, Aggregate, Repository, or Saga logic.</p>
 *
 * @author RegalPine
 */
public interface MessagePublisher {

    /**
     * Publishes a message envelope to the messaging infrastructure.
     *
     * @param message the message envelope to publish
     * @return the publish result
     */
    PublishResult publish(MessageEnvelope message);
}
