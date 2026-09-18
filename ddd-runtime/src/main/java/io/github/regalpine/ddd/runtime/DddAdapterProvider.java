package io.github.regalpine.ddd.runtime;

/**
 * SPI for infrastructure adapters to register themselves with the runtime.
 *
 * <p>Implementations can be discovered via {@link java.util.ServiceLoader}
 * using {@code META-INF/services/} or registered explicitly.</p>
 *
 * <p>Example providers:</p>
 * <ul>
 *   <li>{@code JdbcAdapterProvider}</li>
 *   <li>{@code JpaAdapterProvider}</li>
 *   <li>{@code KafkaAdapterProvider}</li>
 *   <li>{@code RedisAdapterProvider}</li>
 * </ul>
 *
 * @author RegalPine
 */
public interface DddAdapterProvider {

    /**
     * Registers adapter components with the given component registry.
     *
     * @param registry the component registry to populate
     */
    void register(ComponentRegistry registry);
}
