package io.github.regalpine.ddd.infrastructure.mybatis.configuration;

import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Provides the MyBatis {@link SqlSessionFactory} for the adapter.
 *
 * <p>Phase XII §5/§59: the factory is typically created from a DataSource
 * and MyBatis configuration. This interface allows pluggable factory
 * creation strategies.</p>
 *
 * @author RegalPine
 */
public interface MyBatisSessionFactoryProvider {

    /**
     * Returns the SqlSessionFactory for creating sessions.
     *
     * @return the session factory, never {@code null}
     */
    SqlSessionFactory sessionFactory();
}
