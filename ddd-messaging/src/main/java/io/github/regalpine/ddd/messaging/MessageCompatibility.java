package io.github.regalpine.ddd.messaging;

/**
 * Determines whether a consumer supports a given message type and schema version.
 *
 * <p>Phase IX §47: Consumer must declare supported message compatibility.
 * Unsupported messages must not enter business processing.</p>
 *
 * @author RegalPine
 */
public interface MessageCompatibility {

    /**
     * Checks whether the consumer supports the given message type at the given schema version.
     *
     * @param messageType   the message type name
     * @param schemaVersion the schema version
     * @return true if the consumer can handle this message
     */
    boolean supports(String messageType, int schemaVersion);
}
