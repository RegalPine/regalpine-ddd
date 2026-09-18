package io.github.regalpine.ddd.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link PersistenceMapper} contract (Phase VII §10, PERSIST-006).
 */
@DisplayName("PersistenceMapper contract")
class PersistenceMapperTest {

    record DomainModel(String id, String name, int value) {}
    record PersistenceModel(String id, String name, int value) {}

    private final PersistenceMapper<DomainModel, PersistenceModel> mapper = new PersistenceMapper<>() {
        @Override
        public PersistenceModel toPersistence(DomainModel domain) {
            return new PersistenceModel(domain.id(), domain.name(), domain.value());
        }

        @Override
        public DomainModel toDomain(PersistenceModel persistence) {
            return new DomainModel(persistence.id(), persistence.name(), persistence.value());
        }
    };

    @Test
    @DisplayName("toPersistence converts domain to persistence model")
    void toPersistenceConverts() {
        var domain = new DomainModel("id-1", "test", 42);
        var persistence = mapper.toPersistence(domain);

        assertEquals("id-1", persistence.id());
        assertEquals("test", persistence.name());
        assertEquals(42, persistence.value());
    }

    @Test
    @DisplayName("toDomain converts persistence to domain model")
    void toDomainConverts() {
        var persistence = new PersistenceModel("id-2", "name", 99);
        var domain = mapper.toDomain(persistence);

        assertEquals("id-2", domain.id());
        assertEquals("name", domain.name());
        assertEquals(99, domain.value());
    }

    @Test
    @DisplayName("round-trip preserves all fields")
    void roundTripPreservesFields() {
        var original = new DomainModel("id-3", "round-trip", 7);
        var roundTripped = mapper.toDomain(mapper.toPersistence(original));

        assertEquals(original, roundTripped);
    }
}
