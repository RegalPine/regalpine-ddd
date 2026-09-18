package io.github.regalpine.ddd.messaging;

/**
 * Represents the lifecycle state of a message.
 *
 * <p>Phase IX §71: Message state represents the messaging lifecycle only.
 * It must not be confused with Domain Entity state.</p>
 *
 * <p>Normal flow: CREATED → OUTBOXED → PUBLISHED → RECEIVED → PROCESSING → PROCESSED.
 * Exception flow: PROCESSING → FAILED → RETRYING → PROCESSING, or → DEAD_LETTERED.</p>
 *
 * @author RegalPine
 */
public enum MessageState {

    /** Message has been created. */
    CREATED,

    /** Message has been written to the outbox. */
    OUTBOXED,

    /** Message has been published to the broker. */
    PUBLISHED,

    /** Message has been received by a consumer. */
    RECEIVED,

    /** Message is being processed. */
    PROCESSING,

    /** Message has been successfully processed. */
    PROCESSED,

    /** Message is being retried after failure. */
    RETRYING,

    /** Message has been moved to the dead letter queue. */
    DEAD_LETTERED
}
