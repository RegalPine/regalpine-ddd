package io.github.regalpine.ddd.core.event;

import io.github.regalpine.ddd.core.identifier.Identifier;

/**
 * Unique identifier for a domain event.
 *
 * @param value the event ID string, must not be blank
 */
public record EventId(String value) implements Identifier {

    public EventId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Event ID must not be blank");
        }
    }

    /**
     * Factory method for creating an EventId.
     *
     * @param value the event ID string
     * @return a new EventId
     */
    public static EventId of(String value) {
        return new EventId(value);
    }
}
