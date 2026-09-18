package io.github.regalpine.ddd.messaging.schema;

import java.util.Objects;

/**
 * Defines a message schema registered in the schema registry.
 *
 * <p>Phase IX §48: SchemaDefinition captures the message type, version,
 * content type, and the schema definition itself.</p>
 *
 * @param messageType the message type name
 * @param version     the schema version
 * @param contentType the content type (e.g. "application/json")
 * @param definition  the schema definition bytes
 * @author RegalPine
 */
public record SchemaDefinition(
        String messageType,
        int version,
        String contentType,
        byte[] definition
) {

    public SchemaDefinition {
        Objects.requireNonNull(messageType, "messageType must not be null");
        Objects.requireNonNull(contentType, "contentType must not be null");
        Objects.requireNonNull(definition, "definition must not be null");
    }
}
