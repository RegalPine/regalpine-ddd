package io.github.regalpine.ddd.core.pagination;

/**
 * Strategy for executing COUNT queries during pagination.
 *
 * <p>Phase XII §18: controls whether and how the total element count
 * is determined for a paginated query.</p>
 *
 * @author RegalPine
 */
public enum CountMode {

    /**
     * Execute exact COUNT(*) query.
     */
    EXACT,

    /**
     * Do not execute COUNT; totalElements = -1, totalPages = -1.
     */
    NONE,

    /**
     * Database estimated count where supported (e.g. pg_class.reltuples).
     */
    ESTIMATED,

    /**
     * Adapter determines the best strategy automatically.
     */
    AUTO
}
