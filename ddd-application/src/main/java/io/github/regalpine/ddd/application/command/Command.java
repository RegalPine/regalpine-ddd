package io.github.regalpine.ddd.application.command;

/**
 * Represents a business intent requesting the system to perform an action.
 *
 * <p>A Command is an immutable request object. It expresses intent,
 * not database operations. Commands are handled by a
 * {@link CommandHandler}.</p>
 *
 * <p>Recommended implementation: Java {@code record}.</p>
 *
 * @param <R> the result type
 */
public interface Command<R> {
}
