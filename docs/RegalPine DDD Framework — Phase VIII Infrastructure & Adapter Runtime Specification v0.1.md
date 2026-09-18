# RegalPine DDD Framework

# Phase VIII — Infrastructure & Adapter Runtime Specification

**Version:** v0.1  
**Group ID:** `io.github.regalpine.ddd`  
**Java:** 17+  
**Build:** Maven  
**Architecture:** DDD + Hexagonal Architecture + CQRS  
**Status:** Framework Specification  
**Scope:** Infrastructure Runtime / Adapter / Bootstrap / Lifecycle / Repository Factory / Transaction Runtime / Event Runtime / Configuration / Observability

---

## 1. 文档定位

Phase VIII 的目标不是继续增加 DDD 理论抽象，而是解决：

> **如何将 Phase I–VII 已定义的 Framework Contract 组装成为一个可以运行的 Java Framework。**

Phase VIII 不重新定义：

- Entity
- Value Object
- Aggregate
- Domain Event
- Repository Contract
- Command
- Query
- CommandBus
- QueryBus
- Transaction
- UnitOfWork
- CQRS
- Outbox
- Inbox

这些已经在前序阶段定义。

本阶段定义：

```text
Contract
   ↓
Runtime
   ↓
Adapter
   ↓
Infrastructure
   ↓
Application Runtime
```

---

# 2. Phase VIII 核心目标

Phase VIII 必须解决以下问题：

1. Runtime 如何启动
2. Framework Component 如何注册
3. Repository 如何创建
4. Transaction 如何执行
5. UnitOfWork 如何绑定
6. CommandBus 如何组装
7. QueryBus 如何组装
8. Event Dispatcher 如何运行
9. Outbox 如何调度
10. Inbox 如何消费
11. Adapter 如何发现
12. Configuration 如何管理
13. Lifecycle 如何管理
14. Shutdown 如何执行
15. Health 如何暴露
16. Metrics / Tracing 如何接入
17. Spring Boot 是否只是 Adapter，而不是 Framework Core

---

# 3. 总体架构

```text
                         Application
                              │
                    ┌─────────┴─────────┐
                    │                   │
                CommandBus          QueryBus
                    │                   │
                    ▼                   ▼
               Transaction          Query Runtime
                    │
                    ▼
                UnitOfWork
                    │
                    ▼
                 Domain
                    │
                    ▼
             Repository Contract
                    │
                    ▼
            Infrastructure Runtime
                    │
       ┌────────────┼────────────┐
       ▼            ▼            ▼
   Persistence   Messaging     Cache
    Adapter       Adapter      Adapter
       │            │            │
       ▼            ▼            ▼
   Database       Broker       Cache
```

---

# 4. Runtime 与 Domain 的关系

必须保持：

```text
Runtime
   ↓
Domain Contract
```

而不是：

```text
Domain
   ↓
Runtime
```

因此：

```text
ddd-core
ddd-domain
```

永远不能依赖：

```text
ddd-infrastructure
```

---

# 5. Infrastructure Runtime

定义统一 Runtime：

```java
public interface DddRuntime {

    void start();

    RuntimeState state();

    void shutdown();
}
```

状态：

```java
public enum RuntimeState {

    CREATED,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    FAILED
}
```

---

# 6. Runtime 生命周期

```text
CREATED
   ↓
STARTING
   ↓
RUNNING
   ↓
STOPPING
   ↓
STOPPED
```

启动失败：

```text
STARTING
   ↓
FAILED
```

---

# 7. Runtime 启动原则

Runtime 必须按照依赖拓扑启动：

```text
Configuration
      ↓
Core Services
      ↓
Infrastructure
      ↓
Persistence
      ↓
Transaction
      ↓
Messaging
      ↓
Application
      ↓
Ready
```

---

# 8. Runtime Shutdown

关闭顺序必须与启动顺序相反：

```text
Application
      ↓
Messaging Consumers
      ↓
Outbox Dispatcher
      ↓
Transaction
      ↓
Persistence
      ↓
Infrastructure
```

---

# 9. Graceful Shutdown

Runtime 停止时：

```text
Stop Accepting New Commands
        ↓
Drain Active Commands
        ↓
Stop Event Consumers
        ↓
Drain Outbox
        ↓
Close Persistence
```

不得直接：

```text
System.exit()
```

作为 Framework Runtime 的正常关闭机制。

---

# 10. Runtime Component

定义：

```java
public interface RuntimeComponent {

    String name();

    void start(RuntimeContext context);

    void stop(RuntimeContext context);
}
```

---

# 11. Component Dependency

组件可以声明依赖：

```java
public interface RuntimeComponentDescriptor {

    String name();

    Set<String> dependencies();
}
```

Runtime 根据依赖建立：

```text
Directed Acyclic Graph
```

---

# 12. Component Cycle

禁止：

```text
A → B
B → C
C → A
```

启动器必须检测循环依赖。

发现循环：

```text
RuntimeBootstrapException
```

---

# 13. Runtime Context

定义：

```java
public interface RuntimeContext {

    <T> Optional<T> get(Class<T> type);

    <T> T require(Class<T> type);
}
```

Runtime Context 是：

```text
Component Registry
```

不是：

```text
Domain Service Locator
```

---

# 14. Service Registry

可以注册：

```text
CommandBus
QueryBus
TransactionManager
UnitOfWorkManager
RepositoryFactory
EventDispatcher
OutboxDispatcher
InboxProcessor
Clock
IdGenerator
Serializer
```

