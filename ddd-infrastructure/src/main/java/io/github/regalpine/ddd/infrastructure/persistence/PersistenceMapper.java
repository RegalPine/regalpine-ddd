package io.github.regalpine.ddd.infrastructure.persistence;

/**
 * Bidirectional mapper between Domain Model and Persistence Model.
 *
 * <p>Per Phase VII §10, the mapper is responsible for:</p>
 * <ul>
 *   <li>Identity Mapping</li>
 *   <li>Value Object Mapping</li>
 *   <li>State Mapping</li>
 *   <li>Version Mapping</li>
 *   <li>Child Entity Mapping</li>
 *   <li>Collection Mapping</li>
 * </ul>
 *
 * <p>The mapper must NOT:</p>
 * <ul>
 *   <li>Execute business rules (MAP-005)</li>
 *   <li>Perform authorization (MAP-006)</li>
 *   <li>Manage transactions (MAP-007)</li>
 * </ul>
 *
 * @param <D> the domain model type
 * @param <P> the persistence model type
 * @author RegalPine
 */
public interface PersistenceMapper<D, P> {

    /**
     * Converts a domain model to its persistence representation.
     *
     * @param domain the domain model, must not be {@code null}
     * @return the persistence model
     */
    P toPersistence(D domain);

    /**
     * Converts a persistence model back to its domain representation.
     *
     * <p>The reconstructed domain aggregate must be fully complete
     * and in a business-usable state (PERSIST-010, PERSIST-011).</p>
     *
     * @param persistence the persistence model, must not be {@code null}
     * @return the domain model
     */
    D toDomain(P persistence);
}
