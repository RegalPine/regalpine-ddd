# RegalPine DDD Framework

## Phase X — Developer Framework Implementation & Conformance Specification

**Version:** v0.1  
**Status:** Implementation Baseline  
**GroupId:** `io.github.regalpine.ddd`  
**Java:** 17+  
**Build:** Maven  
**Architecture:** DDD + Hexagonal Architecture + CQRS + Event-Driven Architecture

---

# 1. 文档定位

Phase X 是 RegalPine DDD Framework 从：

```text
Architecture Specification
```

进入：

```text
Framework Implementation Specification
```

的正式阶段。

前九个阶段已经定义：

```text
Core Domain Model
Domain Layer
Application Layer
CQRS
Event Architecture
Transaction / UoW
Runtime
Infrastructure
Messaging
```

Phase X 不再扩展核心领域抽象，而是解决：

```text
如何实现
如何装配
如何扩展
如何测试
如何验证
如何发布
如何升级
```

---

# 2. Phase X 核心目标

Phase X 必须实现：

1. Maven 最终模块结构
2. Java 17 API
3. SPI
4. Runtime Bootstrap
5. Handler Registration
6. Repository Registration
7. Transaction Adapter
8. Outbox / Inbox
9. Messaging Adapter
10. In-Memory Adapter
11. Test Framework
12. Conformance Test Kit
13. Reference Implementation
14. API Stability
15. Compatibility Policy
16. Developer Experience

核心原则：

> Phase X 不重新设计 DDD 模型，而是把已经定义的模型变成可运行 Framework。

---

# 3. 最终模块结构

推荐最终 Maven 结构：

```text
regalpine-ddd
│
├── ddd-core
├── ddd-domain
├── ddd-application
├── ddd-cqrs
├── ddd-event
├── ddd-transaction
├── ddd-runtime
├── ddd-messaging
│
├── ddd-infrastructure
│
├── ddd-test
├── ddd-conformance
│
├── ddd-infrastructure-jdbc
├── ddd-infrastructure-jpa
├── ddd-messaging-kafka
├── ddd-messaging-rabbitmq
│
└── ddd-spring-boot
```

其中：

```text
核心模块
=
ddd-core
ddd-domain
ddd-application
ddd-cqrs
ddd-event
ddd-transaction
ddd-runtime
ddd-messaging
```

扩展模块：

```text
Infrastructure
Messaging Adapter
Spring Adapter
```

---

# 4. Maven GroupId

统一：

```xml
<groupId>io.github.regalpine.ddd</groupId>
```

---

# 5. Artifact 命名

核心：

```text
io.github.regalpine.ddd:ddd-core
io.github.regalpine.ddd:ddd-domain
io.github.regalpine.ddd:ddd-application
io.github.regalpine.ddd:ddd-cqrs
io.github.regalpine.ddd:ddd-event
io.github.regalpine.ddd:ddd-transaction
io.github.regalpine.ddd:ddd-runtime
io.github.regalpine.ddd:ddd-messaging
```

测试：

```text
io.github.regalpine.ddd:ddd-test
io.github.regalpine.ddd:ddd-conformance
```

Adapter：

```text
io.github.regalpine.ddd:ddd-infrastructure-jdbc
io.github.regalpine.ddd:ddd-infrastructure-jpa
io.github.regalpine.ddd:ddd-messaging-kafka
io.github.regalpine.ddd:ddd-messaging-rabbitmq
io.github.regalpine.ddd:ddd-spring-boot
```

---

# 6. Maven Dependency DAG

最终依赖方向：

```text
ddd-core
   ↑
ddd-domain
   ↑
ddd-application
   ↑
┌─────────────┬──────────────┬───────────────┐
ddd-cqrs      ddd-event      ddd-transaction
   │             │                 │
   └─────────────┴─────────┬───────┘
                           ↓
                      ddd-runtime
                           ↓
                     ddd-messaging
                           ↓
                  ddd-infrastructure
```

测试：

```text
ddd-test
   ↓
all framework modules

ddd-conformance
   ↓
framework contracts
```

Adapter：

```text
ddd-messaging-kafka
        ↓
ddd-messaging

ddd-infrastructure-jdbc
        ↓
ddd-domain
ddd-transaction
ddd-runtime
```

---

# 7. Dependency Forbidden Rules