---

# 15. Service Locator 限制

Domain 不允许：

```java
runtimeContext.require(OrderRepository.class);
```

Domain Dependency 必须显式通过：

```java
constructor injection
```

或：

```java
method parameter
```

获得。

---

# 16. Dependency Injection

Framework Core 不强制：

```text
Spring
CDI
Guice
Micronaut
```

可以提供轻量：

```text
ddd-runtime
```

用于 Framework 自身组件组装。

---

# 17. Component Registration

推荐：

```java
public interface ComponentRegistry {

    <T> void register(Class<T> type, T component);

    <T> Optional<T> find(Class<T> type);

    <T> T require(Class<T> type);
}
```

---

# 18. Component Identity

每个 Framework Component 必须有稳定 Identity：

```text
componentName
componentType
version
```

例如：

```text
repository.order
transaction.default
outbox.dispatcher
```

---

# 19. Repository Factory

Runtime 必须能够发现 Repository：

```java
public interface RepositoryFactory {

    <A extends AggregateRoot<I>, I extends Identifier>
    AggregateRepository<A, I> repository(
            Class<A> aggregateType);
}
```

---

# 20. Repository Registration

Repository 注册：

```text
Order
    ↓
OrderRepository
    ↓
JdbcOrderRepository
```

Runtime：

```text
Order.class
    ↓
RepositoryFactory
    ↓
OrderRepository
```

---

# 21. Repository Resolution

必须保证：

```text
Aggregate Type
      ↓
Exactly One Active Repository
```

如果：

```text
Order → Repository A
Order → Repository B
```

同时处于 Active：

```text
RepositoryResolutionException
```

---

# 22. Repository Adapter Replacement

Runtime 允许：

```text
OrderRepository
       ↓
JDBC Adapter
```

替换为：

```text
OrderRepository
       ↓
JPA Adapter
```

无需修改 Domain。

---

# 23. Transaction Runtime

Phase VI 定义：

```java
public interface TransactionManager {

    <T> T execute(TransactionCallback<T> callback);
}
```

Phase VIII 定义其 Runtime：

```text
TransactionManager
        ↓
Transaction Adapter
        ↓
Database Transaction
```

---

# 24. Transaction Adapter

```java
public interface TransactionAdapter {

    <T> T execute(
        TransactionDefinition definition,
        TransactionCallback<T> callback);
}
```

---

# 25. Transaction Definition

```java
public record TransactionDefinition(
        Propagation propagation,
        Isolation isolation,
        boolean readOnly,
        Duration timeout
) {}
```

---

# 26. Default Transaction

Command：

```text
Propagation = REQUIRED
Isolation   = DEFAULT
ReadOnly    = false
```

Query：

```text
Propagation = REQUIRED
Isolation   = DEFAULT
ReadOnly    = true
```

具体实现可优化为无事务读取。

---

# 27. UnitOfWork Runtime

UnitOfWork 生命周期：

```text
CREATE
 ↓
ACTIVE
 ↓
COMMITTING
 ↓
COMMITTED
```

失败：

```text
ACTIVE
 ↓
ROLLING_BACK
 ↓
ROLLED_BACK
```

---

# 28. UnitOfWork Context

推荐：

```java
public interface UnitOfWorkManager {

    UnitOfWork current();

    boolean hasCurrent();

    <T> T execute(UnitOfWorkCallback<T> callback);
}
```

---

# 29. UnitOfWork 与 Transaction

关系必须固定：

```text
Transaction
     │
     ▼
UnitOfWork
     │
     ▼
Aggregate
```

不是：

```text
UnitOfWork
     ↓
opens arbitrary transactions
```

---

# 30. Runtime Execution

Command：

```text
CommandBus
   ↓
TransactionManager
   ↓
UnitOfWorkManager
   ↓
CommandHandler
```

---

# 31. Command Runtime

完整流程：

```text
Command
 ↓
CommandBus
 ↓
Middleware
 ↓
Transaction
 ↓
UnitOfWork
 ↓
Handler
 ↓
Aggregate
 ↓
Repository
 ↓
Outbox
 ↓
Commit
```

---

# 32. Query Runtime

```text
Query
 ↓
QueryBus
 ↓
Middleware
 ↓
Read Repository
 ↓
Read Model
```

Query 默认不创建 Domain UnitOfWork。

---

# 33. Command Handler Invocation

Runtime：

```java
public interface CommandHandlerInvoker {

    <R> R invoke(Command<R> command);
}
```

Handler Registry：

```java
public interface CommandHandlerRegistry {

    <C extends Command<R>, R>
    void register(
        Class<C> commandType,
        CommandHandler<C, R> handler);
}
```

---

# 34. Handler Uniqueness

同一个：

```text
Command Type
```

只能存在：

```text
One Active Handler
```

否则：

```text
DuplicateHandlerException
```

---

# 35. Query Handler Registry

同样：

```text
Query Type
      ↓
Exactly One Handler
```

---

# 36. Event Dispatcher

Domain Event Dispatcher：

```java
public interface EventDispatcher {

    void dispatch(DomainEvent event);
}
```

但必须区分：

```text
Domain Event
```

和：

```text
Integration Event
```

---

# 37. Event Dispatcher Boundary

Aggregate：

```text
Aggregate
 ↓
Pending Domain Event
```

Runtime：

```text
UnitOfWork
 ↓
Outbox
```

Broker：

```text
Outbox
 ↓
Integration Event
 ↓
Broker
```

Aggregate 不直接调用：

