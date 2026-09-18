package io.github.regalpine.ddd.application.idempotency;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyStoreTest {

    // -- Test fixtures --

    static final class InMemoryIdempotencyStore implements IdempotencyStore {
        private final Map<String, IdempotencyResult> store = new ConcurrentHashMap<>();

        @Override
        public Optional<IdempotencyResult> find(String key) {
            return Optional.ofNullable(store.get(key));
        }

        @Override
        public void store(String key, IdempotencyResult result) {
            store.put(key, result);
        }
    }

    // -- Tests --

    @Test
    void shouldReturnEmptyWhenNotFound() {
        IdempotencyStore store = new InMemoryIdempotencyStore();
        assertThat(store.find("non-existent")).isEmpty();
    }

    @Test
    void shouldStoreAndFindResult() {
        IdempotencyStore store = new InMemoryIdempotencyStore();
        store.store("cmd-1", IdempotencyResult.alreadyProcessed("result-1"));

        var found = store.find("cmd-1");
        assertThat(found).isPresent();
        assertThat(found.get().alreadyProcessed()).isTrue();
        assertThat(found.get().previousResult()).isEqualTo("result-1");
    }

    @Test
    void shouldStoreNotProcessed() {
        IdempotencyResult result = IdempotencyResult.notProcessed();
        assertThat(result.alreadyProcessed()).isFalse();
        assertThat(result.previousResult()).isNull();
    }

    @Test
    void shouldCreateAlreadyProcessedResult() {
        IdempotencyResult result = IdempotencyResult.alreadyProcessed("previous");
        assertThat(result.alreadyProcessed()).isTrue();
        assertThat(result.previousResult()).isEqualTo("previous");
    }

    @Test
    void shouldSupportRecordEquality() {
        var r1 = new IdempotencyResult(true, "data");
        var r2 = new IdempotencyResult(true, "data");
        assertThat(r1).isEqualTo(r2);
    }
}
