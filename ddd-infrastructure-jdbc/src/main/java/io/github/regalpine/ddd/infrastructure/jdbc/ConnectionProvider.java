package io.github.regalpine.ddd.infrastructure.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provider for JDBC connections.
 * <p>
 * Implementations may use connection pools, data sources, or simple connection factories.
 *
 * @author RegalPine
 */
public interface ConnectionProvider {

    /**
     * Returns a JDBC connection.
     *
     * @return a new or pooled connection
     * @throws SQLException if a connection cannot be obtained
     */
    Connection getConnection() throws SQLException;
}