## FORBID-001

`ddd-core` 不得依赖：

```text
Spring
Jakarta
Hibernate
JPA
JDBC
Kafka
RabbitMQ
Redis
Jackson
```

---

## FORBID-002

`ddd-domain` 不得依赖 Infrastructure。

---

## FORBID-003

`ddd-domain` 不得依赖：

```text
CommandBus
QueryBus
MessageBroker
TransactionAdapter
```

---

## FORBID-004

Application 不得直接创建 Infrastructure Adapter。

错误：

```java
new KafkaMessagePublisher()
```

正确：

```java
MessagePublisher publisher
```

由 Runtime 注入。

---

# 8. Java 17 Baseline

Framework 使用：

```text
Java 17
```

允许：

```text
record
sealed interface
pattern matching instanceof
switch expression
text block
```

不得依赖高于 Java 17 的语言特性。

---

# 9. API Stability

核心 API 分为：

```text
STABLE
EXPERIMENTAL
INTERNAL
```

例如：

```java
public interface AggregateRoot<I extends Identifier>
```

属于：

```text
STABLE
```

而：

```java
internal.runtime.*
```

属于：

```text
INTERNAL
```

---

# 10. Public API 原则

只有以下内容允许进入 Public API：

```text
Developer-facing Contract
Extension SPI
Framework Configuration
Testing API
```

以下内容不得成为公共 API：

```text
Internal Registry
ThreadLocal Context Implementation
Broker Internals
Persistence Internals
Runtime Scheduling Internals
```

---

# 11. SPI

SPI 是 Framework 的核心扩展机制。

基本模型：

```text
Application
     ↓
Framework SPI
     ↓
Adapter
```

SPI 必须：

```text
small
stable
technology-neutral
testable
```

---

# 12. Repository SPI

Domain：

```java
public interface AggregateRepository<A, I> {

    Optional<A> findById(I id);

    void save(A aggregate);

    void delete(A aggregate);
}
```

Infrastructure：

```text
JDBC Repository
JPA Repository
MyBatis Repository
```

实现 Repository Contract。

---

# 13. Repository Registration

Runtime 启动时：

```text
Repository
      ↓
Registry
```

注册：

```java
repositoryRegistry.register(
    Order.class,
    orderRepository
);
```

要求：

```text
one Aggregate Type
→
one active Repository
```

---

# 14. Handler Registration

Command Handler：

```java
public interface CommandHandler<C extends Command<R>, R> {

    R handle(C command);
}
```

注册：

```java
commandRegistry.register(
    CreateOrderCommand.class,
    createOrderHandler
);
```

---

# 15. Handler Uniqueness

禁止：

```text
CreateOrderCommand
   ↓
Handler A
Handler B
```

除非显式支持：

```text
Composite Handler
```

默认：

```text
One Command Type
→
One Handler
```

---

# 16. Query Handler

```java
public interface QueryHandler<Q extends Query<R>, R> {

    R handle(Q query);
}
```

注册：

```java
queryRegistry.register(
    GetOrderQuery.class,
    getOrderHandler
);
```

---

# 17. Handler Registry

```java
public interface HandlerRegistry {

    <C, R> void registerCommand(
            Class<C> commandType,
            CommandHandler<C, R> handler);

    <Q, R> void registerQuery(
            Class<Q> queryType,
            QueryHandler<Q, R> handler);
}
```

Registry 必须在 Runtime Bootstrap 阶段完成。

---

# 18. Runtime Bootstrap

启动流程：

```text
Configuration
     ↓
Validate
     ↓
Create Registry
     ↓
Register Components
     ↓
Validate Dependency Graph
     ↓
Initialize Infrastructure
     ↓
Initialize Messaging
     ↓
Initialize Runtime
     ↓
READY
```

---

# 19. Bootstrap Failure

以下任一条件失败：

```text
Missing Repository
Missing Handler
Duplicate Handler
Invalid Configuration
Adapter Initialization Failure
Dependency Cycle
```

则：

```text
Runtime = FAILED
```

不得进入：

```text
RUNNING
```

---

# 20. Runtime Builder

推荐：

```java
public interface DddRuntimeBuilder {

    DddRuntimeBuilder configuration(
            DddConfiguration configuration);

    DddRuntimeBuilder register(
            Object component);

    DddRuntime build();
}
```

