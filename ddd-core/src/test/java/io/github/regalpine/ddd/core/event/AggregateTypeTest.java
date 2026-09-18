package io.github.regalpine.ddd.core.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AggregateTypeTest {

    @Test
    void shouldCreateAggregateTypeWithValidValue() {
        var type = new AggregateType("order");
        assertThat(type.value()).isEqualTo("order");
    }

    @Test
    void shouldCreateViaFactoryMethod() {
        var type = AggregateType.of("customer");
        assertThat(type.value()).isEqualTo("customer");
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> new AggregateType(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(() -> new AggregateType(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldSupportValueEquality() {
        assertThat(AggregateType.of("order")).isEqualTo(AggregateType.of("order"));
    }
}
