package io.github.regalpine.ddd.cqrs.bus;

import io.github.regalpine.ddd.application.command.Command;
import io.github.regalpine.ddd.application.command.CommandHandler;
import io.github.regalpine.ddd.cqrs.middleware.InvocationContext;
import io.github.regalpine.ddd.cqrs.middleware.Middleware;
import io.github.regalpine.ddd.cqrs.middleware.MiddlewareChain;
import io.github.regalpine.ddd.cqrs.registry.CommandHandlerRegistry;

import java.util.List;
import java.util.Objects;

/**
 * Default {@link CommandBus} implementation with middleware pipeline support.
 * <p>
 * Dispatches commands to handlers resolved via {@link CommandHandlerRegistry},
 * passing through a configurable middleware chain.
 *
 * @author RegalPine
 */
public final class DefaultCommandBus implements CommandBus {

    private final CommandHandlerRegistry registry;
    private final List<Middleware<?>> middlewares;

    public DefaultCommandBus(CommandHandlerRegistry registry) {
        this(registry, List.of());
    }

    public DefaultCommandBus(CommandHandlerRegistry registry, List<Middleware<?>> middlewares) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.middlewares = List.copyOf(middlewares);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R dispatch(Command<R> command) {
        Objects.requireNonNull(command, "command must not be null");
        return dispatchCapture((Class<Command<R>>) command.getClass(), command);
    }

    /**
     * Capture helper that converts the wildcard class into a type variable
     * compatible with {@link CommandHandlerRegistry#find(Class)}.
     */
    private <C extends Command<R>, R> R dispatchCapture(Class<C> commandType, Command<R> command) {
        CommandHandler<C, R> handler = registry.find(commandType);

        InvocationContext<R> context = new InvocationContext<>(command, command.getClass().getCanonicalName());
        MiddlewareChain<R> chain = new MiddlewareChain<>((List<Middleware<R>>) (List<?>) middlewares);

        return chain.execute(context, () -> handler.handle((C) command));
    }
}