使用：

```java
DddRuntime runtime =
    DddRuntime.builder()
        .configuration(configuration)
        .register(orderRepository)
        .register(createOrderHandler)
        .build();
```

---

# 21. Explicit Registration

Framework 优先：

```text
Explicit Registration
```

而不是：

```text
Magic Discovery
```

原因：

- 可预测
- 可测试
- 可审计
- 启动错误明确
- 减少隐式行为

---

# 22. Optional SPI Discovery

允许：

```java
ServiceLoader
```

用于：

```text
Adapter Provider
Serializer Provider
Broker Provider
```

但：

```text
Explicit Registration
>
Automatic Discovery
```

---

# 23. DDD Runtime

Runtime 必须：

```text
start()
state()
shutdown()
```

生命周期：

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

异常：

```text
STARTING
 ↓
FAILED
```

---

# 24. Graceful Shutdown

Shutdown 顺序：

```text
Stop Inbound Messaging
        ↓
Stop New Commands
        ↓
Finish In-Flight Requests
        ↓
Flush Outbox
        ↓
Close Infrastructure
        ↓
STOPPED
```

不得直接：

```text
kill process
```

作为正常 Shutdown 策略。

---

# 25. Threading Model

Core：

```text
Thread-safe where required
```

Domain：

```text
No implicit ThreadLocal
```

Runtime：

```text
Explicit Executor
```

Messaging：

```text
Consumer Executor
```

不得把线程池隐藏成 Domain 行为。

---

# 26. Context Propagation

允许：

```text
Correlation ID
Trace Context
Tenant Context
Security Context
```

但必须显式定义传播边界。

禁止：

```text
ThreadLocal context
→
自动跨异步边界
```

---

# 27. Command Execution

最终执行链：

```text
CommandBus
 ↓
Middleware
 ↓
Transaction
 ↓
UnitOfWork
 ↓
CommandHandler
 ↓
Aggregate
 ↓
Repository
 ↓
Domain Event
 ↓
Outbox
 ↓
Commit
```

---

# 28. Query Execution

```text
QueryBus
 ↓
Middleware
 ↓
QueryHandler
 ↓
Read Repository
 ↓
Read Model
```

默认：

```text
No Domain UnitOfWork
```

---

# 29. Middleware

统一接口：

```java
public interface Middleware {

    <R> R execute(
            InvocationContext context,
            InvocationChain chain);
}
```

Command 默认：

```text
Tracing
 ↓
Validation
 ↓
Authorization
 ↓
Idempotency
 ↓
Transaction
 ↓
Handler
```

---

# 30. Middleware Ordering

Middleware 必须具有确定顺序。

禁止：

```text
Random Order
```

推荐：

```java
@Order(100)
Tracing

@Order(200)
Validation

@Order(300)
Authorization

@Order(400)
Idempotency

@Order(500)
Transaction
```

但 Annotation 仅为一种 Adapter 实现方式。

核心仍保持 Framework-neutral。

---

# 31. Transaction SPI

```java
public interface TransactionAdapter {

    <T> T execute(
            TransactionDefinition definition,
            TransactionCallback<T> callback);
}
```

Framework 不规定数据库技术。

---

# 32. UnitOfWork SPI

```java
public interface UnitOfWorkManager {

    boolean hasCurrent();

    UnitOfWork current();

    <T> T execute(
            UnitOfWorkCallback<T> callback);
}
```

---

# 33. In-Memory Runtime

必须提供：

```text
InMemoryRepository
InMemoryEventStore
InMemoryOutbox
InMemoryInbox
InMemoryMessageBus
InMemoryTransaction
```

用于：

```text
Unit Test
Integration Test
Conformance Test
Demo
Prototype
```

---

# 34. In-Memory Transaction

In-Memory Adapter 必须尽可能模拟：

```text
Commit
Rollback
Concurrency
Outbox Atomicity
Inbox Atomicity
```

不能因为是测试实现而完全忽略 Framework Contract。

---

# 35. Test Framework

新增：

```text
ddd-test
```

用于：

```text
Aggregate Test
Command Test
Query Test
Event Test
Transaction Test
Messaging Test
```

---

# 36. Aggregate Test

提供：

```java
AggregateTestFixture
```

示例：

