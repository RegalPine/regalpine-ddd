package io.github.regalpine.ddd.infrastructure.mybatis.repository;

import io.github.regalpine.ddd.runtime.RepositoryFactory;

/**
 * MyBatis-specific repository factory.
 *
 * <p>Phase XII §61: extends the framework's {@link RepositoryFactory}
 * to provide MyBatis-backed aggregate repositories. Ensures one aggregate
 * type maps to one active repository.</p>
 *
 * @author RegalPine
 */
public interface MyBatisRepositoryFactory extends RepositoryFactory {
}
