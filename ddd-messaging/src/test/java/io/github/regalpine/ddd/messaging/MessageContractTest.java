package io.github.regalpine.ddd.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MessageContractTest {

    @Test
    void shouldCreateContract() {
        MessageContract contract = MessageContract.of("order.created", 1, Compatibility.BACKWARD);

        assertThat(contract.messageType()).isEqualTo("order.created");
        assertThat(contract.schemaVersion()).isEqualTo(1);
        assertThat(contract.compatibility()).isEqualTo(Compatibility.BACKWARD);
    }

    @Test
    void shouldRejectNullMessageType() {
        assertThatThrownBy(() -> new MessageContract(null, 1, Compatibility.FULL))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void allCompatibilityValuesShouldExist() {
        assertThat(Compatibility.values()).containsExactly(
                Compatibility.BACKWARD, Compatibility.FORWARD,
                Compatibility.FULL, Compatibility.NONE);
    }
}
