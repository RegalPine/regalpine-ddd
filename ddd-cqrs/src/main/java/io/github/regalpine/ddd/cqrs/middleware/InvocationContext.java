package io.github.regalpine.ddd.cqrs.middleware;

import java.util.Objects;

/**
 * Context for a middleware invocation in the command/query pipeline.
 *
 * @param <R> the result type
 * @author RegalPine
 */
public final class InvocationContext<R> {

    private final Object message;
    private final String messageType;

    public InvocationContext(Object message, String messageType) {
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.messageType = Objects.requireNonNull(messageType, "messageType must not be null");
    }

    /**
     * Returns the command or query being dispatched.
     */
    public Object message() {
        return message;
    }

    /**
     * Returns the canonical class name of the message type.
     */
    public String messageType() {
        return messageType;
    }
}
