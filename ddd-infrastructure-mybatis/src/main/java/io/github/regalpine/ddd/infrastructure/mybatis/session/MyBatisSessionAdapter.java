package io.github.regalpine.ddd.infrastructure.mybatis.session;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Objects;

/**
 * Default adapter that manages MyBatis session lifecycle.
 *
 * <p>Phase XII §5: bridges the framework session context with
 * the MyBatis SqlSessionFactory. Sessions are opened on demand
 * and closed when the transaction completes.</p>
 *
 * @author RegalPine
 */
public final class MyBatisSessionAdapter {

    private final SqlSessionFactory sessionFactory;
    private final ThreadLocal<SqlSession> currentSession = new ThreadLocal<>();

    public MyBatisSessionAdapter(SqlSessionFactory sessionFactory) {
        this.sessionFactory = Objects.requireNonNull(sessionFactory, "sessionFactory must not be null");
    }

    /**
     * Opens a new session and binds it to the current thread.
     *
     * @return the opened session
     * @throws IllegalStateException if a session is already active
     */
    public SqlSession openSession() {
        return openSession(io.github.regalpine.ddd.transaction.TransactionDefinition.DEFAULT);
    }

    public SqlSession openSession(io.github.regalpine.ddd.transaction.TransactionDefinition definition) {
        if (currentSession.get() != null) {
            throw new IllegalStateException("Session already active on current thread");
        }
        SqlSession session = definition.isolation() == io.github.regalpine.ddd.transaction.TransactionIsolation.DEFAULT
                ? sessionFactory.openSession(false)
                : sessionFactory.openSession(org.apache.ibatis.session.TransactionIsolationLevel.valueOf(definition.isolation().name()));
        try {
            var connection = session.getConnection();
            connection.setReadOnly(definition.readOnly());
            if (!definition.timeout().isZero()) {
                if (!"PostgreSQL".equals(connection.getMetaData().getDatabaseProductName())) {
                    throw new IllegalStateException("SQL 事务超时仅支持 PostgreSQL");
                }
                try (var statement = connection.prepareStatement("SELECT set_config('statement_timeout', ?, true)")) {
                    statement.setString(1, Math.max(1, definition.timeout().toMillis()) + "ms");
                    statement.execute();
                }
            }
            currentSession.set(session);
            return session;
        } catch (java.sql.SQLException | RuntimeException failure) {
            try { session.rollback(true); } catch (RuntimeException e) { failure.addSuppressed(e); }
            try { session.close(); } catch (RuntimeException e) { failure.addSuppressed(e); }
            throw new IllegalStateException("初始化 MyBatis 事务失败", failure);
        }
    }

    public SqlSession suspend() {
        SqlSession session = Objects.requireNonNull(currentSession.get(), "没有活动会话");
        currentSession.remove();
        return session;
    }

    public void resume(SqlSession session) {
        if (hasCurrentSession()) throw new IllegalStateException("已有活动会话");
        currentSession.set(Objects.requireNonNull(session, "session"));
    }

    /** 临时会话仅用于查询，写入要求调用方先建立事务。 */
    public <T> T execute(boolean requireTransaction, java.util.function.Function<SqlSession, T> work) {
        Objects.requireNonNull(work, "work");
        if (hasCurrentSession()) return work.apply(currentSession());
        if (requireTransaction) throw new IllegalStateException("MyBatis 写入必须位于事务内");
        try (SqlSession session = sessionFactory.openSession(true)) {
            return work.apply(session);
        }
    }

    /**
     * Returns the current session without opening a new one.
     *
     * @return the current session, or null if none is active
     */
    public SqlSession currentSession() {
        return currentSession.get();
    }

    /**
     * Returns whether a session is currently active.
     */
    public boolean hasCurrentSession() {
        return currentSession.get() != null;
    }

    /**
     * Closes the current session and unbinds it from the thread.
     */
    public void closeSession() {
        SqlSession session = currentSession.get();
        if (session != null) {
            currentSession.remove();
            session.close();
        }
    }
}
