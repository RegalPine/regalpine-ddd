package io.github.regalpine.ddd.cqrs.registry;

import io.github.regalpine.ddd.application.command.Command;
import io.github.regalpine.ddd.application.command.CommandHandler;

/**
 * Registry for command handlers.
 * <p>
 * Enforces one handler per command type. Registering a duplicate throws {@link
 * io.github.regalpine.ddd.cqrs.bus.DuplicateHandlerException}.
 *
 * @author RegalPine
 */
public interface CommandHandlerRegistry {

    /**
     * Registers a handler for the command type extracted from the handler's generic parameter.
     *
     * @param handler the command handler
     * @param <C>     the command type
     * @param <R>     the result type
     * @throws io.github.regalpine.ddd.cqrs.bus.DuplicateHandlerException if a handler is already registered
     */
    <C extends Command<R>, R> void register(CommandHandler<C, R> handler);

    /**
     * Finds the handler for the given command type.
     *
     * @param commandType the command class
     * @param <C>         the command type
     * @param <R>         the result type
     * @return the registered handler
     * @throws io.github.regalpine.ddd.cqrs.bus.HandlerNotFoundException if no handler is found
     */
    <C extends Command<R>, R> CommandHandler<C, R> find(Class<C> commandType);
}
