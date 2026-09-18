package io.github.regalpine.ddd.messaging;

import java.util.Objects;

/**
 * Defines a message contract including type, version, and schema compatibility.
 *
 * @author RegalPine
 */
public record MessageContract(
        String messageType,
        int schemaVersion,
        Compatibility compatibility
) {

    public MessageContract {
        Objects.requireNonNull(messageType, "messageType must not be null");
        Objects.requireNonNull(compatibility, "compatibility must not be null");
    }

    public static MessageContract of(String messageType, int schemaVersion, Compatibility compatibility) {
        return new MessageContract(messageType, schemaVersion, compatibility);
    }
}
