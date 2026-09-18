package io.github.regalpine.ddd.cqrs.registry;

import io.github.regalpine.ddd.application.command.Command;
import io.github.regalpine.ddd.application.command.CommandHandler;
import io.github.regalpine.ddd.application.query.Query;
import io.github.regalpine.ddd.application.query.QueryHandler;

/**
 * Unified registry for both command and query handlers.
 *
 * <p>Phase X §17: HandlerRegistry provides a single registration point
 * for all CQRS handlers. Registry must be completed during Runtime Bootstrap.</p>
 *
 * <p>This interface delegates to {@link CommandHandlerRegistry} and
 * {@link QueryHandlerRegistry} internally, maintaining backward compatibility
 * with the separate registries.</p>
 *
 * @author RegalPine
 */
public interface HandlerRegistry {

    /**
     * Registers a command handler for the given command type.
     *
     * @param commandType the command class
     * @param handler     the command handler
     * @param <C>         the command type
     * @param <R>         the result type
     */
    <C extends Command<R>, R> void registerCommand(Class<C> commandType, CommandHandler<C, R> handler);

    /**
     * Registers a query handler for the given query type.
     *
     * @param queryType the query class
     * @param handler   the query handler
     * @param <Q>       the query type
     * @param <R>       the result type
     */
    <Q extends Query<R>, R> void registerQuery(Class<Q> queryType, QueryHandler<Q, R> handler);
}
