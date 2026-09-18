package io.github.regalpine.ddd.event.handler;

import io.github.regalpine.ddd.core.event.DomainEvent;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default in-memory implementation of {@link DomainEventDispatcher}.
 *
 * <p>Resolves event types from handler generic parameters and dispatches
 * individual events by type.</p>
 *
 * @author RegalPine
 */
public final class DefaultDomainEventDispatcher implements DomainEventDispatcher {

    private final Map<Class<?>, List<DomainEventHandler<?>>> handlerMap = new ConcurrentHashMap<>();

    public DefaultDomainEventDispatcher() {
    }

    public DefaultDomainEventDispatcher(List<DomainEventHandler<?>> handlers) {
        Objects.requireNonNull(handlers, "handlers must not be null");
        handlers.forEach(this::register);
    }

    /**
     * Registers a domain event handler.
     */
    public void register(DomainEventHandler<?> handler) {
        Objects.requireNonNull(handler, "handler must not be null");
        Class<?> eventType = resolveEventType(handler);
        handlerMap.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void dispatch(DomainEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        List<DomainEventHandler<?>> handlers = handlerMap.get(event.getClass());
        if (handlers != null) {
            for (DomainEventHandler<?> handler : handlers) {
                ((DomainEventHandler<DomainEvent>) handler).handle(event);
            }
        }
    }

    private Class<?> resolveEventType(DomainEventHandler<?> handler) {
        for (Type iface : handler.getClass().getGenericInterfaces()) {
            if (iface instanceof ParameterizedType pt
                    && pt.getRawType() == DomainEventHandler.class) {
                Type eventTypeArg = pt.getActualTypeArguments()[0];
                if (eventTypeArg instanceof Class<?> clazz) {
                    return clazz;
                }
            }
        }
        throw new IllegalArgumentException(
                "Cannot resolve event type from handler: " + handler.getClass().getCanonicalName());
    }
}
