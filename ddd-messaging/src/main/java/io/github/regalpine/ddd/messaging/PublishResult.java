package io.github.regalpine.ddd.messaging;

import java.util.Objects;

/**
 * Result of a message publish operation.
 *
 * <p>Phase IX §23/§24: Publisher returns a result indicating success or failure.</p>
 *
 * @param success       whether the publish succeeded
 * @param messageId     the message identifier (always present for correlation)
 * @param failureReason the failure reason (null if successful)
 * @author RegalPine
 */
public record PublishResult(boolean success, String messageId, String failureReason) {

    public PublishResult {
        Objects.requireNonNull(messageId, "messageId must not be null");
    }

    /**
     * Creates a successful publish result.
     */
    public static PublishResult success(String messageId) {
        return new PublishResult(true, messageId, null);
    }

    /**
     * Creates a failed publish result.
     */
    public static PublishResult failure(String messageId, String failureReason) {
        return new PublishResult(false, messageId, failureReason);
    }
}
