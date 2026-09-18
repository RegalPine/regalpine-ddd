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
import io.github.regalpine.ddd.event.outbox.LeasedOutboxStore;
import io.github.regalpine.ddd.infrastructure.transaction.InMemoryTransactionManager;
import io.github.regalpine.ddd.infrastructure.transaction.InMemoryUnitOfWorkManager;
import io.github.regalpine.ddd.messaging.BrokerAdapter;
import io.github.regalpine.ddd.runtime.ComponentRegistry;
import io.github.regalpine.ddd.runtime.DefaultComponentRegistry;
import io.github.regalpine.ddd.runtime.DddRuntime;
import io.github.regalpine.ddd.runtime.DddRuntimeBuilder;
import io.github.regalpine.ddd.runtime.RuntimeComponent;
import io.github.regalpine.ddd.runtime.RuntimeConfig;
import io.github.regalpine.ddd.runtime.TransactionalCommandBus;
import io.github.regalpine.ddd.runtime.messaging.OutboxWorkerComponent;
import io.github.regalpine.ddd.transaction.TransactionManager;
import io.github.regalpine.ddd.transaction.UnitOfWorkManager;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.List;

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

    /**
     * 事务性 CommandBus 装饰器。
     *
     * <p>当存在 TransactionManager 时，自动将 CommandBus 包装为事务性。</p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "transactionalCommandBus")
    public CommandBus transactionalCommandBus(CommandBus commandBus, TransactionManager transactionManager) {
        return new TransactionalCommandBus(commandBus, transactionManager);
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

    /**
     * Outbox Worker 组件。
     *
     * <p>当配置了 ddd.outbox.enabled=true 且存在必要的 Bean 时启用。</p>
     */
    @Bean
    @ConditionalOnProperty(name = "ddd.outbox.enabled", havingValue = "true")
    @ConditionalOnClass({LeasedOutboxStore.class, BrokerAdapter.class})
    public RuntimeComponent outboxWorkerComponent(
            LeasedOutboxStore outboxStore,
            BrokerAdapter brokerAdapter,
            TransactionManager transactionManager,
            ObjectProvider<OutboxWorkerProperties> propertiesProvider) {
        OutboxWorkerProperties props = propertiesProvider.getIfAvailable(OutboxWorkerProperties::new);
        return new OutboxWorkerComponent(
                outboxStore,
                brokerAdapter,
                transactionManager,
                props.lease(),
                props.pollInterval()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public DddRuntime dddRuntime(ComponentRegistry componentRegistry, RuntimeConfig config,
                                  ObjectProvider<List<RuntimeComponent>> componentsProvider) {
        DddRuntimeBuilder builder = DddRuntime.builder()
                .componentRegistry(componentRegistry)
                .config(config);
        List<RuntimeComponent> components = componentsProvider.getIfAvailable();
        if (components != null) {
            for (RuntimeComponent component : components) {
                builder.register(component);
            }
        }
        DddRuntime runtime = builder.build();
        runtime.start();
        return runtime;
    }
}