```text
EventDispatcher
```

---

# 38. Outbox Dispatcher

```java
public interface OutboxDispatcher {

    DispatchResult dispatchBatch(int batchSize);
}
```

---

# 39. Outbox Dispatcher Lifecycle

```text
START
 ↓
Load Pending
 ↓
Publish
 ↓
Mark Published
 ↓
Repeat
```

---

# 40. Outbox Failure

Publish 失败：

```text
Outbox Record
       ↓
Remain Pending
```

或者：

```text
Retry
```

不能：

```text
Delete Record
```

---

# 41. Retry Policy

定义：

```java
public interface RetryPolicy {

    boolean shouldRetry(
        Throwable failure,
        int attempt);

    Duration nextDelay(int attempt);
}
```

---

# 42. Retry Types

支持：

```text
Fixed
Exponential
Exponential + Jitter
```

推荐：

```text
Exponential + Jitter
```

用于：

```text
Network
Broker
Database transient error
```

---

# 43. 不应 Retry

默认不得自动 Retry：

```text
BusinessRuleViolation
AuthorizationFailure
ValidationFailure
InvalidCommand
```

---

# 44. Concurrency Retry

可以：

```text
ConcurrencyConflict
 ↓
Reload Aggregate
 ↓
Re-execute Command
```

但必须明确配置。

不能无限重试。

---

# 45. Retry Limit

推荐：

```text
maxAttempts = finite
```

例如：

```text
3
5
```

超过限制：

```text
Failure
```

---

# 46. Async Boundary

Runtime 必须明确：

```text
Transaction Context
```

不能隐式跨越：

```text
Thread
Executor
CompletableFuture
Message
Broker
```

---

# 47. Async Rule

错误：

```text
Transaction
 ↓
Async Task
 ↓
Database
```

正确：

```text
Transaction A
 ↓
Commit
 ↓
Message
 ↓
Transaction B
 ↓
Consumer
```

---

# 48. Thread Context

Runtime 可以使用：

```text
ThreadLocal
```

或：

```text
Scoped Context
```

保存：

```text
UnitOfWork
CorrelationId
TenantContext
TransactionContext
```

但必须：

```text
clear after completion
```

---

# 49. Context Leak

禁止：

```text
Request A
 ↓
ThreadLocal
 ↓
Request B
 ↓
Reuse A Context
```

Runtime 必须在：

```text
finally
```

清理上下文。

---

# 50. Correlation Context

Runtime 可以定义：

```java
public record CorrelationContext(
        String correlationId,
        String causationId,
        String traceId
) {}
```

这些属于：

```text
Infrastructure / Application Context
```

不是 Domain State。

---

# 51. Tenant Context

```java
public record TenantContext(
        String tenantId
) {}
```

生命周期：

```text
Request
 ↓
Application
 ↓
Transaction
 ↓
Persistence
```

完成后清理。

---

# 52. Security Context

Framework Runtime 可以接入：

```text
SecurityContext
```

但是：

```text
ddd-core
```

不依赖 Security Framework。

---

# 53. Authorization Runtime

Command Middleware：

```text
Authorization
```

Query：

```text
Authorization
```

Domain：

```text
Business Invariant
```

三者必须保持区别。

---

# 54. Configuration

Framework 配置：

```java
public interface DddConfiguration {

    <T> T get(
        ConfigurationKey<T> key);
}
```

---

# 55. Configuration Key

例如：

```text
ddd.transaction.timeout
ddd.outbox.batch-size
ddd.outbox.poll-interval
ddd.retry.max-attempts
ddd.runtime.shutdown-timeout
```

---

# 56. Configuration Precedence

推荐：

```text
Programmatic
    >
Environment
    >
Configuration File
    >
Default
```

---

# 57. Configuration Validation

启动时必须检查：

```text
Required Property
Type
Range
Dependency
Conflict
```

配置错误：

```text
RuntimeConfigurationException
```

---

# 58. Fail Fast

例如：

```text
database.url = null
```

如果 Database Adapter Required：

```text
Startup Failure
```

而不是：

```text
Start Successfully
 ↓
First Request Failure
```

---

# 59. Adapter Discovery

支持：

```text
Explicit Registration
```

优先于：

```text
Reflection Scanning
```

Java 17 环境可以使用：

```text
ServiceLoader
```

作为标准 SPI。

---

# 60. ServiceLoader

例如：

```text
META-INF/services/
```

实现：

```java
public interface DddAdapterProvider {

    void register(
        ComponentRegistry registry);
}
```

---

# 61. Adapter Provider

可以提供：

```text
JdbcAdapterProvider
JpaAdapterProvider
MyBatisAdapterProvider
KafkaAdapterProvider
RedisAdapterProvider
```

但它们都是：

```text
Optional
```

---

# 62. No Mandatory Spring

核心 Runtime：

```text
No Spring
```

Spring Boot：

```text
Adapter
```

因此：

```text
ddd-spring-boot
```

负责：

```text
AutoConfiguration
Bean Registration
Property Binding
Lifecycle
Health
```

---

# 63. Spring Boot Boundary

```text
Spring Boot
      ↓
ddd-spring-boot
      ↓
ddd-runtime
      ↓
ddd-* contracts
```

而不是：

```text
ddd-core
      ↓
Spring
```

---

# 64. Bootstrap API

建议提供：

```java
DddRuntime runtime =
        DddRuntimeBuilder
            .create()
            .configuration(configuration)
            .register(orderRepository)
            .register(commandHandler)
            .register(queryHandler)
            .build();

runtime.start();
```

