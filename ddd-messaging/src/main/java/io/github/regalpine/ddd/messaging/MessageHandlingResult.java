package io.github.regalpine.ddd.messaging;

/**
 * Defines the unified message failure model.
 *
 * <p>Phase IX §62: Message processing result determines the next action.
 * DROP must have explicit policy and audit capability.</p>
 *
 * @author RegalPine
 */
public enum MessageHandlingResult {

    /** Message processed successfully. */
    SUCCESS,

    /** Message should be retried. */
    RETRY,

    /** Message should be sent to dead letter queue. */
    DEAD_LETTER,

    /** Message should be dropped (requires audit). */
    DROP
}
