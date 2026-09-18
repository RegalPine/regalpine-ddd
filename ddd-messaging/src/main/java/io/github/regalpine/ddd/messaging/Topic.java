package io.github.regalpine.ddd.messaging;

import java.util.Objects;

/**
 * Represents a messaging topic.
 *
 * @author RegalPine
 */
public record Topic(String name) {

    public Topic {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Topic name must not be blank");
        }
    }

    public static Topic of(String name) {
        return new Topic(name);
    }
}