---

# 65. Builder

```java
public interface DddRuntimeBuilder {

    DddRuntimeBuilder configuration(
        DddConfiguration configuration);

    DddRuntimeBuilder register(
        Object component);

    DddRuntime build();
}
```

---

# 66. Runtime Bootstrap

完整过程：

```text
Builder
 ↓
Collect Components
 ↓
Validate
 ↓
Build Dependency Graph
 ↓
Create Runtime Context
 ↓
Initialize Components
 ↓
Start Components
 ↓
RUNNING
```

---

# 67. Bootstrap Validation

启动前检查：

```text
Command Handler
Query Handler
Repository
Transaction Manager
Required Adapter
Duplicate Component
Dependency Cycle
Configuration
```

---

# 68. Dependency Graph

例如：

```text
CommandBus
    ↓
Middleware
    ↓
TransactionManager
    ↓
UnitOfWorkManager
    ↓
RepositoryFactory
    ↓
PersistenceAdapter
```

Runtime 必须保证依赖已经 Ready。

---

# 69. Health Check

定义：

```java
public interface HealthIndicator {

    HealthStatus health();
}
```

---

# 70. Health Status

```java
public enum HealthState {

    UP,
    DEGRADED,
    DOWN
}
```

---

# 71. Health Categories

至少：

```text
Runtime
Database
Message Broker
Outbox
Inbox
Cache
```

---

# 72. Readiness

Runtime：

```text
STARTING
```

不能报告：

```text
READY
```

只有：

```text
RUNNING
+
Required Components Healthy
```

才可以：

```text
READY
```

---

# 73. Liveness

Liveness 表示：

```text
Runtime Process
```

仍然可以工作。

不能简单将：

```text
Database Down
```

全部映射成：

```text
Process Dead
```

---

# 74. Metrics

Framework 可以提供：

```text
Command Count
Command Duration
Command Failure
Query Count
Query Duration
Transaction Count
Transaction Failure
Concurrency Conflict
Outbox Pending
Outbox Retry
Inbox Duplicate
Projection Lag
```

---

# 75. Metrics API

```java
public interface MetricsRecorder {

    void increment(String name);

    void record(
        String name,
        Duration duration);
}
```

---

# 76. Metrics 不进入 Domain

Domain：

```text
Order.pay()
```

不应该直接：

```java
metrics.increment("order.pay");
```

除非通过极轻量且明确的 Domain Telemetry Port；默认不建议。

---

# 77. Tracing

Runtime 可以提供：

```text
Command Span
Query Span
Transaction Span
Repository Span
Outbox Span
Consumer Span
```

推荐：

```text
OpenTelemetry Adapter
```

但 Core 不依赖 OpenTelemetry。

---

# 78. Logging

Infrastructure 日志必须包含：

```text
timestamp
level
component
correlationId
traceId
```

如果适用：

```text
tenantId
aggregateType
aggregateId
commandType
```

---

# 79. Logging Security

不得记录：

```text
password
token
secret
private key
sensitive payload
```

---

# 80. Serialization

Integration Event 需要：

```text
Serializer
```

定义：

```java
public interface EventSerializer {

    byte[] serialize(Object value);

    <T> T deserialize(
        byte[] payload,
        Class<T> type);
}
```

---

# 81. Serialization Boundary

Domain Event：

```text
Java Object
```

Integration Event：

```text
Serialized Payload
```

转换发生在：

```text
Event Infrastructure
```

---

# 82. Serialization Version

Serializer 必须支持：

```text
eventType
eventVersion
```

否则 Event Evolution 无法可靠实现。

---

# 83. Message Broker Adapter

定义：

```java
public interface MessagePublisher {

    PublishResult publish(
        IntegrationEvent event);
}
```

可以适配：

```text
Kafka
RabbitMQ
Pulsar
JMS
HTTP
```

---

# 84. Broker Independence

Domain：

```text
No Kafka
No RabbitMQ
No Pulsar
```

Application：

```text
No concrete Broker dependency
```

Infrastructure：

```text
Broker Adapter
```

---

# 85. Message Consumer

定义：

```java
public interface MessageConsumer {

    void start();

    void stop();
}
```

---

# 86. Consumer Transaction

推荐：

```text
Message
 ↓
Begin Transaction
 ↓
Inbox
 ↓
Handler
 ↓
Aggregate
 ↓
Outbox
 ↓
Commit
```

---

# 87. Consumer Failure

如果处理失败：

```text
Transaction Rollback
```

则：

```text
Inbox Record
```

也回滚。

---

# 88. Dead Letter

连续失败可以进入：

```text
DLQ
```

但 DLQ 是：

```text
Messaging Infrastructure
```

不是 Domain。

---

# 89. Cache Adapter

可以定义：

```java
public interface Cache {

    Optional<byte[]> get(String key);

    void put(
        String key,
        byte[] value,
        Duration ttl);

    void evict(String key);
}
```

---

# 90. Cache Rule

Cache 不得成为：

```text
Domain Source of Truth
```

默认：

```text
Database
```

仍然是 Write Model Source of Truth。

---

# 91. Cache Consistency

Cache 更新可以：

```text
Event Driven
```

例如：

```text
Aggregate
 ↓
Commit
 ↓
Domain/Integration Event
 ↓
Cache Invalidation
```

不要：

```text
Before Commit
 ↓
Evict Cache
```

导致事务失败后 Cache 已经发生变化。

---

# 92. Clock

