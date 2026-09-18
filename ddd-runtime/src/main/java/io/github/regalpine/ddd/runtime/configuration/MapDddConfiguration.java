package io.github.regalpine.ddd.runtime.configuration;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map-backed implementation of {@link DddConfiguration}.
 *
 * <p>Values are stored in a concurrent map. If a key is not present,
 * the key's default value is returned.</p>
 *
 * @author RegalPine
 */
public final class MapDddConfiguration implements DddConfiguration {

    private final Map<ConfigurationKey<?>, Object> values;

    private MapDddConfiguration(Map<ConfigurationKey<?>, Object> values) {
        this.values = new ConcurrentHashMap<>(values);
    }

    /**
     * Creates an empty configuration that returns only default values.
     *
     * @return a new empty configuration
     */
    public static MapDddConfiguration empty() {
        return new MapDddConfiguration(Map.of());
    }

    /**
     * Creates a configuration from the given map.
     *
     * @param values the configuration values
     * @return a new map-backed configuration
     */
    public static MapDddConfiguration of(Map<ConfigurationKey<?>, Object> values) {
        return new MapDddConfiguration(Objects.requireNonNull(values, "values must not be null"));
    }

    /**
     * Sets a configuration value.
     *
     * @param key   the configuration key
     * @param value the value
     * @param <T>   the value type
     * @return this instance for fluent chaining
     */
    public <T> MapDddConfiguration set(ConfigurationKey<T> key, T value) {
        values.put(Objects.requireNonNull(key, "key must not be null"), value);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(ConfigurationKey<T> key) {
        Objects.requireNonNull(key, "key must not be null");
        Object value = values.get(key);
        if (value != null) {
            return key.type().cast(value);
        }
        return key.defaultValue();
    }
}
