package io.github.regalpine.ddd.transaction;

/**
 * Records the idempotency state of a command execution.
 * <p>
 * Command idempotency must be maintained within the same local transaction
 * as the aggregate state and outbox records. The idempotency key is
 * typically {@code tenantId + commandId} or just {@code commandId}.
 *
 * @param commandId        the unique command identifier
 * @param commandType      the fully qualified command type name
 * @param status           the current idempotency status
 * @param resultReference  a reference to the result (e.g., aggregate ID or serialized response)
 * @author RegalPine
 */
public record IdempotencyRecord(
        String commandId,
        String commandType,
        IdempotencyStatus status,
        String resultReference
) {
}