```java
fixture
    .given(existingEvents)
    .when(command)
    .expectState(expectedState)
    .expectEvents(expectedEvents);
```

---

# 37. Given / When / Then

统一：

```text
Given
 ↓
When
 ↓
Then
```

例如：

```text
Given OrderCreated
When PayOrder
Then OrderPaid
```

---

# 38. Domain Event Test

验证：

```text
Event Type
Event Data
Event Count
Event Order
```

---

# 39. Command Test

验证：

```text
Command
 ↓
Handler
 ↓
Aggregate
 ↓
State
 ↓
Events
```

---

# 40. Query Test

验证：

```text
Query
 ↓
QueryHandler
 ↓
ReadModel
```

不要求启动：

```text
Broker
Database
```

---

# 41. Transaction Test

必须验证：

```text
Success
Rollback
Concurrency Conflict
Retry
Outbox Atomicity
```

---

# 42. Messaging Test

验证：

```text
Publish
Consume
Duplicate
Retry
DLQ
Replay
Ordering
```

---

# 43. Conformance Test Kit

新增：

```text
ddd-conformance
```

目标：

> 验证任何 Adapter 是否真正遵守 RegalPine DDD Framework Contract。

---

# 44. Repository Conformance

任何 Repository Adapter 必须通过：

```text
save
find
delete
identity
version
concurrency
transaction
```

测试。

---

# 45. Transaction Conformance

验证：

```text
commit
rollback
propagation
isolation mapping
timeout
context cleanup
```

---

# 46. Outbox Conformance

验证：

```text
append
load pending
publish
retry
mark published
duplicate prevention
```

---

# 47. Inbox Conformance

验证：

```text
new message
duplicate message
transaction rollback
commit
consumer isolation
```

---

# 48. Message Adapter Conformance

验证：

```text
publish
receive
ack
retry
ordering
consumer group
failure handling
```

---

# 49. Adapter Certification

Adapter 可以声明：

```text
RegalPine DDD Compatible
```

必须通过：

```text
ddd-conformance
```

而不是仅仅：

```text
implements Interface
```

---

# 50. Compatibility Levels

定义：

```text
CORE_COMPATIBLE
RUNTIME_COMPATIBLE
PERSISTENCE_COMPATIBLE
MESSAGING_COMPATIBLE
FULLY_CONFORMANT
```

---

# 51. Reference Implementation

官方 Reference Implementation 建议：

```text
Java 17
+
In-Memory
+
JDBC
+
PostgreSQL
```

结构：

```text
examples/
└── reference-order-service
```

---

# 52. Reference Application

使用典型：

```text
Order
Customer
Inventory
Payment
```

演示：

```text
Aggregate
Command
Query
Domain Event
Integration Event
Outbox
Inbox
Saga
CQRS
Transaction
```

---

# 53. Reference Flow

```text
CreateOrder
 ↓
Order Aggregate
 ↓
OrderCreated
 ↓
Outbox
 ↓
Integration Event
 ↓
Inventory
 ↓
ReserveInventoryCommand
 ↓
Inventory Aggregate
 ↓
InventoryReserved
 ↓
Order Process
```

---

# 54. Spring Boot Adapter

Spring Boot 只作为：

```text
Adapter
```

架构：

```text
Spring Boot
     ↓
ddd-spring-boot
     ↓
DddRuntime
     ↓
DDD Framework
```

不得：

```text
Spring
 ↓
Domain
```

---

# 55. Spring Bean Registration

允许：

```java
@Bean
OrderRepository orderRepository(...)
```

然后：

```text
Spring ApplicationContext
 ↓
Framework Registry
```

Spring 负责生命周期。

Framework 负责 DDD Runtime 语义。

---

# 56. Annotation Policy

Framework 可以提供：

```text
@CommandHandler
@QueryHandler
@Aggregate
@DomainService
```

但：

> Annotation 不得成为 Domain 正确性的唯一依据。

核心 Contract 必须可以：

```text
without annotations
```

运行。

---

# 57. Reflection Policy

Reflection 可以用于：

```text
Adapter Discovery
Annotation Scanning
Developer Convenience
```

但：

```text
Core Domain correctness
```

不得依赖 Reflection。

---

# 58. Error Model

统一 Framework Error：

```java
public interface FrameworkError {

    String code();

    String message();
}
```

