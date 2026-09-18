package io.github.regalpine.ddd.infrastructure.jdbc;

import io.github.regalpine.ddd.infrastructure.exception.PersistenceAccessException;
import io.github.regalpine.ddd.infrastructure.exception.PersistenceConnectionException;
import io.github.regalpine.ddd.transaction.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * JDBC-based transaction adapter.
 * <p>
 * Manages JDBC connections and transaction boundaries.
 * Uses the provided {@link ConnectionProvider} to obtain database connections.
 *
 * @author RegalPine
 */
public final class JdbcTransactionAdapter implements TransactionAdapter {

    private final ConnectionProvider connectionProvider;
    private final ThreadLocal<Connection> currentConnection = new ThreadLocal<>();

    public JdbcTransactionAdapter(ConnectionProvider connectionProvider) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider, "connectionProvider must not be null");
    }

    @Override
    public void begin(TransactionDefinition definition) {
        if (currentConnection.get() != null) {
            throw new IllegalStateException("Transaction already active on current thread");
        }
        try {
            Connection conn = connectionProvider.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new PersistenceConnectionException("Failed to begin JDBC transaction", e);
        }
    }

    @Override
    public void commit() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalStateException("No active transaction to commit");
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw new PersistenceAccessException("Failed to commit JDBC transaction", e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public void rollback() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new PersistenceAccessException("Failed to rollback JDBC transaction", e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public boolean isActive() {
        return currentConnection.get() != null;
    }

    /**
     * Returns the current JDBC connection for the active transaction.
     *
     * @return the current connection, or null if no transaction is active
     */
    public Connection currentConnection() {
        return currentConnection.get();
    }

    private void closeConnection(Connection conn) {
        currentConnection.remove();
        try {
            conn.close();
        } catch (SQLException e) {
            // Log and ignore
        }
    }
}
