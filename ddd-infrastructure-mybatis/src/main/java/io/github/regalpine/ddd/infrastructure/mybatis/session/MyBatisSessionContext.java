package io.github.regalpine.ddd.infrastructure.mybatis.session;

import org.apache.ibatis.session.SqlSession;

/**
 * Provides access to the current managed MyBatis session.
 *
 * <p>Phase XII §56: ensures one transaction = one managed SqlSession.
 * Repositories obtain their session from the current context rather
 * than creating their own.</p>
 *
 * @author RegalPine
 */
public interface MyBatisSessionContext {

    /**
     * Returns the current managed SqlSession.
     *
     * @return the current session
     * @throws IllegalStateException if no session is active
     */
    SqlSession current();

    /**
     * Returns whether a session is currently active.
     *
     * @return true if a session is managed
     */
    boolean hasCurrent();
}
