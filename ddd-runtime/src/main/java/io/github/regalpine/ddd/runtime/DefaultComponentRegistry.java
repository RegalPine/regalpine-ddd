package io.github.regalpine.ddd.runtime;

import io.github.regalpine.ddd.cqrs.bus.CommandBus;
import io.github.regalpine.ddd.cqrs.bus.QueryBus;
import io.github.regalpine.ddd.event.handler.DomainEventDispatcher;
import io.github.regalpine.ddd.messaging.MessagePublisher;
import io.github.regalpine.ddd.transaction.TransactionManager;
import io.github.regalpine.ddd.transaction.UnitOfWorkManager;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default in-memory implementation of {@link ComponentRegistry}.
 * <p>
 * Components are stored in a concurrent map keyed by type. Typed convenience
 * methods delegate to the generic register/find/require API.
 *
 * @author RegalPine
 */
public final class DefaultComponentRegistry implements ComponentRegistry, RuntimeContext {

    private final Map<Class<?>, Object> components = new ConcurrentHashMap<>();

    @Override
    public <T> void register(Class<T> type, T component) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(component, "component must not be null");
        components.put(type, component);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> find(Class<T> type) {
        return Optional.ofNullable((T) components.get(type));
    }

    @Override
    public <T> Optional<T> get(Class<T> type) {
        return find(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T require(Class<T> type) {
        T component = (T) components.get(type);
        if (component == null) {
            throw new NoSuchElementException("Component not registered: " + type.getName());
        }
        return component;
    }

    @Override
    public ComponentRegistry commandBus(CommandBus commandBus) {
        register(CommandBus.class, Objects.requireNonNull(commandBus, "commandBus must not be null"));
        return this;
    }

    @Override
    public ComponentRegistry queryBus(QueryBus queryBus) {
        register(QueryBus.class, Objects.requireNonNull(queryBus, "queryBus must not be null"));
        return this;
    }

    @Override
    public ComponentRegistry transactionManager(TransactionManager transactionManager) {
        register(TransactionManager.class, Objects.requireNonNull(transactionManager, "transactionManager must not be null"));
        return this;
    }

    @Override
    public ComponentRegistry unitOfWorkManager(UnitOfWorkManager unitOfWorkManager) {
        register(UnitOfWorkManager.class, Objects.requireNonNull(unitOfWorkManager, "unitOfWorkManager must not be null"));
        return this;
    }

    @Override
    public ComponentRegistry eventDispatcher(DomainEventDispatcher eventDispatcher) {
        register(DomainEventDispatcher.class, Objects.requireNonNull(eventDispatcher, "eventDispatcher must not be null"));
        return this;
    }

    @Override
    public ComponentRegistry messagePublisher(MessagePublisher messagePublisher) {
        register(MessagePublisher.class, Objects.requireNonNull(messagePublisher, "messagePublisher must not be null"));
        return this;
    }

    @Override
    public CommandBus commandBus() {
        return require(CommandBus.class);
    }

    @Override
    public QueryBus queryBus() {
        return require(QueryBus.class);
    }

    @Override
    public TransactionManager transactionManager() {
        return require(TransactionManager.class);
    }

    @Override
    public UnitOfWorkManager unitOfWorkManager() {
        return require(UnitOfWorkManager.class);
    }

    @Override
    public DomainEventDispatcher eventDispatcher() {
        return require(DomainEventDispatcher.class);
    }

    @Override
    public MessagePublisher messagePublisher() {
        return require(MessagePublisher.class);
    }

    @Override
    public boolean hasMessagePublisher() {
        return find(MessagePublisher.class).isPresent();
    }
}
