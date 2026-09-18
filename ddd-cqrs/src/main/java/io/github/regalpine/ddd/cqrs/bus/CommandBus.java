package io.github.regalpine.ddd.cqrs.bus;

import io.github.regalpine.ddd.application.command.Command;

/**
 * Command bus that dispatches commands to their registered handlers.
 * <p>
 * Each command type must have exactly one handler. Duplicate handlers cause startup failure.
 *
 * @author RegalPine
 */
public interface CommandBus {

    /**
     * Dispatches a command to its registered handler.
     *
     * @param command the command to dispatch
     * @param <R>     the result type
     * @return the handler result
     * @throws HandlerNotFoundException  if no handler is registered for the command type
     * @throws DuplicateHandlerException if multiple handlers are registered for the same command type
     */
    <R> R dispatch(Command<R> command);
}
