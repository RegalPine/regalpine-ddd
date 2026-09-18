package io.github.regalpine.ddd.messaging.schema;

/**
 * Optional registry for message schemas.
 *
 * <p>Phase IX §48: Schema Registry is an optional infrastructure capability.
 * It belongs to the Integration Layer, not the Domain Layer (§49).</p>
 *
 * <p>Phase IX MSG-SEC-007: Schema Registry must be subject to change permission control.</p>
 *
 * @author RegalPine
 */
public interface SchemaRegistry {

    /**
     * Retrieves a schema definition by message type and version.
     *
     * @param messageType the message type name
     * @param version     the schema version
     * @return the schema definition, or null if not found
     */
    SchemaDefinition get(String messageType, int version);

    /**
     * Registers a new schema definition.
     *
     * @param schema the schema definition to register
     */
    void register(SchemaDefinition schema);
}