错误分类：

```text
DOMAIN
APPLICATION
CONCURRENCY
TRANSACTION
INFRASTRUCTURE
MESSAGING
CONFIGURATION
RUNTIME
```

---

# 59. Exception Mapping

Domain：

```text
DomainException
```

Application：

```text
ApplicationException
```

Infrastructure：

```text
InfrastructureException
```

Messaging：

```text
MessagingException
```

禁止把：

```text
SQLException
KafkaException
HibernateException
```

直接暴露给 Domain。

---

# 60. Configuration

配置：

```java
public interface DddConfiguration {

    <T> T get(
            ConfigurationKey<T> key);
}
```

优先级：

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

# 61. Configuration Validation

启动必须验证：

```text
Required
Type
Range
Dependency
Conflict
Security
```

失败：

```text
Fail Fast
```

---

# 62. Secrets

Configuration API 不得：

```text
log secret
serialize secret
expose secret
```

例如：

```text
password
private key
access token
API secret
```

必须使用：

```text
Secret Provider Adapter
```

---

# 63. API Documentation

每一个 Stable API 必须具有：

```text
Purpose
Contract
Thread Safety
Lifecycle
Exception
Example
```

---

# 64. Semantic Versioning

采用：

```text
MAJOR.MINOR.PATCH
```

例如：

```text
1.0.0
1.1.0
1.1.1
2.0.0
```

---

# 65. Breaking Changes

以下属于 Major：

```text
删除 Public API
改变方法语义
改变默认行为
改变事务语义
改变消息兼容策略
改变 Repository Contract
```

---

# 66. Non-Breaking Changes

通常属于 Minor：

```text
新增 API
新增 Adapter
新增 Optional Configuration
新增 Test Utility
```

---

# 67. Patch

Patch 仅用于：

```text
Bug Fix
Security Fix
Performance Fix
Documentation Correction
```

不得在 Patch 中改变 Framework 语义。

---

# 68. Deprecation

删除 API 前：

```text
Stable
 ↓
Deprecated
 ↓
Removal Notice
 ↓
Major Version
 ↓
Removed
```

至少保证一个合理迁移窗口。

---

# 69. Compatibility

Framework 必须区分：

```text
Source Compatibility
Binary Compatibility
Behavior Compatibility
Persistence Compatibility
Message Compatibility
```

---

# 70. Persistence Compatibility

数据库 Schema 不属于：

```text
ddd-domain
```

由：

```text
Infrastructure
Migration
```

管理。

---

# 71. Event Compatibility

Integration Event 必须独立管理版本：

```text
Framework Version
≠
Event Schema Version
```

例如：

```text
Framework 2.0
Event order.created v1
```

仍然可以合法存在。

---

# 72. Developer Experience

开发者最小使用模型：

```java
public final class CreateOrderHandler
        implements CommandHandler<CreateOrderCommand, OrderId> {

    private final OrderRepository repository;

    @Override
    public OrderId handle(CreateOrderCommand command) {

        Order order = Order.create(
                command.customerId());

        repository.save(order);

        return order.id();
    }
}
```

开发者不应该关心：

```text
Transaction Connection
Outbox Insert
ACK
Retry
ThreadLocal Cleanup
Broker Connection
```

这些由 Framework Runtime 负责。

---

# 73. Developer Programming Model

最终开发者主要关注：

```text
Domain
Application
Ports
```

而不是：

```text
Runtime Internals
```

---

# 74. Minimal Application

最小应用：

```text
Aggregate
Repository
Command
CommandHandler
Query
QueryHandler
```

即可运行。

Messaging、Saga、Kafka 等均为：

```text
Optional Capability
```

---

# 75. Enterprise Application

企业应用可以启用：

```text
CQRS
Outbox
Inbox
Kafka
Saga
Tracing
Metrics
Authorization
Multi-Tenant
```

但这些能力不得强迫所有应用引入。

---

# 76. Framework Layering

最终开发者体验：

```text
                    Application
                         │
               ┌─────────┴─────────┐
               │                   │
             Command              Query
               │                   │
             Domain             ReadModel
               │                   │
               └─────────┬─────────┘
                         │
                       Runtime
                         │
             ┌───────────┼───────────┐
             │           │           │
         Transaction   Messaging   Repository
             │           │           │
             └───────────┼───────────┘
                         │
                    Infrastructure
```

