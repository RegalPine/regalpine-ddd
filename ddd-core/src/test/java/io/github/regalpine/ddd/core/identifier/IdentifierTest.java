package io.github.regalpine.ddd.core.identifier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the {@link Identifier} contract.
 *
 * <p>Per Phase II §6–§8, Identifier must be immutable, use value equality,
 * and support typed identifiers to prevent type confusion.</p>
 */
class IdentifierTest {

    // -- Test fixtures --

    record OrderId(String value) implements Identifier {
        public OrderId {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Identifier value must not be blank");
            }
        }
    }

    record CustomerId(String value) implements Identifier {
        public CustomerId {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Identifier value must not be blank");
            }
        }
    }

    // -- Tests --

    @Test
    void shouldReturnValue() {
        Identifier id = new OrderId("001");
        assertThat(id.value()).isEqualTo("001");
    }

    @Test
    void shouldBeValueEqual() {
        Identifier a = new OrderId("001");
        Identifier b = new OrderId("001");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void shouldNotEqualDifferentValue() {
        Identifier a = new OrderId("001");
        Identifier b = new OrderId("002");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void shouldNotEqualDifferentType() {
        // Per §7: OrderId("001") ≠ CustomerId("001")
        Identifier orderId = new OrderId("001");
        Identifier customerId = new CustomerId("001");
        assertThat(orderId).isNotEqualTo(customerId);
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> new OrderId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(() -> new OrderId("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
