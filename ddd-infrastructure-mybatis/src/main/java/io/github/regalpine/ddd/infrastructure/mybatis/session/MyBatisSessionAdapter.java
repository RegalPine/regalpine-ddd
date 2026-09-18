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
        if (currentSession.get() != null) {
            throw new IllegalStateException("Session already active on current thread");
        }
        SqlSession session = sessionFactory.openSession();
        currentSession.set(session);
        return session;
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
