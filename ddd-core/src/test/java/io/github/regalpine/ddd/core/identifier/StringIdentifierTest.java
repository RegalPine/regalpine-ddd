package io.github.regalpine.ddd.core.identifier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringIdentifierTest {

    @Test
    void shouldCreateValidIdentifier() {
        var id = new StringIdentifier("abc-123");
        assertThat(id.value()).isEqualTo("abc-123");
    }

    @Test
    void shouldSupportValueBasedEquality() {
        var id1 = new StringIdentifier("same");
        var id2 = new StringIdentifier("same");
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void shouldRejectNull() {
        assertThatThrownBy(() -> new StringIdentifier(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlank() {
        assertThatThrownBy(() -> new StringIdentifier("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectEmpty() {
        assertThatThrownBy(() -> new StringIdentifier(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
