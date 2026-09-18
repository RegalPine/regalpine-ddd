package io.github.regalpine.ddd.domain.factory;

/**
 * Factory for creating complex domain objects.
 *
 * <p>A Domain Factory encapsulates the creation logic for aggregates
 * or entities that require complex initialization, cross-entity
 * coordination, or multi-step construction.</p>
 *
 * <p>Simple aggregates with straightforward constructors do not
 * require a separate factory.</p>
 *
 * @param <I> the input type for creation
 * @param <O> the output type (aggregate or entity)
 */
@FunctionalInterface
public interface DomainFactory<I, O> {

    /**
     * Creates a domain object from the given input.
     *
     * @param input the creation input
     * @return the created domain object
     */
    O create(I input);
}