Framework 应提供：

```java
public interface ClockProvider {

    Instant now();
}
```

测试时：

```text
FixedClock
```

生产：

```text
SystemClock
```

---

# 93. ID Generator

```java
public interface IdentifierGenerator {

    String generate();
}
```

可适配：

```text
UUID
ULID
UUIDv7
Snowflake
Database Sequence
```

Domain 不绑定具体算法。

---

# 94. Identifier Generation

如果 ID 在业务行为中产生：

```text
Application / Domain
```

可以调用：

```text
IdentifierGenerator Port
```

而不是：

```text
Database Sequence
```

直接进入 Domain。

---

# 95. Runtime Error Model

Runtime 错误分类：

```text
Bootstrap
Configuration
Component
Adapter
Transaction
Persistence
Messaging
Serialization
Lifecycle
```

---

# 96. Runtime Exception

推荐：

```java
DddRuntimeException
```

层次：

```text
DddRuntimeException
 ├── BootstrapException
 ├── ConfigurationException
 ├── ComponentException
 ├── AdapterException
 ├── LifecycleException
 └── RuntimeExecutionException
```

---

# 97. Business Exception Boundary

Business Exception：

```text
DomainException
```

不能被 Runtime 随意吞掉。

正确：

```text
DomainException
 ↓
Command Failure
 ↓
Transaction Rollback
```

---

# 98. Infrastructure Exception Boundary

例如：

```text
Database Timeout
```

应该：

```text
PersistenceTimeoutException
 ↓
Transaction Rollback
 ↓
Retry Policy
```

如果不可恢复：

```text
Failure
```

---

# 99. Runtime Transaction Example

```java
transactionManager.execute(() -> {

    UnitOfWork uow = unitOfWorkManager.current();

    Order order =
        orderRepository
            .findById(command.orderId())
            .orElseThrow();

    order.pay();

    orderRepository.save(order);

    return null;
});
```

实际 Framework 中：

> Application Handler 不应该手动承担 commit/rollback。

---

# 100. 推荐 Command Runtime

```java
public <R> R execute(Command<R> command) {

    return transactionManager.execute(() ->
        unitOfWorkManager.execute(() ->
            handler.handle(command)
        )
    );
}
```

异常：

```text
throw
 ↓
rollback
 ↓
context cleanup
```

成功：

```text
handler
 ↓
UoW commit
 ↓
transaction commit
```

---

# 101. Commit Ordering

最终顺序必须保持：

```text
Domain State
 ↓
Repository Persistence
 ↓
Outbox Persistence
 ↓
UnitOfWork Commit
 ↓
Transaction Commit
```

Broker Publish：

```text
Transaction Commit
 ↓
Outbox Dispatcher
 ↓
Broker
```

---

# 102. 禁止提前 Publish

错误：

```text
Aggregate
 ↓
Broker.publish()
 ↓
Database Commit
```

如果：

```text
Database Commit Failed
```

则 Broker 已经产生事件。

这是：

```text
Event Inconsistency
```

---

# 103. 正确 Publish

```text
Aggregate
 ↓
Repository
 ↓
Outbox
 ↓
Transaction Commit
 ↓
Dispatcher
 ↓
Broker
```

---

# 104. Startup Ordering

Messaging Consumer 不得在：

```text
Persistence
Transaction
```

尚未 Ready 时开始消费。

推荐：

```text
Persistence
 ↓
Transaction
 ↓
Application
 ↓
Outbox
 ↓
Consumer
```

---

# 105. Shutdown Ordering

推荐：

```text
Stop Consumer
 ↓
Drain In-flight Message
 ↓
Stop Outbox Polling
 ↓
Drain Outbox
 ↓
Stop Application
 ↓
Close Transaction
 ↓
Close Persistence
```

---

# 106. Runtime Resource Ownership

每个 Resource 必须存在唯一 Owner。

例如：

```text
DataSource
   ↓
Persistence Runtime
```

```text
Connection
   ↓
Transaction Runtime
```

```text
Executor
   ↓
Messaging Runtime
```

---

# 107. Resource Close

Runtime 必须支持：

```text
AutoCloseable
```

例如：

```java
public interface DddRuntime
        extends AutoCloseable {

    void start();

    void shutdown();

    @Override
    default void close() {
        shutdown();
    }
}
```

---

# 108. Thread Pool

Framework Runtime 可以创建：

```text
Command Executor
Query Executor
Outbox Executor
Consumer Executor
Projection Executor
```

但必须：

```text
Named
Bounded
Observable
Shutdownable
```

---

# 109. Unbounded Executor

禁止默认：

```text
newCachedThreadPool()
```

承担无限外部消息处理。

推荐：

```text
Bounded Queue
Bounded Workers
Backpressure
```

---

# 110. Backpressure

消息处理能力不足时：

```text
Producer
 ↓
Queue
 ↓
Consumer
```

必须存在：

```text
Capacity Limit
```

避免：

```text
Memory Exhaustion
```

---

# 111. Runtime Configuration Example

```yaml
ddd:
  runtime:
    shutdown-timeout: 30s

  transaction:
    timeout: 30s

  outbox:
    batch-size: 100
    poll-interval: 1s

  retry:
    max-attempts: 3

  messaging:
    consumer-workers: 8
```

该配置属于：

```text
Infrastructure Runtime
```

不是 Domain Configuration。

---

# 112. Runtime Package

建议：

