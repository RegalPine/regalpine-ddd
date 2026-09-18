package io.github.regalpine.ddd.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link PersistenceAdapter} SPI contract (Phase VII §82).
 */
@DisplayName("PersistenceAdapter SPI contract")
class PersistenceAdapterTest {

    private final Map<String, String> store = new HashMap<>();

    private final PersistenceAdapter<String, String, String> adapter = new PersistenceAdapter<>() {
        @Override
        public Optional<String> load(String id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public void insert(String aggregate) {
            store.put(aggregate, aggregate);
        }

        @Override
        public void update(String aggregate) {
            if (!store.containsKey(aggregate)) {
                throw new IllegalStateException("not found");
            }
            store.put(aggregate, aggregate);
        }

        @Override
        public void delete(String aggregate) {
            store.remove(aggregate);
        }
    };

    @Test
    @DisplayName("load returns empty for unknown id")
    void loadReturnsEmptyForUnknown() {
        assertTrue(adapter.load("non-existent").isEmpty());
    }

    @Test
    @DisplayName("insert and load round-trip")
    void insertAndLoad() {
        adapter.insert("aggregate-1");
        assertEquals(Optional.of("aggregate-1"), adapter.load("aggregate-1"));
    }

    @Test
    @DisplayName("delete removes aggregate")
    void deleteRemoves() {
        adapter.insert("aggregate-2");
        adapter.delete("aggregate-2");
        assertTrue(adapter.load("aggregate-2").isEmpty());
    }
}
