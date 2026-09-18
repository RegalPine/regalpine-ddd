package io.github.regalpine.ddd.transaction;

/**
 * Represents the status of a command idempotency record.
 *
 * @author RegalPine
 */
public enum IdempotencyStatus {

    /** The command is currently being processed. */
    PROCESSING,

    /** The command has been completed successfully. */
    COMPLETED,

    /** The command execution has failed. */
    FAILED
}