---

# 77. Testing Pyramid

```text
             E2E
              ▲
              │
        Integration
              ▲
              │
       Conformance
              ▲
              │
        Application
              ▲
              │
          Domain
```

Domain Test 应该最多。

E2E Test 应该最少。

---

# 78. Contract Testing

Adapter 必须采用：

```text
Contract Test
```

而不是仅依赖：

```text
Mock Test
```

例如：

```text
Kafka Adapter
 ↓
Messaging Contract
 ↓
Conformance Suite
```

---

# 79. CI Pipeline

推荐：

```text
Compile
 ↓
Unit Test
 ↓
Architecture Test
 ↓
Conformance Test
 ↓
Integration Test
 ↓
Compatibility Test
 ↓
Security Scan
 ↓
Package
 ↓
Release
```

---

# 80. Architecture Test

CI 必须验证：

```text
ddd-core
  cannot depend on Spring

ddd-domain
  cannot depend on infrastructure

ddd-application
  cannot depend on broker implementation

adapter
  can depend on framework contracts
```

---

# 81. Architecture Enforcement

可以使用：

```text
ArchUnit
```

但 ArchUnit 属于：

```text
Test Tool
```

而不是 Framework Runtime Dependency。

---

# 82. Example Architecture Rule

概念规则：

```text
Domain
must not access
Infrastructure
```

以及：

```text
Core
must not access
Spring
```

---

# 83. Performance Baseline

Framework 本身不得成为主要性能瓶颈。

关键路径：

```text
Command
 ↓
Handler
 ↓
Aggregate
```

应该保持：

```text
low allocation
minimal reflection
minimal locking
```

---

# 84. Reflection Isolation

如果使用 Reflection：

```text
Bootstrap Time
```

完成扫描。

运行时尽量使用：

```text
Direct Invocation
```

而不是：

```text
Reflection per request
```

---

# 85. Runtime Performance

Runtime 应尽量避免：

```text
Global Lock
Global synchronized Registry
Unbounded Queue
Unbounded Retry
Unbounded Cache
```

---

# 86. Resource Management

所有 Infrastructure Adapter 必须实现：

```text
start
stop
close
```

资源包括：

```text
Connection Pool
Executor
Broker Connection
Scheduler
Thread
File Handle
```

---

# 87. Resource Leak Prevention

Runtime Shutdown 必须确保：

```text
Executor terminated
Connection closed
Consumer stopped
Scheduler stopped
```

---

# 88. Observability Baseline

Framework 至少支持：

```text
Health
Metrics
Tracing
Diagnostics
```

指标：

```text
command.count
command.duration
command.failure
query.count
query.duration
transaction.failure
concurrency.conflict
outbox.backlog
inbox.duplicate
message.retry
message.dlq
```

---

# 89. Health Model

定义：

```text
Liveness
Readiness
Degraded
Failed
```

例如：

```text
Database unavailable
```

可能：

```text
Liveness = UP
Readiness = DOWN
```

---

# 90. Security Baseline

Framework 默认：

```text
Secure by Default
```

必须避免：

```text
Plaintext Secret
Sensitive Logging
Cross-Tenant Access
Unauthenticated Diagnostics
Uncontrolled Replay
```

---

# 91. Multi-Tenant Boundary

Framework 不强制 Tenant Domain Model。

但是 Runtime / Infrastructure 可以提供：

```text
TenantContext
TenantAwareRepository
TenantAwareMessage
```

Tenant Isolation 必须在：

```text
Persistence
Messaging
Authorization
```

等边界实施。

---

# 92. Framework 不承担业务授权

Framework 可以提供：

```text
Authorization SPI
```

但业务权限仍由：

```text
Application Policy
```

定义。

---

# 93. Developer CLI

CLI 不属于 Core。

未来可以提供：

```text
regalpine-ddd
```

用于：

```text
project init
generate test
validate architecture
run conformance
```

但 CLI 不应成为 Framework Runtime 必需组件。

---

# 94. Project Template

建议提供：

```text
ddd-starter
```

最小项目：

```text
domain
application
infrastructure
bootstrap
```

默认不生成：

```text
Kafka
Saga
JPA
Redis
```

---

# 95. Example Project

