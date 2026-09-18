package io.github.regalpine.ddd.runtime;

import java.util.Optional;

/**
 * Context available to {@link RuntimeComponent}s during lifecycle operations.
 *
 * <p>Provides type-safe access to registered framework services.
 * This is a component registry, not a domain service locator.</p>
 *
 * @author RegalPine
 */
public interface RuntimeContext {

    /**
     * Looks up a service by type.
     *
     * @param type the service type
     * @param <T>  the type parameter
     * @return an optional containing the service, or empty if not found
     */
    <T> Optional<T> get(Class<T> type);

    /**
     * Looks up a required service by type.
     *
     * @param type the service type
     * @param <T>  the type parameter
     * @return the service instance
     * @throws java.util.NoSuchElementException if the service is not registered
     */
    <T> T require(Class<T> type);
}
