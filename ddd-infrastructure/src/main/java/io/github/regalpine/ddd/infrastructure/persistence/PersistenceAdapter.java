package io.github.regalpine.ddd.infrastructure.persistence;

import java.util.Optional;

/**
 * Service Provider Interface for persistence adapters.
 *
 * <p>Per Phase VII §82/§83, this SPI defines the contract between
 * the Domain Repository layer and the Database Driver layer:</p>
 * <pre>{@code
 * Domain Repository
 *        │
 *        ▼
 * Concrete Repository Adapter
 *        │
 *        ▼
 * Persistence Store  ← you are here
 *        │
 *        ▼
 * Database Driver
 * }</pre>
 *
 * <p>Implementations handle the actual storage operations (insert, update, delete)
 * and retrieval (load). Each adapter technology (JDBC, JPA, MyBatis) provides
 * its own implementation.</p>
 *
 * <p>This is a suggested contract; not all repositories must implement it directly.
 * Repositories should prefer domain semantics over generic CRUD (§82).</p>
 *
 * @param <D> the domain aggregate type
 * @param <P> the persistence model type
 * @param <I> the identifier type
 * @author RegalPine
 */
public interface PersistenceAdapter<D, P, I> {

    /**
     * Loads a domain aggregate by its identity.
     *
     * @param id the aggregate identity
     * @return an optional containing the aggregate, or empty if not found
     */
    Optional<D> load(I id);

    /**
     * Inserts a new aggregate into the persistence store.
     *
     * @param aggregate the aggregate to insert, must not be {@code null}
     */
    void insert(D aggregate);

    /**
     * Updates an existing aggregate in the persistence store.
     *
     * <p>Implementations must check the expected version for optimistic
     * concurrency control (PCON-001).</p>
     *
     * @param aggregate the aggregate to update, must not be {@code null}
     */
    void update(D aggregate);

    /**
     * Deletes an aggregate from the persistence store.
     *
     * <p>Implementations must check the expected version for concurrency
     * control (PERSIST-013).</p>
     *
     * @param aggregate the aggregate to delete, must not be {@code null}
     */
    void delete(D aggregate);
}