```text
io.github.regalpine.ddd.runtime
├── DddRuntime
├── DddRuntimeBuilder
├── RuntimeContext
├── RuntimeComponent
├── ComponentRegistry
├── RuntimeState
├── bootstrap
├── lifecycle
├── configuration
├── health
└── metrics
```

---

# 113. Infrastructure Package

```text
io.github.regalpine.ddd.infrastructure
├── repository
├── persistence
├── transaction
├── event
├── outbox
├── inbox
├── messaging
├── serialization
├── cache
├── identity
└── time
```

---

# 114. Module Architecture

Phase VIII 后：

```text
ddd-core
   ↑
ddd-domain
   ↑
ddd-application
   ↑
ddd-cqrs
ddd-event
ddd-transaction
   ↑
ddd-runtime
   ↑
ddd-infrastructure
   ↑
Adapters
```

---

# 115. 推荐模块进一步收敛

为了避免模块无限膨胀：

```text
ddd-runtime
```

负责：

```text
Runtime Lifecycle
Component Registry
Bootstrap
Configuration
Health
```

而：

```text
ddd-infrastructure
```

负责：

```text
Concrete Infrastructure SPI
Adapter Base
Persistence
Messaging
Outbox
Inbox
```

---

# 116. Adapter Modules

只有在实际存在独立技术依赖时才拆分：

```text
ddd-infrastructure-jdbc
ddd-infrastructure-jpa
ddd-infrastructure-mybatis
ddd-infrastructure-kafka
ddd-infrastructure-redis
ddd-spring-boot
```

禁止：

```text
ddd-infrastructure-foo
ddd-infrastructure-bar
ddd-runtime-x
ddd-runtime-y
```

无实际独立生命周期时不要拆模块。

---

# 117. Maven Dependency

推荐：

```text
ddd-core
   ↑
ddd-domain
   ↑
ddd-application
   ↑
ddd-transaction
   ↑
ddd-runtime
   ↑
ddd-infrastructure
   ↑
Technology Adapters
```

CQRS：

```text
ddd-cqrs
   ↓
ddd-application
```

Event：

```text
ddd-event
   ↓
ddd-domain
```

---

# 118. 禁止依赖

```text
ddd-core
    X ddd-runtime

ddd-domain
    X ddd-runtime

ddd-domain
    X ddd-infrastructure

ddd-application
    X JPA

ddd-application
    X JDBC

ddd-cqrs
    X Kafka

ddd-event
    X Kafka
```

---

# 119. Dependency Direction

最终必须满足：

```text
High-Level Business Policy
          ↑
       Domain
          ↑
     Application
          ↑
   Infrastructure
          ↑
      Technology
```

从代码依赖角度：

```text
Technology
    ↓
Infrastructure
    ↓
Application / Domain Contracts
```

---

# 120. Architecture Rule

Framework 必须满足：

> **Dependency Rule**

具体技术永远不能成为业务模型的上层依赖。

---

# 121. Runtime SPI

建议 Runtime 提供：

```java
public interface DddAdapterProvider {

    void register(
        RuntimeContext context);
}
```

Infrastructure Adapter 通过该 SPI 注册。

---

# 122. Adapter Lifecycle

```text
DISCOVERED
 ↓
VALIDATED
 ↓
INITIALIZED
 ↓
STARTED
 ↓
READY
```

关闭：

```text
STOPPING
 ↓
STOPPED
```

---

# 123. Adapter Failure

一个非关键 Adapter：

```text
Cache
```

失败可以：

```text
DEGRADED
```

一个 Required Adapter：

```text
Database
```

失败：

```text
STARTUP FAILURE
```

---

# 124. Required / Optional

Component 必须声明：

```java
public interface ComponentRequirement {

    boolean required();
}
```

例如：

```text
Database       Required
Transaction    Required
Cache          Optional
Metrics        Optional
Tracing        Optional
```

---

# 125. Optional Adapter Rule

Optional Adapter 不得改变：

```text
Domain Semantics
```

例如 Redis 不可用：

```text
Cache Disabled
```

不应该导致：

```text
Domain Behavior Changed
```

除非业务明确要求 Cache 为强依赖。

---

# 126. Framework Extension

Framework 支持：

```text
Extension
```

例如：

```java
public interface DddExtension {

    void configure(DddRuntimeBuilder builder);
}
```

---

# 127. Extension Restrictions

Extension 不得修改：

```text
Domain Contract
```

只能：

```text
Register
Configure
Observe
Adapt
```

---

# 128. Runtime Event Hooks

可以提供：

```text
RuntimeStarted
RuntimeStopping
RuntimeStopped
ComponentStarted
ComponentStopped
```

这些是：

```text
Runtime Lifecycle Event
```

不是：

```text
Domain Event
```

---

# 129. Domain Event 与 Runtime Event

必须严格区分：

| 类型 | 来源 | 目的 |
|---|---|---|
| Domain Event | Aggregate | Business Fact |
| Integration Event | Application/Infrastructure | External Communication |
| Runtime Event | Runtime | Lifecycle |

---

# 130. Runtime Observability

至少支持：

```text
Command
Query
Transaction
Repository
Outbox
Inbox
Projection
Runtime
```

作为 Observability Boundary。

---

# 131. Runtime Debugging

Framework 应能回答：

```text
当前 Runtime 是否 Ready？
哪个 Component 启动失败？
哪个 Repository 被注册？
哪个 Handler 被注册？
Outbox 有多少积压？
当前 Transaction 是否 Active？
```

但不能泄漏：

```text
Secrets
Credentials
Sensitive Payload
```

