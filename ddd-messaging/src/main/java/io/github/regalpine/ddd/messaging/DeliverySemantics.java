package io.github.regalpine.ddd.messaging;

/**
 * Defines the delivery semantics for messaging.
 *
 * <p>Phase IX §25: The framework supports AT_MOST_ONCE and AT_LEAST_ONCE.
 * Exactly-once is NOT declared as a default capability (MSG-014/MSG-015).</p>
 *
 * @author RegalPine
 */
public enum DeliverySemantics {

    /** At-most-once delivery: messages may be lost but are never duplicated. */
    AT_MOST_ONCE,

    /** At-least-once delivery: messages are never lost but may be duplicated. */
    AT_LEAST_ONCE
}
