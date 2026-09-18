package io.github.regalpine.ddd.core.error;

/**
 * Categorizes framework errors by their originating layer.
 *
 * <p>Phase X §58: Error classification enables layer-specific error handling
 * and ensures infrastructure exceptions do not leak into the domain.</p>
 *
 * @author RegalPine
 */
public enum ErrorCategory {

    /** Domain layer errors (business rule violations, invariants). */
    DOMAIN,

    /** Application layer errors (use case failures, orchestration). */
    APPLICATION,

    /** Concurrency errors (optimistic locking, version conflicts). */
    CONCURRENCY,

    /** Transaction errors (commit/rollback failures, propagation issues). */
    TRANSACTION,

    /** Infrastructure errors (persistence, mapping, connection failures). */
    INFRASTRUCTURE,

    /** Messaging errors (publish/consume failures, serialization). */
    MESSAGING,

    /** Configuration errors (missing/invalid/ conflicting configuration). */
    CONFIGURATION,

    /** Runtime errors (bootstrap failure, lifecycle, component errors). */
    RUNTIME
}
