package io.github.regalpine.ddd.runtime.configuration;

import java.util.Objects;

/**
 * A typed key for accessing configuration values.
 *
 * <p>Phase XI §117: ConfigurationKey is a record with name, type, and default value.
 * Equality is based on {@link #name()} only, so the same logical key
 * can be used across different configuration instances.</p>
 *
 * @param <T> the configuration value type
 * @author RegalPine
 */
public record ConfigurationKey<T>(String name, Class<T> type, T defaultValue) {

    public ConfigurationKey {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }

    /**
     * Creates a new configuration key.
     *
     * @param name         the key name (e.g. "ddd.transaction.timeout")
     * @param type         the value type
     * @param defaultValue the default value
     * @param <T>          the value type
     * @return a new configuration key
     */
    public static <T> ConfigurationKey<T> of(String name, Class<T> type, T defaultValue) {
        return new ConfigurationKey<>(name, type, defaultValue);
    }

    /**
     * Equality is based on name only, so the same logical key
     * works across different ConfigurationKey instances.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationKey<?> that)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "ConfigurationKey[" + name + "]";
    }
}
