package io.github.regalpine.ddd.cqrs.registry;

import io.github.regalpine.ddd.application.command.Command;
import io.github.regalpine.ddd.application.command.CommandHandler;
import io.github.regalpine.ddd.cqrs.bus.DuplicateHandlerException;
import io.github.regalpine.ddd.cqrs.bus.HandlerNotFoundException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default in-memory implementation of {@link CommandHandlerRegistry}.
 * <p>
 * Uses a {@link ConcurrentHashMap} for thread-safe handler storage.
 * Handler command types are resolved via reflection on the handler's generic interfaces.
 *
 * @author RegalPine
 */
public final class DefaultCommandHandlerRegistry implements CommandHandlerRegistry {

    private final Map<Class<?>, CommandHandler<?, ?>> handlers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <C extends Command<R>, R> void register(CommandHandler<C, R> handler) {
        Objects.requireNonNull(handler, "handler must not be null");
        Class<C> commandType = resolveCommandType(handler);
        CommandHandler<?, ?> existing = handlers.putIfAbsent(commandType, handler);
        if (existing != null) {
            throw new DuplicateHandlerException(commandType.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends Command<R>, R> CommandHandler<C, R> find(Class<C> commandType) {
        Objects.requireNonNull(commandType, "commandType must not be null");
        CommandHandler<?, ?> handler = handlers.get(commandType);
        if (handler == null) {
            throw new HandlerNotFoundException(commandType.getCanonicalName());
        }
        return (CommandHandler<C, R>) handler;
    }

    /**
     * Resolves the command type parameter from the handler's generic interfaces.
     */
    private <C extends Command<R>, R> Class<C> resolveCommandType(CommandHandler<C, R> handler) {
        for (Type iface : handler.getClass().getGenericInterfaces()) {
            if (iface instanceof ParameterizedType pt
                    && pt.getRawType() == CommandHandler.class) {
                Type commandTypeArg = pt.getActualTypeArguments()[0];
                if (commandTypeArg instanceof Class<?> clazz) {
                    @SuppressWarnings("unchecked")
                    Class<C> commandClass = (Class<C>) clazz;
                    return commandClass;
                }
            }
        }
        throw new IllegalArgumentException(
                "Cannot resolve command type from handler: " + handler.getClass().getCanonicalName()
                        + ". Ensure the handler implements CommandHandler with concrete type parameters.");
    }
}