---

# 132. Runtime Diagnostics

建议：

```java
public interface RuntimeDiagnostics {

    RuntimeSnapshot snapshot();
}
```

Snapshot：

```text
RuntimeState
ComponentStates
RegisteredCommands
RegisteredQueries
Repositories
Health
Metrics
```

---

# 133. Production Diagnostics

Production 默认不允许：

```text
Dump all sensitive configuration
```

Diagnostics 必须经过：

```text
Authorization
```

---

# 134. Framework Startup Example

```java
DddRuntime runtime =
    DddRuntimeBuilder.create()
        .configuration(configuration)
        .register(new JdbcOrderRepository(...))
        .register(new CreateOrderHandler(...))
        .register(new GetOrderHandler(...))
        .register(transactionManager)
        .register(outboxDispatcher)
        .build();

runtime.start();
```

---

# 135. Spring Boot Example

Spring Boot 只负责：

```text
ApplicationContext
        ↓
Bean Discovery
        ↓
DddRuntimeBuilder
        ↓
DddRuntime
```

Framework Core 不知道：

```text
Spring ApplicationContext
```

---

# 136. Testing Runtime

必须支持：

```text
InMemory Runtime
```

用于：

```text
Unit Test
Application Test
Integration Test
Conformance Test
```

---

# 137. Test Runtime

例如：

```java
DddRuntime runtime =
    TestDddRuntime.create()
        .withInMemoryRepository()
        .withFakeClock()
        .withFakeEventPublisher()
        .build();
```

---

# 138. Deterministic Test

测试环境应支持：

```text
Fixed Clock
Deterministic ID
InMemory Event Store
Fake Transaction
```

使：

```text
Same Input
      ↓
Same Result
```

---

# 139. Runtime Conformance

Runtime 必须通过：

```text
Runtime Conformance Kit
```

验证：

```text
Lifecycle
Component Discovery
Dependency Graph
Transaction
UoW
Repository
Outbox
Messaging
Shutdown
```

---

# 140. Formal Runtime Rules

## RUNTIME-001

Runtime 必须拥有明确生命周期。

## RUNTIME-002

Runtime 启动必须经过依赖验证。

## RUNTIME-003

Runtime 不得存在循环 Component Dependency。

## RUNTIME-004

每个 Required Component 必须 Ready 后 Runtime 才能 Ready。

## RUNTIME-005

Domain 不得依赖 Runtime。

## RUNTIME-006

Runtime Context 不得成为 Domain Service Locator。

## RUNTIME-007

Repository 必须通过明确 Contract 注册。

## RUNTIME-008

同一 Aggregate Type 只能有一个 Active Repository。

## RUNTIME-009

同一 Command Type 只能有一个 Active Handler。

## RUNTIME-010

同一 Query Type 只能有一个 Active Handler。

---

# 141. Transaction Rules

## RUNTIME-TX-001

Transaction Runtime 必须控制物理事务。

## RUNTIME-TX-002

UnitOfWork 不得自行创建任意物理事务。

## RUNTIME-TX-003

Command 默认运行于 Write Transaction。

## RUNTIME-TX-004

Query 默认不得修改业务状态。

## RUNTIME-TX-005

Transaction Context 不得隐式跨 Async Boundary。

## RUNTIME-TX-006

Transaction 完成后必须清理 Context。

---

# 142. Event Rules

## RUNTIME-EVT-001

Aggregate 不得直接调用 Broker。

## RUNTIME-EVT-002

Domain Event 不得直接 Publish 到外部 Broker。

## RUNTIME-EVT-003

Outbox 必须在 Transaction Commit 前持久化。

## RUNTIME-EVT-004

Outbox Dispatcher 只处理已经 Commit 的 Outbox Record。

## RUNTIME-EVT-005

Publish Failure 不得破坏已提交业务事务。

---

# 143. Messaging Rules

## RUNTIME-MSG-001

Message Consumer 必须拥有独立 Transaction Boundary。

## RUNTIME-MSG-002

Inbox 必须支持 Duplicate Detection。

## RUNTIME-MSG-003

Consumer Failure 必须支持 Retry。

## RUNTIME-MSG-004

超过 Retry Policy 后必须支持 DLQ 或等价 Failure Handling。

## RUNTIME-MSG-005

Message Broker 不得成为 Domain Dependency。

---

# 144. Adapter Rules

## ADAPTER-001

Adapter 必须实现 Framework Contract。

## ADAPTER-002

Adapter 不得修改 Domain Contract。

## ADAPTER-003

Adapter 必须拥有明确生命周期。

## ADAPTER-004

Required Adapter 启动失败必须阻止 Runtime Ready。

## ADAPTER-005

Optional Adapter 失败默认只能导致 Degraded。

---

# 145. Observability Rules

## OBS-001

Runtime 必须提供 Health 状态。

## OBS-002

Required Infrastructure 必须可观测。

## OBS-003

Transaction Failure 必须可统计。

## OBS-004

Concurrency Conflict 必须可统计。

## OBS-005

Outbox Backlog 必须可观测。

## OBS-006

Message Failure 必须可观测。

## OBS-007

敏感数据不得进入默认日志。

---

# 146. Security Rules

## SEC-INF-001

Runtime Configuration 不得明文输出 Secret。

## SEC-INF-002

Persistence Adapter 必须支持 Tenant Isolation。

## SEC-INF-003

Messaging Adapter 必须支持认证配置。

## SEC-INF-004

Diagnostics 必须经过授权。

