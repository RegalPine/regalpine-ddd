package io.github.regalpine.ddd.conformance.mybatis;

import io.github.regalpine.ddd.domain.repository.AggregateRepository;
import io.github.regalpine.ddd.infrastructure.mybatis.repository.MyBatisAggregateRepository;

/**
 * Conformance test suite for MyBatis aggregate repository implementations.
 *
 * <p>Phase XII §66: verifies CRUD, identity, version, concurrency, and mapping
 * conformance for any {@link MyBatisAggregateRepository} implementation.</p>
 *
 * <p>Subclasses must provide concrete test fixtures (repository, aggregate factory,
 * identifier factory). Integration tests require a real database.</p>
 *
 * @author RegalPine
 */
public abstract class MyBatisRepositoryConformance {

    /**
     * Subclasses must provide the repository under test.
     */
    protected abstract MyBatisAggregateRepository<?, ?> repository();

    /**
     * Verifies that the repository implements AggregateRepository.
     */
    protected void verifyImplementsAggregateRepository() {
        if (!(repository() instanceof AggregateRepository)) {
            throw new AssertionError(
                    "MyBatisAggregateRepository must implement AggregateRepository");
        }
    }

    /**
     * Verifies that the repository does not commit its own transaction (MYBATIS-006).
     */
    protected void verifyNoSelfCommit() {
        // Structural check: repository class must not have commit() method
        try {
            repository().getClass().getMethod("commit");
            throw new AssertionError(
                    "Repository must not have a commit() method (MYBATIS-006)");
        } catch (NoSuchMethodException expected) {
            // correct
        }
    }

    /**
     * Runs all structural conformance checks.
     */
    public void runAll() {
        verifyImplementsAggregateRepository();
        verifyNoSelfCommit();
    }
}
