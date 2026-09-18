package io.github.regalpine.ddd.runtime;

import io.github.regalpine.ddd.core.aggregate.AggregateRoot;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.domain.repository.AggregateRepository;

/**
 * Factory for discovering repositories by aggregate type.
 *
 * <p>The runtime uses this to resolve the correct repository
 * for a given aggregate class.</p>
 *
 * @author RegalPine
 */
public interface RepositoryFactory {

    /**
     * Returns the repository for the given aggregate type.
     *
     * @param aggregateType the aggregate root class
     * @param <A>           the aggregate type
     * @param <I>           the identifier type
     * @return the repository, never {@code null}
     * @throws IllegalArgumentException if no repository is registered for the type
     */
    <A extends AggregateRoot<I>, I extends Identifier>
    AggregateRepository<A, I> repository(Class<A> aggregateType);
}