## SEC-INF-005

Framework 不得默认记录完整业务 Payload。

---

# 147. Phase VIII 架构冻结

Phase VIII 完成后冻结：

```text
Runtime
Component
Bootstrap
Lifecycle
Component Registry
Repository Factory
Transaction Runtime
UnitOfWork Runtime
Command Runtime
Query Runtime
Outbox Dispatcher
Inbox Processor
Messaging Adapter
Serialization Adapter
Cache Adapter
Clock
ID Generator
Configuration
Health
Metrics
Tracing
Diagnostics
```

---

# 148. Phase VIII Completion Matrix

| Capability | Status |
|---|---|
| Runtime Lifecycle | ✅ |
| Bootstrap | ✅ |
| Component Registry | ✅ |
| Dependency Graph | ✅ |
| Repository Factory | ✅ |
| Transaction Runtime | ✅ |
| UnitOfWork Runtime | ✅ |
| Command Runtime | ✅ |
| Query Runtime | ✅ |
| Outbox Dispatcher | ✅ |
| Inbox Processor | ✅ |
| Message Adapter | ✅ |
| Serialization | ✅ |
| Retry | ✅ |
| DLQ Boundary | ✅ |
| Cache Adapter | ✅ |
| Clock | ✅ |
| ID Generator | ✅ |
| Configuration | ✅ |
| Health | ✅ |
| Metrics | ✅ |
| Tracing | ✅ |
| Diagnostics | ✅ |
| Graceful Shutdown | ✅ |
| Adapter Lifecycle | ✅ |
| Spring Boot Boundary | ✅ |
| Test Runtime | ✅ |

---

# 149. 当前 Framework 完整架构

经过 Phase I–VIII：

```text
┌─────────────────────────────────────────────┐
│                 Interface                   │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│                Application                  │
│ Command / Query / Handler / Use Case        │
└──────────────────────┬──────────────────────┘
                       │
              ┌────────┴────────┐
              ▼                 ▼
         Write Side         Read Side
              │                 │
              ▼                 ▼
         Transaction       Read Repository
              │
              ▼
         UnitOfWork
              │
              ▼
         Domain Model
              │
              ▼
      Repository Contract
              │
              ▼
     Infrastructure Runtime
              │
       ┌──────┼──────┐
       ▼      ▼      ▼
    JDBC/JPA Message Cache
       │      │      │
       ▼      ▼      ▼
       DB   Broker   Cache
```

---

# 150. Phase I–VIII 架构收敛

```text
Phase I
Ontology / DDD Concept
        ↓
Phase II
ddd-core
        ↓
Phase III
ddd-domain
        ↓
Phase IV
ddd-application
        ↓
Phase V
CQRS + Event
        ↓
Phase VI
Transaction + UoW
        ↓
Phase VII
Persistence
        ↓
Phase VIII
Infrastructure Runtime
```

至此已经形成：

> **完整的 DDD Framework Runtime 基础。**

---

# 151. 下一阶段定位

下一阶段不再继续增加：

```text
Entity
Aggregate
Value Object
Repository
Transaction
CQRS
Persistence
Runtime
```

这些核心抽象已经基本冻结。

下一阶段进入：

# Phase IX — Integration & Messaging Specification

主要解决：

```text
Domain Event
        ↓
Integration Event
        ↓
Event Envelope
        ↓
Message Broker
        ↓
Consumer
        ↓
Inbox
        ↓
Application Process
        ↓
Command
```

以及：

```text
Kafka
RabbitMQ
Pulsar
HTTP
Webhook
Cloud/Enterprise Message Bus
```

之间的统一 Adapter Contract。

---

# 152. Phase IX 的边界

Phase IX 将重点解决：

1. Integration Event
2. Event Envelope
3. Message Contract
4. Message Routing
5. Topic / Queue
6. Consumer Group
7. Delivery Semantics
8. Retry
9. DLQ
10. Inbox
11. Idempotency
12. Event Ordering
13. Schema Registry
14. Event Compatibility
15. Consumer Versioning
16. Message Security
17. Message Observability
18. Broker Adapter

但不会重新定义 Domain Event。

---

# 153. 收敛原则

从 Phase IX 开始：

> **新增内容必须证明它属于 Framework 的实际运行能力，而不能仅仅因为理论上“还可以抽象”就增加新的核心模型。**

Framework 的核心模型应保持稳定：

```text
Entity
Value Object
Aggregate
Domain Event
Repository
Command
Query
Handler
Transaction
UnitOfWork
Read Model
Integration Event
Runtime
Adapter
```

后续主要进行：

```text
Implementation
Integration
Testing
Conformance
Developer Experience
```

而不是无限增加新的领域抽象。

---

# 154. Phase VIII 最终结论

RegalPine DDD Framework 到 Phase VIII 已经从：

```text
DDD Architecture
```

演进为：

```text
DDD Framework Runtime Architecture
```

并形成稳定的六边形依赖方向：

```text
Business
   ↑
Application
   ↑
Ports / Contracts
   ↑
Infrastructure
   ↑
Technology
```

同时支持：

```text
DDD
+
Hexagonal Architecture
+
Four-Layer Architecture
+
CQRS
+
Domain Events
+
Outbox
+
Inbox
+
Optimistic Concurrency
+
Transaction
+
UnitOfWork
+
Persistence Adapter
+
Runtime
```

**Phase VIII 不改变 Domain 核心模型。**

它只负责让已经定义的模型：

> **能够被组装、启动、运行、监控、停止和替换。**