package io.github.regalpine.ddd.infrastructure.mybatis.configuration;

import io.github.regalpine.ddd.core.pagination.CountMode;

/**
 * Configuration record for the MyBatis adapter.
 *
 * <p>Phase XII §62: controls adapter behavior including lazy initialization,
 * naming strategy, pagination, optimistic locking, and default count mode.</p>
 *
 * @param lazyInitialization    whether to lazily initialize MyBatis mappers
 * @param mapUnderscoreToCamelCase whether to auto-map underscore column names to camelCase
 * @param paginationEnabled     whether pagination support is active
 * @param optimisticLockEnabled whether optimistic locking is enforced
 * @param defaultCountMode      the default count strategy for paginated queries
 * @author RegalPine
 */
public record MyBatisAdapterConfiguration(
        boolean lazyInitialization,
        boolean mapUnderscoreToCamelCase,
        boolean paginationEnabled,
        boolean optimisticLockEnabled,
        CountMode defaultCountMode
) {

    /**
     * Returns a configuration with recommended defaults:
     * <ul>
     *   <li>lazyInitialization = false</li>
     *   <li>mapUnderscoreToCamelCase = false</li>
     *   <li>paginationEnabled = true</li>
     *   <li>optimisticLockEnabled = true</li>
     *   <li>defaultCountMode = EXACT</li>
     * </ul>
     */
    public static MyBatisAdapterConfiguration defaults() {
        return new MyBatisAdapterConfiguration(
                false, false, true, true, CountMode.EXACT);
    }
}
