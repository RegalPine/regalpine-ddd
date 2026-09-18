package io.github.regalpine.ddd.infrastructure.mybatis.pagination;

import java.util.Optional;

/**
 * Whitelist registry for sort field names.
 *
 * <p>Phase XII §43/§44: user-provided sort property names must be mapped
 * through this registry before being used in SQL. This prevents SQL injection
 * via sort fields (e.g. "created_at DESC; DROP TABLE orders" must be rejected).</p>
 *
 * <p>Example mapping:</p>
 * <pre>
 * createdAt → created_at
 * customerId → customer_id
 * status → status
 * </pre>
 *
 * @author RegalPine
 */
public interface SortFieldRegistry {

    /**
     * Resolves a user-provided sort property to its database column name.
     *
     * @param property the user-facing property name
     * @return the database column name, or empty if the property is not registered
     */
    Optional<String> resolve(String property);
}
