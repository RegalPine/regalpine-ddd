package io.github.regalpine.ddd.conformance;

/**
 * Defines the compatibility levels for adapter certification.
 *
 * <p>Phase X §50: Adapters can declare their conformance level.
 * Each level builds upon the previous one, culminating in FULLY_CONFORMANT.</p>
 *
 * <ul>
 *   <li>{@link #CORE_COMPATIBLE} — implements core domain contracts</li>
 *   <li>{@link #RUNTIME_COMPATIBLE} — also compatible with runtime lifecycle</li>
 *   <li>{@link #PERSISTENCE_COMPATIBLE} — also passes repository conformance</li>
 *   <li>{@link #MESSAGING_COMPATIBLE} — also passes messaging conformance</li>
 *   <li>{@link #FULLY_CONFORMANT} — passes all conformance suites</li>
 * </ul>
 *
 * @author RegalPine
 */
public enum CompatibilityLevel {

    /** Compatible with core domain model contracts. */
    CORE_COMPATIBLE,

    /** Also compatible with runtime bootstrap and lifecycle. */
    RUNTIME_COMPATIBLE,

    /** Also passes repository/persistence conformance tests. */
    PERSISTENCE_COMPATIBLE,

    /** Also passes messaging conformance tests. */
    MESSAGING_COMPATIBLE,

    /** Passes all conformance test suites. */
    FULLY_CONFORMANT
}
