package io.github.regalpine.ddd.conformance;

import io.github.regalpine.ddd.core.aggregate.AggregateRoot;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.domain.repository.AggregateRepository;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Conformance test suite for {@link AggregateRepository} implementations.
 *
 * <p>Phase X §44: Any Repository Adapter must pass save, find, delete,
 * identity, and version tests.</p>
 *
 * <p>Subclasses must provide concrete test aggregate and identifier factories.
 * Uses raw types internally to allow testing any aggregate repository regardless
 * of its specific generic parameters.</p>
 *
 * @author RegalPine
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class RepositoryConformance {

    /**
     * Subclasses must provide the repository under test.
     */
    protected abstract AggregateRepository repository();

    /**
     * Subclasses must provide a test aggregate with the given identifier.
     */
    protected abstract AggregateRoot<?> createAggregate(String id);

    /**
     * Subclasses must provide the identifier for lookups.
     */
    protected abstract Identifier identifier(String value);

    /**
     * Verifies save and find behavior.
     */
    @Test
    public void verifySaveAndFind() {
        String id = "test-" + System.nanoTime();
        AggregateRoot aggregate = createAggregate(id);

        repository().save(aggregate);

        Optional found = repository().findById(identifier(id));
        assertTrue(found.isPresent(), "Should find aggregate after save");
    }

    /**
     * Verifies find returns empty for unknown identifier.
     */
    @Test
    public void verifyFindUnknown() {
        Optional found = repository().findById(identifier("non-existent-" + System.nanoTime()));
        assertTrue(found.isEmpty(), "Should not find aggregate for unknown identifier");
    }

    /**
     * Verifies delete behavior.
     */
    @Test
    public void verifyDelete() {
        String id = "test-del-" + System.nanoTime();
        AggregateRoot aggregate = createAggregate(id);

        repository().save(aggregate);
        repository().delete(aggregate);

        Optional found = repository().findById(identifier(id));
        assertTrue(found.isEmpty(), "Should not find aggregate after delete");
    }

    /**
     * Verifies identity preservation (same id in, same id out).
     */
    @Test
    public void verifyIdentityPreservation() {
        String id = "test-id-" + System.nanoTime();
        AggregateRoot aggregate = createAggregate(id);

        repository().save(aggregate);

        Optional found = repository().findById(identifier(id));
        assertTrue(found.isPresent(), "Should find aggregate");
        AggregateRoot<?> foundAggregate = (AggregateRoot<?>) found.get();
        assertEquals(identifier(id), foundAggregate.id(),
                "Identity should be preserved after save and find");
    }
}
