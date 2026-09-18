package io.github.regalpine.ddd.transaction;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link TransactionDefinition} record and factory methods.
 */
class TransactionDefinitionTest {

    @Test
    void requiredShouldCreateReadWriteDefinition() {
        TransactionDefinition def = TransactionDefinition.required();

        assertThat(def.propagation()).isEqualTo(TransactionPropagation.REQUIRED);
        assertThat(def.isolation()).isEqualTo(TransactionIsolation.DEFAULT);
        assertThat(def.readOnly()).isFalse();
        assertThat(def.timeout()).isEqualTo(Duration.ZERO);
    }

    @Test
    void readOnlyDefinitionShouldCreateSupportsDefinition() {
        TransactionDefinition def = TransactionDefinition.readOnlyDefinition();

        assertThat(def.propagation()).isEqualTo(TransactionPropagation.SUPPORTS);
        assertThat(def.isolation()).isEqualTo(TransactionIsolation.DEFAULT);
        assertThat(def.readOnly()).isTrue();
        assertThat(def.timeout()).isEqualTo(Duration.ZERO);
    }

    @Test
    void ofWithPropagationShouldDefaultIsolationAndReadWrite() {
        TransactionDefinition def = TransactionDefinition.of(TransactionPropagation.REQUIRES_NEW);

        assertThat(def.propagation()).isEqualTo(TransactionPropagation.REQUIRES_NEW);
        assertThat(def.isolation()).isEqualTo(TransactionIsolation.DEFAULT);
        assertThat(def.readOnly()).isFalse();
    }

    @Test
    void ofWithTimeoutShouldSetTimeout() {
        TransactionDefinition def = TransactionDefinition.of(
                TransactionPropagation.REQUIRED, TransactionIsolation.DEFAULT, Duration.ofSeconds(30));

        assertThat(def.timeout()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void constructorShouldRejectNullPropagation() {
        assertThatThrownBy(() -> new TransactionDefinition(null, TransactionIsolation.DEFAULT, false, Duration.ZERO))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructorShouldRejectNullTimeout() {
        assertThatThrownBy(() -> new TransactionDefinition(TransactionPropagation.REQUIRED, TransactionIsolation.DEFAULT, false, null))
                .isInstanceOf(NullPointerException.class);
    }
}
