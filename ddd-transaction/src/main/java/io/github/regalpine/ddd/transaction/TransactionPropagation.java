package io.github.regalpine.ddd.transaction;

/**
 * Defines the propagation behavior for transactions.
 *
 * @author RegalPine
 */
public enum TransactionPropagation {

    /** Use an existing transaction or create a new one. */
    REQUIRED,

    /** Always create a new transaction, suspending any existing one. */
    REQUIRES_NEW,

    /** Execute non-transactionally, suspending any existing transaction. */
    NOT_SUPPORTED,

    /** Use an existing transaction or execute non-transactionally. */
    SUPPORTS,

    /** Use an existing transaction or fail if none exists. */
    MANDATORY,

    /** Always execute non-transactionally, failing if a transaction exists. */
    NEVER
}
