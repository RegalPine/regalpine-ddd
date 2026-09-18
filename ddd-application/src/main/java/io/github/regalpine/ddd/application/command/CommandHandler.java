package io.github.regalpine.ddd.application.command;

/**
 * Handles a specific type of command.
 *
 * <p>A CommandHandler is an orchestrator: it loads aggregates,
 * invokes domain behavior, saves aggregates, and returns results.
 * It must NOT contain business rules — those belong in the domain.</p>
 *
 * @param <C> the command type
 * @param <R> the result type
 */
public interface CommandHandler<C extends Command<R>, R> {

    /**
     * Handles the given command.
     *
     * @param command the command to handle
     * @return the result of handling
     */
    R handle(C command);
}
