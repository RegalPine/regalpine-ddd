package io.github.regalpine.ddd.domain.repository;

import io.github.regalpine.ddd.core.aggregate.AggregateRoot;
import io.github.regalpine.ddd.core.identifier.Identifier;

import java.util.Optional;

/**
 * Repository contract for aggregate persistence.
 *
 * <p>Defined in the domain layer, this interface represents the aggregate's
 * persistence capability as seen by the domain. Infrastructure adapters
 * provide concrete implementations (JDBC, JPA, in-memory, etc.).</p>
 *
 * <p>Key semantics:</p>
 * <ul>
 *   <li>{@code findById} reconstructs the full aggregate in a business-usable state</li>
 *   <li>{@code save} registers the aggregate for persistence, not an immediate commit</li>
 *   <li>{@code delete} registers the aggregate for removal</li>
 * </ul>
 *
 * <p>This is NOT a generic CRUD repository. One repository per aggregate root.</p>
 *
 * @param <A> the aggregate root type
 * @param <I> the identifier type
 */
public interface AggregateRepository<A extends AggregateRoot<I>, I extends Identifier> {

    /**
     * Finds an aggregate root by its identity.
     *
     * <p>The returned aggregate must be fully reconstructed and in a
     * business-usable state. All internal entities and value objects
     * must be loaded.</p>
     *
     * @param id the aggregate identity
     * @return an optional containing the aggregate, or empty if not found
     */
    Optional<A> findById(I id);

    /**
     * Saves the aggregate's current state.
     *
     * <p>This registers the aggregate for persistence within the current
     * unit of work. It does NOT commit the transaction. The actual commit
     * is managed by the transaction boundary.</p>
     *
     * @param aggregate the aggregate to save, must not be {@code null}
     * @return the saved aggregate
     */
    A save(A aggregate);

    /**
     * Registers the aggregate for removal within the current unit of work.
     *
     * @param aggregate the aggregate to delete, must not be {@code null}
     */
    void delete(A aggregate);
}
