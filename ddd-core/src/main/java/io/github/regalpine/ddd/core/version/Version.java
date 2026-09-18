package io.github.regalpine.ddd.core.version;

/**
 * Represents the optimistic concurrency version of an aggregate.
 *
 * <p>Version starts at 0 for a new aggregate and increments by 1
 * on each successful persistence operation. The persistence adapter
 * is responsible for enforcing version checks during updates.</p>
 *
 * <p>Version is a persistence concern, not a business concept.</p>
 *
 * @param value the version number, must be non-negative
 */
public record Version(long value) {

    public Version {
        if (value < 0) {
            throw new IllegalArgumentException("Version must not be negative");
        }
    }

    /**
     * Returns the initial version for a newly created aggregate.
     *
     * @return version 0
     */
    public static Version initial() {
        return new Version(0);
    }

    /**
     * Returns the next version by incrementing this version by 1.
     *
     * @return the next version
     */
    public Version next() {
        return new Version(value + 1);
    }
}
