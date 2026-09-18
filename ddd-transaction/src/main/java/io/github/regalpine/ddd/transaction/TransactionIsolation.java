package io.github.regalpine.ddd.transaction;

/**
 * Defines the isolation level for transactions.
 *
 * @author RegalPine
 */
public enum TransactionIsolation {

    /** Default isolation level of the underlying adapter. */
    DEFAULT,

    /** Dirty reads, non-repeatable reads, and phantom reads can occur. */
    READ_UNCOMMITTED,

    /** Dirty reads are prevented; non-repeatable reads and phantom reads can occur. */
    READ_COMMITTED,

    /** Dirty reads and non-repeatable reads are prevented; phantom reads can occur. */
    REPEATABLE_READ,

    /** Dirty reads, non-repeatable reads, and phantom reads are prevented. */
    SERIALIZABLE
}
