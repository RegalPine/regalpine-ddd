package io.github.regalpine.ddd.infrastructure.mybatis.configuration;

import io.github.regalpine.ddd.core.pagination.CountMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MyBatisAdapterConfiguration} (Phase XII §62).
 */
@DisplayName("MyBatisAdapterConfiguration")
class MyBatisAdapterConfigurationTest {

    @Test
    @DisplayName("defaults() returns recommended defaults")
    void defaultsAreCorrect() {
        var config = MyBatisAdapterConfiguration.defaults();

        assertFalse(config.lazyInitialization());
        assertFalse(config.mapUnderscoreToCamelCase());
        assertTrue(config.paginationEnabled());
        assertTrue(config.optimisticLockEnabled());
        assertEquals(CountMode.EXACT, config.defaultCountMode());
    }

    @Test
    @DisplayName("custom configuration is preserved")
    void customConfiguration() {
        var config = new MyBatisAdapterConfiguration(
                true, true, false, false, CountMode.NONE);

        assertTrue(config.lazyInitialization());
        assertTrue(config.mapUnderscoreToCamelCase());
        assertFalse(config.paginationEnabled());
        assertFalse(config.optimisticLockEnabled());
        assertEquals(CountMode.NONE, config.defaultCountMode());
    }
}
