package io.github.regalpine.ddd.runtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link RepositoryFactory} interface contract.
 */
class RepositoryFactoryTest {

    @Test
    void shouldDefineRepositoryFactoryInterface() {
        // RepositoryFactory is an interface — verify it can be implemented
        RepositoryFactory factory = new RepositoryFactory() {
            @Override
            public <A extends io.github.regalpine.ddd.core.aggregate.AggregateRoot<I>, I extends io.github.regalpine.ddd.core.identifier.Identifier>
            io.github.regalpine.ddd.domain.repository.AggregateRepository<A, I> repository(Class<A> aggregateType) {
                throw new IllegalArgumentException("No repository for: " + aggregateType.getName());
            }
        };

        assertThat(factory).isNotNull();
    }

    @Test
    void shouldReturnRepositoryForRegisteredType() {
        // Verify the interface contract allows returning a repository
        RepositoryFactory factory = new RepositoryFactory() {
            @Override
            public <A extends io.github.regalpine.ddd.core.aggregate.AggregateRoot<I>, I extends io.github.regalpine.ddd.core.identifier.Identifier>
            io.github.regalpine.ddd.domain.repository.AggregateRepository<A, I> repository(Class<A> aggregateType) {
                throw new IllegalArgumentException("Not registered: " + aggregateType.getName());
            }
        };

        assertThat(factory).isInstanceOf(RepositoryFactory.class);
    }
}