```text
order-service
├── domain
│   ├── order
│   └── customer
│
├── application
│   ├── command
│   └── query
│
├── infrastructure
│   ├── persistence
│   └── messaging
│
└── bootstrap
```

---

# 96. Framework Usage Boundary

开发者允许：

```text
Use Framework API
Implement SPI
Implement Domain
Configure Runtime
```

开发者不应该：

```text
Modify Framework Internal
Access Internal Registry
Directly manipulate UoW
Directly publish Broker Message
```

---

# 97. Internal API

所有内部 API 推荐：

```text
internal
```

或：

```text
runtime.internal
```

并明确：

```text
NOT PUBLIC API
```

---

# 98. Extension Point Classification

扩展点分为：

```text
Domain Extension
Application Extension
Runtime Extension
Infrastructure Extension
Messaging Extension
Test Extension
```

---

# 99. Domain Extension

允许：

```text
Entity
Value Object
Aggregate
Domain Service
Specification
Domain Event
Repository Contract
```

---

# 100. Application Extension

允许：

```text
Command
Query
Handler
Middleware
Process Manager
Saga
Authorization
Idempotency
```

---

# 101. Runtime Extension

允许：

```text
RuntimeComponent
RuntimeContext
Configuration
Health
Metrics
Lifecycle
```

---

# 102. Infrastructure Extension

允许：

```text
Repository Adapter
Transaction Adapter
Outbox Store
Inbox Store
Cache Adapter
```

---

# 103. Messaging Extension

允许：

```text
MessagePublisher
MessageConsumer
BrokerAdapter
Serializer
SchemaRegistry
RetryPolicy
```

---

# 104. Test Extension

允许：

```text
Fixture
Fake Repository
InMemory Transaction
InMemory Bus
Conformance Test
```

---

# 105. Conformance Matrix

| Contract | Required |
|---|---:|
| Core Model | YES |
| Domain Contract | YES |
| Application Contract | YES |
| CQRS | YES |
| Transaction | YES |
| Runtime | YES |
| Messaging | YES |
| Repository SPI | YES |
| In-Memory Adapter | YES |
| Test Framework | YES |
| Conformance Kit | YES |
| Kafka | NO |
| RabbitMQ | NO |
| JPA | NO |
| Spring Boot | NO |
| Redis | NO |

---

# 106. Definition of Done

RegalPine DDD Framework 1.0 必须满足：

```text
[✓] Core API Stable
[✓] Domain API Stable
[✓] Application API Stable
[✓] CQRS Stable
[✓] Event Contract Stable
[✓] Transaction Contract Stable
[✓] Runtime Stable
[✓] Messaging Contract Stable
[✓] SPI Stable
[✓] In-Memory Reference Implementation
[✓] Test Framework
[✓] Conformance Test
[✓] Reference Application
[✓] Documentation
[✓] Compatibility Policy
```

---

# 107. Phase I–X 总体状态

```text
Phase I
Ontology / Architecture
        ↓
Phase II
Core API
        ↓
Phase III
Domain API
        ↓
Phase IV
Application API
        ↓
Phase V
CQRS / Event
        ↓
Phase VI
Transaction / UoW
        ↓
Phase VII
Consistency / Reliability
        ↓
Phase VIII
Runtime / Infrastructure
        ↓
Phase IX
Integration / Messaging
        ↓
Phase X
Implementation / Conformance
```

---

# 108. 架构成熟度

当前 Framework 已经覆盖：

```text
                  ┌──────────────┐
                  │ Developer DX │
                  └──────┬───────┘
                         │
              ┌──────────▼──────────┐
              │ Conformance / Test  │
              └──────────┬──────────┘
                         │
              ┌──────────▼──────────┐
              │ Runtime / Adapter   │
              └──────────┬──────────┘
                         │
              ┌──────────▼──────────┐
              │ Messaging / Event   │
              └──────────┬──────────┘
                         │
              ┌──────────▼──────────┐
              │ CQRS / Application  │
              └──────────┬──────────┘
                         │
              ┌──────────▼──────────┐
              │      Domain         │
              └──────────┬──────────┘
                         │
              ┌──────────▼──────────┐
              │       Core          │
              └─────────────────────┘
```

---

# 109. 关键收敛决策

从 Phase X 开始：

## 不再新增核心 DDD 类型

