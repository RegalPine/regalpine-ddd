package io.github.regalpine.ddd.messaging.dlq;

import io.github.regalpine.ddd.messaging.MessageEnvelope;

/**
 * Publishes messages to the Dead Letter Queue after retry exhaustion.
 *
 * <p>Phase IX §37: Messages that exceed the retry limit are forwarded to DLQ.
 * DLQ messages must preserve (§38): originalMessageId, originalMessageType,
 * originalPayload, failureReason, failureType, attemptCount, firstReceivedAt,
 * lastFailedAt, consumerId.</p>
 *
 * <p>Phase IX §84: Poison messages must be able to enter DLQ (MSG-016).
 * Phase IX §102: Failure matrix maps various failures to DLQ.</p>
 *
 * @author RegalPine
 */
public interface DeadLetterPublisher {

    /**
     * Publishes a failed message to the dead letter queue.
     *
     * @param message  the original message envelope
     * @param cause    the failure cause
     * @param attempts the number of processing attempts
     */
    void publish(MessageEnvelope message, Throwable cause, int attempts);
}
