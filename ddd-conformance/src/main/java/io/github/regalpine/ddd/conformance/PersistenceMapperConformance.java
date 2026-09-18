package io.github.regalpine.ddd.conformance;

import io.github.regalpine.ddd.infrastructure.persistence.PersistenceMapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Conformance test suite for {@link PersistenceMapper} implementations.
 *
 * <p>Per Phase VII §90, mapping must not lose information during
 * domain-to-persistence and persistence-to-domain round-trips.</p>
 *
 * <p>Adapter implementations should extend this class and provide
 * their mapper instance via the abstract type parameters.</p>
 *
 * @param <D> the domain model type
 * @param <P> the persistence model type
 * @author RegalPine
 */
public abstract class PersistenceMapperConformance<D, P> {

    /**
     * Subclasses must provide the mapper under test.
     */
    protected abstract PersistenceMapper<D, P> mapper();

    /**
     * Subclasses must provide a sample domain model instance.
     */
    protected abstract D sampleDomain();

    /**
     * Subclasses must provide a sample persistence model instance.
     */
    protected abstract P samplePersistence();

    /**
     * Subclasses must implement equality check for domain models.
     */
    protected abstract void assertDomainEquals(D expected, D actual);

    /**
     * Subclasses must implement equality check for persistence models.
     */
    protected abstract void assertPersistenceEquals(P expected, P actual);

    /**
     * Verifies that toPersistence -> toDomain round-trip preserves all information.
     */
    @Test
    public void verifyDomainRoundTrip() {
        D original = sampleDomain();
        P persistence = mapper().toPersistence(original);
        D roundTripped = mapper().toDomain(persistence);
        assertDoesNotThrow(() -> assertDomainEquals(original, roundTripped),
                "Domain round-trip should preserve all information");
    }

    /**
     * Verifies that toDomain -> toPersistence round-trip preserves all information.
     */
    @Test
    public void verifyPersistenceRoundTrip() {
        P original = samplePersistence();
        D domain = mapper().toDomain(original);
        P roundTripped = mapper().toPersistence(domain);
        assertDoesNotThrow(() -> assertPersistenceEquals(original, roundTripped),
                "Persistence round-trip should preserve all information");
    }
}
