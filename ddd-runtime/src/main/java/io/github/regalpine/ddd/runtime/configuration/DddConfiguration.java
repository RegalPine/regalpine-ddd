package io.github.regalpine.ddd.runtime.configuration;

/**
 * Provides typed access to framework configuration values.
 *
 * <p>Configuration values are accessed via {@link ConfigurationKey} instances,
 * which provide type safety and default values.</p>
 *
 * <p>Configuration precedence (highest to lowest):</p>
 * <ol>
 *   <li>Programmatic (explicit overrides)</li>
 *   <li>Environment variables</li>
 *   <li>Configuration file</li>
 *   <li>Default value from key</li>
 * </ol>
 *
 * @author RegalPine
 */
public interface DddConfiguration {

    /**
     * Returns the configuration value for the given key.
     *
     * <p>If no explicit value is set, returns the key's default value.</p>
     *
     * @param key the configuration key
     * @param <T> the value type
     * @return the configuration value, or the key's default value
     */
    <T> T get(ConfigurationKey<T> key);
}
