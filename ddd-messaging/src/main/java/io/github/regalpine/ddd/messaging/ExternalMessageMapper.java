package io.github.regalpine.ddd.messaging;

/**
 * Translates external system messages into internal concepts.
 *
 * <p>Phase IX §77: External messages must pass through an Anti-Corruption Layer
 * before entering the domain. This mapper provides the translation contract.</p>
 *
 * <p>Example: SAP Order → ExternalOrderMapper → OrderImportCommand.</p>
 *
 * @param <E> the external message type
 * @param <I> the internal concept type (command, event, etc.)
 * @author RegalPine
 */
public interface ExternalMessageMapper<E, I> {

    /**
     * Translates an external message into an internal concept.
     *
     * @param externalMessage the external message
     * @return the translated internal concept
     */
    I translate(E externalMessage);
}
