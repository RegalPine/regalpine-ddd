package io.github.regalpine.ddd.messaging;

import java.util.Objects;

/**
 * Represents a messaging queue.
 *
 * @author RegalPine
 */
public record Queue(String name) {

    public Queue {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Queue name must not be blank");
        }
    }

    public static Queue of(String name) {
        return new Queue(name);
    }
}
