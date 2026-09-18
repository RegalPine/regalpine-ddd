package io.github.regalpine.ddd.application.idempotency;

import java.util.Optional;

/**
 * Port for idempotency checking of command execution.
 *
 * <p>Before executing a command, the framework checks whether
 * the command has already been processed. If so, the previous
 * result is returned without re-executing.</p>
 *
 * <p>Infrastructure adapters provide the concrete implementation
 * (e.g. Redis, database, distributed KV store).</p>
 *
 * @see IdempotencyResult
 */
public interface IdempotencyStore {

    /**
     * Checks whether a command with the given key has already been executed.
     *
     * @param key the unique idempotency key (typically the command ID)
     * @return the idempotency result if a record exists, empty otherwise
     */
    Optional<IdempotencyResult> find(String key);

    /**
     * Stores the result of a command execution.
     *
     * @param key    the unique idempotency key
     * @param result the execution result to store
     */
    void store(String key, IdempotencyResult result);
}
