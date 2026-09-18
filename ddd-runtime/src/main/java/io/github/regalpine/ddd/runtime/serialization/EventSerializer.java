package io.github.regalpine.ddd.runtime.serialization;

/**
 * Serializer/deserializer for integration event payloads.
 *
 * <p>Converts between Java objects and byte arrays for event storage
 * and message broker transmission. Implementations must support
 * event type and version metadata for schema evolution.</p>
 *
 * @author RegalPine
 */
public interface EventSerializer {

    /**
     * Serializes the given value to a byte array.
     *
     * @param value the object to serialize
     * @return the serialized byte array
     */
    byte[] serialize(Object value);

    /**
     * Deserializes the given byte array to an instance of the specified type.
     *
     * @param payload the byte array to deserialize
     * @param type    the target type
     * @param <T>     the target type parameter
     * @return the deserialized instance
     */
    <T> T deserialize(byte[] payload, Class<T> type);
}