除非发现：

```text
现有模型无法表达基本语义
```

否则不得新增：

```text
Manager
Context
Coordinator
Engine
Orchestrator
Registry
Model
Descriptor
```

作为新的领域抽象。

---

# 110. “Framework” 与 “Platform” 分界

RegalPine DDD Framework：

```text
提供开发模型
提供运行时
提供 SPI
提供 Adapter
提供 Test
提供 Conformance
```

但不负责：

```text
企业 IAM
企业流程管理
组织管理
租户管理平台
DevOps 平台
API Gateway
服务治理平台
```

这些属于：

```text
Enterprise Platform
```

而不是 DDD Framework Core。

---

# 111. Framework 与 Spring 的最终关系

```text
              RegalPine DDD
                    │
        ┌───────────┴───────────┐
        │                       │
   Plain Java                Spring Boot
        │                       │
        ▼                       ▼
 ddd-runtime             ddd-spring-boot
        │                       │
        └───────────┬───────────┘
                    ▼
             Same DDD Contracts
```

因此：

> Spring 是部署方式之一，而不是 RegalPine DDD 的架构基础。

---

# 112. Framework 与数据库的最终关系

```text
Domain
  │
Repository Contract
  │
  ▼
Infrastructure Adapter
  │
 ┌┼──────────────┐
 ▼              ▼
JDBC            JPA
```

因此：

> 数据库技术可以替换，而 Domain Model 不应因此改变。

---

# 113. Framework 与消息中间件的最终关系

```text
Integration Event
       │
       ▼
Message Contract
       │
       ▼
Broker Adapter
       │
 ┌─────┼───────────┐
 ▼     ▼           ▼
Kafka RabbitMQ    Pulsar
```

因此：

> 消息中间件是 Adapter，而不是 Framework 的核心编程模型。

---

# 114. 最终稳定架构

RegalPine DDD Framework 的核心稳定模型：

```text
                    DDD CORE
                       │
                ┌──────▼──────┐
                │   Domain    │
                └──────┬──────┘
                       │
                ┌──────▼──────┐
                │ Application │
                └──────┬──────┘
                       │
              ┌────────▼────────┐
              │ CQRS / Event    │
              └────────┬────────┘
                       │
             ┌─────────▼─────────┐
             │ Transaction / UoW │
             └─────────┬─────────┘
                       │
                 ┌─────▼─────┐
                 │  Runtime  │
                 └─────┬─────┘
                       │
              ┌────────▼────────┐
              │    Messaging    │
              └────────┬────────┘
                       │
              ┌────────▼────────┐
              │ Infrastructure  │
              └─────────────────┘
```

---

# 115. Phase X 结论

Phase X 完成后，RegalPine DDD Framework 已经从：

```text
DDD Architecture
```

发展为：

```text
DDD Developer Framework
```

其核心特征为：

```text
Java 17
+
Pure Domain
+
Four-Layer DDD
+
Hexagonal Architecture
+
CQRS
+
Domain Event
+
Integration Event
+
Transaction / UoW
+
Outbox / Inbox
+
Runtime
+
SPI
+
Adapters
+
Test Framework
+
Conformance
```

最重要的架构结论：

> **RegalPine DDD Framework 的 1.0 设计面已经基本具备收敛条件。**

后续工作的重点不应再是不断增加架构层次，而应该转入：

```text
实现
 ↓
测试
 ↓
Benchmark
 ↓
Conformance
 ↓
真实业务验证
 ↓
1.0 Release
```

---

# 116. 下一阶段

Phase XI 不再继续增加新的 DDD 理论抽象。

建议进入：

## Phase XI — Reference Implementation Specification

重点：

```text
1. ddd-core 完整 Java 17 实现
2. ddd-domain 完整 API
3. ddd-application 完整 API
4. ddd-cqrs 完整实现
5. ddd-event 完整实现
6. ddd-transaction 完整实现
7. ddd-runtime 完整实现
8. ddd-messaging 完整实现
9. In-Memory Infrastructure
10. JDBC Infrastructure
11. Outbox / Inbox
12. Reference Order 示例
13. Maven Parent / BOM
14. 完整 Package Structure
15. API Javadoc
16. 单元测试
17. Conformance Test
```

Phase XI 的原则是：

> **冻结架构、开始写代码。**