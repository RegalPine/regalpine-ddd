package io.github.regalpine.ddd.messaging;

/**
 * Serializes and deserializes message payloads.
 *
 * @author RegalPine
 */
public interface MessageSerializer {

    /**
     * Serializes a message payload to a byte array.
     *
     * @param payload the payload to serialize
     * @param <T>     the payload type
     * @return the serialized bytes
     */
    <T> byte[] serialize(T payload);

    /**
     * Deserializes a byte array to the specified type.
     *
     * @param data the serialized data
     * @param type the target type
     * @param <T>  the payload type
     * @return the deserialized payload
     */
    <T> T deserialize(byte[] data, Class<T> type);
}
