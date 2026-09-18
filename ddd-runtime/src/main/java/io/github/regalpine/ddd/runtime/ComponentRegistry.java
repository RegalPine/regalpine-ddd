package io.github.regalpine.ddd.runtime;

import io.github.regalpine.ddd.cqrs.bus.CommandBus;
import io.github.regalpine.ddd.cqrs.bus.QueryBus;
import io.github.regalpine.ddd.event.handler.DomainEventDispatcher;
import io.github.regalpine.ddd.messaging.MessagePublisher;
import io.github.regalpine.ddd.transaction.TransactionManager;
import io.github.regalpine.ddd.transaction.UnitOfWorkManager;

import java.util.Optional;

/**
 * Central registry holding all runtime components.
 * <p>
 * Supports both generic type-safe registration and typed convenience
 * accessors for well-known framework components.
 *
 * @author RegalPine
 */
public interface ComponentRegistry {

    /**
     * Registers a component by type.
     *
     * @param type      the component type
     * @param component the component instance
     * @param <T>       the type parameter
     */
    <T> void register(Class<T> type, T component);

    /**
     * Looks up a component by type.
     *
     * @param type the component type
     * @param <T>  the type parameter
     * @return an optional containing the component, or empty if not registered
     */
    <T> Optional<T> find(Class<T> type);

    /**
     * Looks up a required component by type.
     *
     * @param type the component type
     * @param <T>  the type parameter
     * @return the component instance
     * @throws java.util.NoSuchElementException if not registered
     */
    <T> T require(Class<T> type);

    // --- Typed convenience methods for well-known components ---

    ComponentRegistry commandBus(CommandBus commandBus);

    ComponentRegistry queryBus(QueryBus queryBus);

    ComponentRegistry transactionManager(TransactionManager transactionManager);

    ComponentRegistry unitOfWorkManager(UnitOfWorkManager unitOfWorkManager);

    ComponentRegistry eventDispatcher(DomainEventDispatcher eventDispatcher);

    ComponentRegistry messagePublisher(MessagePublisher messagePublisher);

    CommandBus commandBus();

    QueryBus queryBus();

    TransactionManager transactionManager();

    UnitOfWorkManager unitOfWorkManager();

    DomainEventDispatcher eventDispatcher();

    MessagePublisher messagePublisher();

    boolean hasMessagePublisher();
}
