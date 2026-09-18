package io.github.regalpine.ddd.infrastructure.jdbc;

import io.github.regalpine.ddd.infrastructure.exception.PersistenceAccessException;
import io.github.regalpine.ddd.infrastructure.exception.PersistenceConnectionException;
import io.github.regalpine.ddd.transaction.*;

import java.sql.*;
import java.util.Objects;

/** JDBC 物理事务；协调器调用 close 统一释放资源。 */
public final class JdbcTransactionAdapter implements TransactionAdapter, JdbcConnectionAccess {
    private final ConnectionProvider provider;
    private final ThreadLocal<Resource> current = new ThreadLocal<>();

    public JdbcTransactionAdapter(ConnectionProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    @Override
    public void begin(TransactionDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (current.get() != null) throw new IllegalStateException("Transaction already active on current thread");
        Connection connection = null;
        try {
            connection = provider.getConnection();
            Resource resource = new Resource(connection, connection.getAutoCommit(), connection.isReadOnly(),
                    connection.getTransactionIsolation());
            int isolation = isolation(definition.isolation());
            if (isolation != 0) {
                if (!connection.getMetaData().supportsTransactionIsolationLevel(isolation)) {
                    throw new SQLException("数据库不支持隔离级别: " + definition.isolation());
                }
                connection.setTransactionIsolation(isolation);
            }
            connection.setReadOnly(definition.readOnly());
            connection.setAutoCommit(false);
            if (!definition.timeout().isZero()) {
                if (!"PostgreSQL".equals(connection.getMetaData().getDatabaseProductName())) {
                    throw new SQLException("当前适配器仅在 PostgreSQL 支持 SQL 事务超时");
                }
                try (PreparedStatement statement = connection.prepareStatement("SELECT set_config('statement_timeout', ?, true)")) {
                    statement.setString(1, Math.max(1, definition.timeout().toMillis()) + "ms");
                    statement.execute();
                }
            }
            current.set(resource);
        } catch (SQLException | RuntimeException failure) {
            if (connection != null) {
                try { connection.rollback(); } catch (SQLException e) { failure.addSuppressed(e); }
                try { connection.close(); } catch (SQLException e) { failure.addSuppressed(e); }
            }
            throw new PersistenceConnectionException("Failed to begin JDBC transaction", failure);
        }
    }

    @Override public void commit() {
        try { require().connection.commit(); require().completed = true; }
        catch (SQLException e) { throw new PersistenceAccessException("Failed to commit JDBC transaction", e); }
    }

    @Override public void rollback() {
        Resource resource = current.get();
        if (resource == null) return;
        try { resource.connection.rollback(); resource.completed = true; }
        catch (SQLException e) { throw new PersistenceAccessException("Failed to rollback JDBC transaction", e); }
    }

    @Override public void close() {
        Resource resource = current.get();
        current.remove();
        if (resource == null) return;
        SQLException failure = null;
        if (!resource.completed) {
            try { resource.connection.rollback(); resource.completed = true; }
            catch (SQLException e) { failure = e; }
        }
        // 回滚失败时不可开启 autoCommit，否则可能提交未知状态的写入。
        if (resource.completed) {
            try { resource.connection.setReadOnly(resource.readOnly); }
            catch (SQLException e) { failure = combine(failure, e); }
            try { resource.connection.setTransactionIsolation(resource.isolation); }
            catch (SQLException e) { failure = combine(failure, e); }
            try { resource.connection.setAutoCommit(resource.autoCommit); }
            catch (SQLException e) { failure = combine(failure, e); }
        }
        try { resource.connection.close(); }
        catch (SQLException e) { failure = combine(failure, e); }
        if (failure != null) throw new PersistenceAccessException("释放 JDBC 资源失败", failure);
    }

    private SQLException combine(SQLException first, SQLException next) {
        if (first == null) return next;
        first.addSuppressed(next);
        return first;
    }

    @Override public boolean isActive() { return current.get() != null; }
    public Connection currentConnection() { return isActive() ? require().connection : null; }
    @Override public Object suspend() {
        Resource resource = require();
        current.remove();
        return resource;
    }
    @Override public void resume(Object token) {
        if (isActive() || !(token instanceof Resource resource)) throw new IllegalStateException("无效事务恢复令牌");
        current.set(resource);
    }

    @Override public <T> T execute(boolean requireTransaction, SqlWork<T> work) {
        Objects.requireNonNull(work, "work");
        if (isActive()) {
            try { return work.execute(require().connection); }
            catch (SQLException e) { throw new PersistenceAccessException("JDBC 操作失败", e); }
        }
        if (requireTransaction) throw new IllegalStateException("写入必须位于活动事务内");
        try (Connection connection = provider.getConnection()) {
            return work.execute(connection);
        } catch (SQLException e) { throw new PersistenceAccessException("JDBC 查询失败", e); }
    }

    private Resource require() {
        Resource resource = current.get();
        if (resource == null) throw new IllegalStateException("No active transaction");
        return resource;
    }
    private int isolation(TransactionIsolation isolation) {
        return switch (isolation) {
            case DEFAULT -> 0;
            case READ_UNCOMMITTED -> Connection.TRANSACTION_READ_UNCOMMITTED;
            case READ_COMMITTED -> Connection.TRANSACTION_READ_COMMITTED;
            case REPEATABLE_READ -> Connection.TRANSACTION_REPEATABLE_READ;
            case SERIALIZABLE -> Connection.TRANSACTION_SERIALIZABLE;
        };
    }
    private static final class Resource {
        final Connection connection;
        final boolean autoCommit;
        final boolean readOnly;
        final int isolation;
        boolean completed;
        Resource(Connection connection, boolean autoCommit, boolean readOnly, int isolation) {
            this.connection = connection;
            this.autoCommit = autoCommit;
            this.readOnly = readOnly;
            this.isolation = isolation;
        }
    }
}
