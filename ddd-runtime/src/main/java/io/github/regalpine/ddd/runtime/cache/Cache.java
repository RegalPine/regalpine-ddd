package io.github.regalpine.ddd.runtime.cache;

import java.time.Duration;
import java.util.Optional;

/**
 * Adapter interface for a key-value cache with TTL support.
 *
 * <p>The cache is an optional infrastructure component. It must not
 * become the source of truth — the database remains the write model
 * source of truth.</p>
 *
 * <p>Cache invalidation should be event-driven (after commit), not
 * before commit, to avoid inconsistency when transactions fail.</p>
 *
 * @author RegalPine
 */
public interface Cache {

    /**
     * Returns the cached value for the given key, if present.
     *
     * @param key the cache key
     * @return an optional containing the value, or empty if not cached or expired
     */
    Optional<byte[]> get(String key);

    /**
     * Stores a value in the cache with the given TTL.
     *
     * @param key   the cache key
     * @param value the value to cache
     * @param ttl   the time-to-live before expiration
     */
    void put(String key, byte[] value, Duration ttl);

    /**
     * Removes the cached value for the given key.
     *
     * @param key the cache key
     */
    void evict(String key);
}
