package io.github.regalpine.ddd.spring;

import io.github.regalpine.ddd.cqrs.bus.CommandBus;
import io.github.regalpine.ddd.cqrs.bus.DefaultCommandBus;
import io.github.regalpine.ddd.cqrs.bus.DefaultQueryBus;
import io.github.regalpine.ddd.cqrs.bus.QueryBus;
import io.github.regalpine.ddd.cqrs.registry.CommandHandlerRegistry;
import io.github.regalpine.ddd.cqrs.registry.DefaultCommandHandlerRegistry;
import io.github.regalpine.ddd.cqrs.registry.DefaultQueryHandlerRegistry;
import io.github.regalpine.ddd.cqrs.registry.QueryHandlerRegistry;
import io.github.regalpine.ddd.event.handler.DefaultDomainEventDispatcher;
import io.github.regalpine.ddd.event.handler.DomainEventDispatcher;
import io.github.regalpine.ddd.infrastructure.transaction.InMemoryTransactionManager;
import io.github.regalpine.ddd.infrastructure.transaction.InMemoryUnitOfWorkManager;
import io.github.regalpine.ddd.runtime.ComponentRegistry;
import io.github.regalpine.ddd.runtime.DefaultComponentRegistry;
import io.github.regalpine.ddd.runtime.DddRuntime;
import io.github.regalpine.ddd.runtime.RuntimeConfig;
import io.github.regalpine.ddd.transaction.TransactionManager;
import io.github.regalpine.ddd.transaction.UnitOfWorkManager;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the RegalPine DDD framework.
 * <p>
 * Provides default beans for all core DDD components.
 * Users can override any bean by providing their own implementation.
 *
 * @author RegalPine
 */
@AutoConfiguration
public class DddAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CommandHandlerRegistry commandHandlerRegistry() {
        return new DefaultCommandHandlerRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryHandlerRegistry queryHandlerRegistry() {
        return new DefaultQueryHandlerRegistry();
    }

    @Bean
    @ConditionalOnMissingBean(CommandBus.class)
    public CommandBus commandBus(CommandHandlerRegistry registry) {
        return new DefaultCommandBus(registry);
    }

    @Bean
    @ConditionalOnMissingBean(QueryBus.class)
    public QueryBus queryBus(QueryHandlerRegistry registry) {
        return new DefaultQueryBus(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public DomainEventDispatcher domainEventDispatcher() {
        return new DefaultDomainEventDispatcher();
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionManager transactionManager() {
        return new InMemoryTransactionManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public UnitOfWorkManager unitOfWorkManager() {
        return new InMemoryUnitOfWorkManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public RuntimeConfig runtimeConfig() {
        return RuntimeConfig.DEFAULT;
    }

    @Bean
    @ConditionalOnMissingBean
    public ComponentRegistry componentRegistry(
            CommandBus commandBus,
            QueryBus queryBus,
            TransactionManager transactionManager,
            UnitOfWorkManager unitOfWorkManager,
            DomainEventDispatcher eventDispatcher) {
        return new DefaultComponentRegistry()
                .commandBus(commandBus)
                .queryBus(queryBus)
                .transactionManager(transactionManager)
                .unitOfWorkManager(unitOfWorkManager)
                .eventDispatcher(eventDispatcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public DddRuntime dddRuntime(ComponentRegistry componentRegistry, RuntimeConfig config) {
        DddRuntime runtime = DddRuntime.builder()
                .componentRegistry(componentRegistry)
                .config(config)
                .build();
        runtime.start();
        return runtime;
    }
}
