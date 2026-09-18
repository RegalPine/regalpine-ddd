package io.github.regalpine.ddd.cqrs.registry;

import io.github.regalpine.ddd.application.command.Command;
import io.github.regalpine.ddd.application.command.CommandHandler;
import io.github.regalpine.ddd.application.query.Query;
import io.github.regalpine.ddd.application.query.QueryHandler;

import java.util.Objects;

/**
 * Default implementation of {@link HandlerRegistry} that delegates to
 * {@link CommandHandlerRegistry} and {@link QueryHandlerRegistry}.
 *
 * <p>Phase X §17: Provides a unified registration facade while preserving
 * the existing separate registries for backward compatibility.</p>
 *
 * @author RegalPine
 */
public final class DefaultHandlerRegistry implements HandlerRegistry {

    private final CommandHandlerRegistry commandRegistry;
    private final QueryHandlerRegistry queryRegistry;

    public DefaultHandlerRegistry(CommandHandlerRegistry commandRegistry, QueryHandlerRegistry queryRegistry) {
        this.commandRegistry = Objects.requireNonNull(commandRegistry, "commandRegistry must not be null");
        this.queryRegistry = Objects.requireNonNull(queryRegistry, "queryRegistry must not be null");
    }

    @Override
    public <C extends Command<R>, R> void registerCommand(Class<C> commandType, CommandHandler<C, R> handler) {
        Objects.requireNonNull(commandType, "commandType must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        commandRegistry.register(handler);
    }

    @Override
    public <Q extends Query<R>, R> void registerQuery(Class<Q> queryType, QueryHandler<Q, R> handler) {
        Objects.requireNonNull(queryType, "queryType must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        queryRegistry.register(handler);
    }
}
