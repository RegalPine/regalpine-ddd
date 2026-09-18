package io.github.regalpine.ddd.infrastructure.mybatis.configuration;

import org.apache.ibatis.session.Configuration;

import java.util.Objects;

/**
 * Wraps the native MyBatis {@link Configuration} with framework-level settings.
 *
 * <p>Phase XII §5: provides a bridge between the MyBatis configuration
 * and the RegalPine adapter configuration without modifying MyBatis internals.</p>
 *
 * @author RegalPine
 */
public final class MyBatisConfiguration {

    private final Configuration nativeConfiguration;
    private final MyBatisAdapterConfiguration adapterConfiguration;

    public MyBatisConfiguration(
            Configuration nativeConfiguration,
            MyBatisAdapterConfiguration adapterConfiguration) {
        this.nativeConfiguration = Objects.requireNonNull(nativeConfiguration, "nativeConfiguration must not be null");
        this.adapterConfiguration = Objects.requireNonNull(adapterConfiguration, "adapterConfiguration must not be null");
    }

    /**
     * Returns the native MyBatis configuration.
     */
    public Configuration nativeConfiguration() {
        return nativeConfiguration;
    }

    /**
     * Returns the framework adapter configuration.
     */
    public MyBatisAdapterConfiguration adapterConfiguration() {
        return adapterConfiguration;
    }
}
