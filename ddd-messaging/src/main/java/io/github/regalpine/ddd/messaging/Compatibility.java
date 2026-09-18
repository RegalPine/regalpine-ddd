package io.github.regalpine.ddd.messaging;

/**
 * Defines schema compatibility strategy for message evolution.
 *
 * @author RegalPine
 */
public enum Compatibility {

    /** New schema can read old data. */
    BACKWARD,

    /** Old schema can read new data. */
    FORWARD,

    /** Both backward and forward compatible. */
    FULL,

    /** No compatibility guarantee. */
    NONE
}
