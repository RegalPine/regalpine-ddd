# RegalPine DDD Framework
## DDD Infrastructure Framework Specification

**GroupId:** `io.github.regalpine.ddd`  
**Artifact Prefix:** `ddd-*`  
**Specification Version:** `0.1  
**Status:** Architecture Baseline  
**Language:** Java 17+  
**Build:** Maven  
**Architecture:** DDD + Classical Four-Layer + Hexagonal Architecture + CQRS  
**DSL:** None  

---

# 1. 文档定位

本文定义一套面向企业级 Java 应用开发的 **DDD 基础设施开发框架**。

本框架不是：

- DDD 建模 DSL
- 新的编程语言
- BPMN 替代品
- Agent DSL
- ORM 框架
- Web MVC 框架
- 消息中间件
- 完整微服务框架

本框架的目标是：

> 将 Domain-Driven Design 中稳定、通用、可复用的架构能力沉淀为 Java 开发基础设施，使开发人员可以直接使用代码、接口、抽象类型和基础设施组件构建 DDD 应用。

因此：

```text
DDD Modeling
      ↓
Java Domain Model
      ↓
DDD Framework
      ↓
Application
      ↓
Infrastructure
```

而不是：

```text
DDD Model
   ↓
DDD DSL
   ↓
DSL Compiler
   ↓
Application
```

---

# 2. 设计目标

## 2.1 核心目标

框架需要解决以下问题：

1. 如何定义 Aggregate
2. 如何定义 Entity
3. 如何定义 Value Object
4. 如何定义 Domain Service
5. 如何定义 Domain Event
6. 如何定义 Repository
7. 如何定义 Application Service
8. 如何实现 Command / Query
9. 如何实现 Transaction Boundary
10. 如何实现 Domain Event Dispatch
11. 如何实现 Outbox
12. 如何实现领域模型与基础设施解耦
13. 如何支持同步与异步执行
14. 如何支持模块化单体和微服务
15. 如何支持测试
16. 如何支持审计、幂等、并发控制
17. 如何避免 Domain 层依赖 Spring/JPA/数据库/MQ

---

# 3. 核心设计原则

## 3.1 Domain First

领域模型是系统核心。

依赖方向：

```text
Interface
    ↓
Application
    ↓
Domain
    ↑
Infrastructure
```

Infrastructure 不得成为 Domain 的控制中心。

---

# 4. 总体架构

框架采用：

```text
                Interface Layer
                       │
                       ▼
                Application Layer
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
        Command Side        Query Side
             │                   │
             └─────────┬─────────┘
                       ▼
                 Domain Layer
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
        Domain Model       Domain Events
             │
             ▼
          Ports
             │
             ▼
     Infrastructure Adapters
             │
      ┌──────┼──────┬──────┐
      ▼      ▼      ▼      ▼
     DB      MQ    Cache   External API
```

对应经典四层：

```text
┌─────────────────────────────┐
│ Interface                   │
├─────────────────────────────┤
│ Application                 │
├─────────────────────────────┤
│ Domain                      │
├─────────────────────────────┤
│ Infrastructure              │
└─────────────────────────────┘
```

同时采用六边形架构：

```text
              Adapter
                 │
                 ▼
          ┌─────────────┐
          │ Input Port  │
          └──────┬──────┘
                 │
                 ▼
          ┌─────────────┐
          │ Application │
          │   Domain    │
          └──────┬──────┘
                 │
                 ▼
          ┌─────────────┐
          │ Output Port │
          └──────┬──────┘
                 │
                 ▼
              Adapter
```

两种架构不是竞争关系。

本框架规定：

> **四层架构用于系统分层，六边形架构用于依赖方向和端口适配。**

---

# 5. DDD 本体论

## 5.1 本体论定位

本框架建立一个轻量级 DDD Ontology，用于定义：

- 概念
- 类型
- 关系
- 生命周期
- 所属边界
- 约束
- 语义

本体论不要求用户编写任何 DSL。

本体论最终通过：

- Java 类型
- Interface
- Annotation（可选）
- Module
- Package
- Runtime metadata

表达。

---

# 6. DDD Ontology 顶层模型

核心概念：

```text
DDDSystem
 ├── BoundedContext
 │     ├── Aggregate
 │     │     ├── AggregateRoot
 │     │     ├── Entity
 │     │     └── ValueObject
 │     │
 │     ├── DomainService
 │     ├── DomainEvent
 │     ├── Repository
 │     └── DomainPolicy
 │
 ├── Application
 │     ├── Command
 │     ├── CommandHandler
 │     ├── Query
 │     ├── QueryHandler
 │     └── ApplicationService
 │
 └── Infrastructure
       ├── Adapter
       ├── Persistence
       ├── Messaging
       ├── Cache
       └── ExternalSystem
```

---

# 7. Ontology Entity 定义

## 7.1 DDDSystem

表示一个完整领域驱动系统。

```text
DDDSystem
```

属性：

| 属性 | 类型 |
|---|---|
| id | SystemId |
| name | String |
| version | Version |
| contexts | Set<BoundedContext> |

---

# 8. Bounded Context

```text
BoundedContext
```

表示明确的领域语义边界。

属性：

```text
BoundedContext
 ├── id
 ├── name
 ├── domain
 ├── aggregates
 ├── services
 ├── events
 └── repositories
```

核心原则：

> 一个 Bounded Context 内部可以拥有完整领域模型，但不同 Context 不应共享内部领域对象。

允许：

```text
Context A
   │
   │ Integration
   ▼
Context B
```

不允许：

```text
Context A
   │
   └──── shared internal Entity ──── Context B
```

---

# 9. Aggregate

Aggregate 是事务一致性边界。

```text
Aggregate
```

核心属性：

```text
Aggregate
 ├── identity
 ├── root
 ├── entities
 ├── valueObjects
 ├── invariants
 └── lifecycle
```

核心规则：

> 一个 Aggregate 必须存在且只能存在一个 Aggregate Root。

---

# 10. Aggregate Root

Aggregate Root 是 Aggregate 对外暴露的唯一实体入口。

```java
public interface AggregateRoot<ID> {

    ID id();

    long version();
}
```

Aggregate 内部 Entity 不允许被外部 Application Layer 直接操作。

错误：

```java
orderItemRepository.findById(...)
```

正确：

```java
orderRepository.findById(orderId)
    .addItem(productId, quantity);
```

---

# 11. Entity

Entity 由身份定义。

```java
public interface Entity<ID> {

    ID id();
}
```

Entity 的生命周期独立于 Value Object。

---

# 12. Value Object

Value Object 不强调身份，而强调值语义。

```java
public interface ValueObject {
}
```

推荐：

```java
public record Money(
    BigDecimal amount,
    Currency currency
) implements ValueObject {
}
```

Value Object 应尽量：

- Immutable
- Side-effect free
- Equality by value

---

# 13. Domain Service

当业务行为：

- 不自然属于某一个 Entity
- 不自然属于某一个 Aggregate
- 但明显属于领域

使用 Domain Service。

```java
public interface DomainService {
}
```

示例：

```java
public interface PricingService extends DomainService {

    Money calculatePrice(
        Product product,
        Customer customer
    );
}
```

Domain Service 不得：

- 访问数据库
- 发送 HTTP
- 操作 MQ
- 操作 Redis
- 操作文件系统

如果需要外部能力，应通过 Domain Port。

---

# 14. Domain Policy

Policy 表示稳定业务规则。

```java
public interface DomainPolicy {
}
```

例如：

```text
CreditPolicy
PricingPolicy
RiskPolicy
DiscountPolicy
```

Policy 应尽量：

```text
Input
 ↓
Business Rule
 ↓
Decision
```

而不是承担 Infrastructure 行为。

---

# 15. Domain Event

Domain Event 表示：

> 领域内部已经发生的业务事实。

```java
public interface DomainEvent {

    EventId eventId();

    Instant occurredAt();

    String aggregateType();

    String aggregateId();
}
```

例如：

```text
OrderCreated
OrderPaid
OrderCancelled
CustomerRegistered
```

事件使用过去时命名。

推荐：

```text
OrderCreated
```

不推荐：

```text
CreateOrderEvent
```

---

# 16. Domain Event 生命周期

```text
Aggregate
   │
   │ business operation
   ▼
State Change
   │
   ▼
Domain Event
   │
   ▼
Event Collection
   │
   ▼
Application Transaction
   │
   ▼
Event Publication
```

Aggregate 不直接操作 MQ。

错误：

```java
kafkaTemplate.send(...)
```

正确：

```text
Aggregate
   ↓
DomainEvent
   ↓
Application
   ↓
EventPublisher Port
   ↓
Infrastructure Adapter
   ↓
Kafka
```

---

# 17. Repository Ontology

Repository 表示 Aggregate 的持久化抽象。

```java
public interface Repository<
    A extends AggregateRoot<ID>,
    ID
> {

    Optional<A> findById(ID id);

    void save(A aggregate);

    void remove(A aggregate);
}
```

Repository 必须以 Aggregate 为主要边界。

不推荐：

```java
UserRepository
OrderItemRepository
PaymentRecordRepository
```

如果这些对象只是 Aggregate 内部 Entity。

推荐：

```text
OrderRepository
CustomerRepository
PaymentRepository
```

---

# 18. Specification

DDD Specification 用于封装可组合业务判断。

```java
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);

    default Specification<T> and(
        Specification<T> other
    ) {
        return candidate ->
            isSatisfiedBy(candidate)
            && other.isSatisfiedBy(candidate);
    }

    default Specification<T> or(
        Specification<T> other
    ) {
        return candidate ->
            isSatisfiedBy(candidate)
            || other.isSatisfiedBy(candidate);
    }

    default Specification<T> not() {
        return candidate ->
            !isSatisfiedBy(candidate);
    }
}
```

---

# 19. Domain Invariant

Invariant 表示 Aggregate 必须持续满足的业务不变量。

例如：

```text
Order
 ├── total >= 0
 ├── status transition valid
 ├── paid order cannot be cancelled
 └── item quantity > 0
```

Invariant 必须尽可能位于 Domain Layer。

原则：

> 不变量必须在任何有效的 Domain 状态变更后成立。

---

# 20. Domain Lifecycle

领域对象生命周期：

```text
Created
   ↓
Active
   ↓
Modified
   ↓
Completed
   ↓
Archived
```

具体生命周期由领域定义。

框架只提供生命周期基础设施，不规定业务生命周期。

---

# 21. Application Ontology

Application Layer 负责：

- Use Case
- Command
- Query
- Transaction
- Authorization boundary
- Idempotency
- Orchestration

Application 不应该包含核心业务规则。

---

# 22. Command

Command 表示：

> 请求系统执行一个业务动作。

```java
public interface Command<R> {
}
```

例如：

```java
public record CreateOrderCommand(
    CustomerId customerId,
    List<OrderItemInput> items
) implements Command<OrderId> {
}
```

Command 是输入模型，不应该直接复用 Domain Entity。

---

# 23. Command Handler

```java
public interface CommandHandler<
    C extends Command<R>,
    R
> {

    R handle(C command);
}
```

Command Handler：

```text
Command
   ↓
Load Aggregate
   ↓
Invoke Domain Behavior
   ↓
Persist Aggregate
   ↓
Commit
   ↓
Publish Domain Events
```

---

# 24. Query

Query 表示：

> 获取数据，而不是改变业务状态。

```java
public interface Query<R> {
}
```

例如：

```java
public record GetOrderQuery(
    OrderId orderId
) implements Query<OrderView> {
}
```

---

# 25. Query Handler

```java
public interface QueryHandler<
    Q extends Query<R>,
    R
> {

    R handle(Q query);
}
```

Query Handler 可以：

- 使用 Read Repository
- 使用 SQL
- 使用 Elasticsearch
- 使用缓存
- 使用投影表

不应该修改 Domain State。

---

# 26. CQRS

框架采用 CQRS，但不强制：

- Event Sourcing
- 独立数据库
- 独立微服务
- Kafka
- 最终一致性

CQRS 最低级别定义：

```text
              Application
              /         \
             /           \
        Command          Query
           │                │
           ▼                ▼
       Write Model      Read Model
           │                │
           ▼                ▼
        Aggregate       Projection
```

---

# 27. CQRS 分级

## Level 1 — Logical CQRS

读写接口分离：

```text
CommandHandler
QueryHandler
```

但共享数据库。

这是默认模式。

---

## Level 2 — Physical CQRS

读写模型使用不同 Repository。

```text
Write DB
    │
    ▼
Read Model
```

---

## Level 3 — Event Driven CQRS

```text
Aggregate
   ↓
Domain Event
   ↓
Event Bus
   ↓
Projection
   ↓
Read Model
```

---

## Level 4 — Event Sourcing

框架允许适配，但不强制。

```text
Command
 ↓
Aggregate
 ↓
Event
 ↓
Event Store
 ↓
Aggregate Rehydration
```

---

# 28. Hexagonal Architecture

本框架定义两类 Port。

## Input Port

系统被外部调用。

```text
REST
GraphQL
gRPC
CLI
Message Consumer
Scheduler
```

通过：

```java
UseCase
CommandHandler
QueryHandler
```

进入 Application。

---

# 29. Output Port

系统访问外部能力。

例如：

```text
Repository
EventPublisher
Clock
IdGenerator
PaymentGateway
NotificationService
FileStorage
```

通过 Port 隔离。

---

# 30. Port 设计原则

Port 必须描述业务需要，而不是技术实现。

错误：

```java
KafkaProducerPort
RedisClientPort
JdbcTemplatePort
```

推荐：

```java
EventPublisher
PaymentGateway
CustomerRepository
NotificationGateway
```

Infrastructure 再决定：

```text
KafkaEventPublisher
RedisCustomerCache
JpaCustomerRepository
```

---

# 31. Four-Layer Architecture

## 31.1 Interface Layer

负责：

- REST
- GraphQL
- gRPC
- Message Consumer
- DTO
- Request Mapping
- Response Mapping

不得：

- 写领域规则
- 操作 Repository
- 操作数据库

---

# 32. Application Layer

负责：

- Use Case
- Command
- Query
- Handler
- Transaction
- Security Context
- Idempotency
- Domain Event Coordination

---

# 33. Domain Layer

负责：

- Aggregate
- Entity
- Value Object
- Domain Service
- Domain Policy
- Domain Event
- Specification
- Business Invariant

Domain Layer 应保持技术无关。

---

# 34. Infrastructure Layer

负责：

- Database
- ORM
- Redis
- MQ
- HTTP Client
- File System
- Cache
- Search
- Observability
- Security Provider

---

# 35. 依赖规则

必须满足：

```text
Interface
    ↓
Application
    ↓
Domain

Infrastructure
    ↓
Application / Domain Ports
```

禁止：

```text
Domain → Infrastructure
Domain → Spring
Domain → Hibernate
Domain → Kafka
Domain → Redis
Domain → HTTP Client
```

---

# 36. Maven 模块设计

建议第一阶段建立：

```text
ddd-parent
│
├── ddd-core
├── ddd-domain
├── ddd-application
├── ddd-cqrs
├── ddd-port
├── ddd-event
├── ddd-transaction
├── ddd-infrastructure
├── ddd-test
└── ddd-spring-boot
```

GroupId：

```xml
<groupId>io.github.regalpine.ddd</groupId>
```

---

# 37. ddd-core

基础类型：

```text
Entity
ValueObject
AggregateRoot
Identifier
DomainEvent
Specification
DomainException
```

依赖：

```text
Java SE
```

原则：

> ddd-core 不依赖 Spring。

---

# 38. ddd-domain

提供：

```text
Aggregate
DomainService
DomainPolicy
Repository
DomainEvent
DomainEventPublisher
DomainSpecification
```

依赖：

```text
ddd-core
```

---

# 39. ddd-application

提供：

```text
Command
CommandHandler
Query
QueryHandler
UseCase
ApplicationService
TransactionBoundary
ExecutionContext
```

依赖：

```text
ddd-domain
```

---

# 40. ddd-cqrs

提供：

```text
CommandBus
QueryBus
CommandMiddleware
QueryMiddleware
CommandHandlerRegistry
QueryHandlerRegistry
```

例如：

```java
public interface CommandBus {

    <R> R dispatch(Command<R> command);
}
```

---

# 41. ddd-port

提供通用输出端口：

```text
Repository
EventPublisher
UnitOfWork
Clock
IdGenerator
LockManager
TransactionManager
```

但不绑定实现。

---

# 42. ddd-event

负责：

```text
DomainEvent
EventEnvelope
EventPublisher
EventSubscriber
EventDispatcher
EventStore
Outbox
```

事件模型：

```text
Event
 ├── eventId
 ├── eventType
 ├── aggregateType
 ├── aggregateId
 ├── occurredAt
 ├── sequence
 ├── metadata
 └── payload
```

---

# 43. ddd-transaction

提供：

```text
TransactionBoundary
TransactionManager
UnitOfWork
TransactionContext
```

核心目标：

```text
Application Use Case
        │
        ▼
Transaction Boundary
        │
 ┌──────┴──────┐
 ▼             ▼
Aggregate    Domain Event
 │             │
 └──────┬──────┘
        ▼
      Commit
```

---

# 44. ddd-infrastructure

Infrastructure 不实现具体技术绑定，而提供基础 SPI 和扩展点。

可进一步拆分：

```text
ddd-infrastructure-jdbc
ddd-infrastructure-jpa
ddd-infrastructure-redis
ddd-infrastructure-kafka
ddd-infrastructure-http
```

这些都是 Adapter。

---

# 45. ddd-test

提供：

```text
AggregateTest
DomainTest
CommandTest
QueryTest
EventTest
Fixture
TestDataBuilder
```

目标：

```java
AggregateTest<Order>
```

能够快速验证：

```text
Command
→
Aggregate
→
Event
→
Invariant
```

---

# 46. ddd-spring-boot

Spring Boot 只是 Adapter。

例如：

```text
ddd-spring-boot-starter
ddd-spring-boot-autoconfigure
```

不得反向污染：

```text
ddd-core
ddd-domain
ddd-application
```

---

# 47. Spring 集成原则

推荐：

```text
Spring Boot
     │
     ▼
DDD Framework Adapter
     │
     ▼
DDD Application
```

而不是：

```text
Spring
 ↓
Domain
```

Domain 可以在没有 Spring Container 的情况下运行。

---

# 48. Repository SPI

基础接口：

```java
public interface Repository<A, ID> {

    Optional<A> findById(ID id);

    void save(A aggregate);

    void remove(A aggregate);
}
```

数据库 Adapter：

```text
JpaRepositoryAdapter
JdbcRepositoryAdapter
MyBatisRepositoryAdapter
MongoRepositoryAdapter
```

均实现：

```text
Repository
```

---

# 49. Unit of Work

Unit of Work 用于维护一次业务操作中的聚合状态。

```java
public interface UnitOfWork {

    void registerNew(Object aggregate);

    void registerDirty(Object aggregate);

    void registerRemoved(Object aggregate);

    void commit();

    void rollback();
}
```

典型流程：

```text
Command
 ↓
UnitOfWork.begin()
 ↓
Load Aggregate
 ↓
Modify
 ↓
Collect Events
 ↓
Repository
 ↓
Commit
```

---

# 50. Optimistic Concurrency

Aggregate 默认支持版本控制：

```java
public interface Versioned {

    long version();
}
```

更新：

```text
Expected Version = 10
Actual Version   = 10
        ↓
UPDATE
        ↓
Version = 11
```

冲突：

```text
Expected = 10
Actual   = 11
        ↓
ConcurrencyConflictException
```

---

# 51. Idempotency

Command Framework 支持：

```text
IdempotencyKey
```

模型：

```text
Command
 ├── commandId
 ├── idempotencyKey
 ├── issuedAt
 └── payload
```

保证：

```text
Same Idempotency Key
        ↓
Same logical operation
        ↓
No duplicate execution
```

---

# 52. Execution Context

Application 执行上下文：

```java
public interface ExecutionContext {

    String tenantId();

    String principalId();

    String correlationId();

    String causationId();
}
```

可扩展：

```text
Locale
Timezone
TraceId
SecurityContext
RequestMetadata
```

---

# 53. Tenant Context

企业应用可支持多租户。

但：

> Tenant 不是 Domain Entity 的强制属性。

租户属于 Execution Context / Application Context 的基础设施能力。

---

# 54. Security Boundary

DDD Framework 不实现完整 IAM。

框架只提供：

```java
public interface SecurityContext {

    Optional<String> principalId();

    boolean hasPermission(String permission);
}
```

具体 IAM 可以由：

```text
OAuth2
OIDC
JWT
Enterprise IAM
Keycloak
Self-developed IAM
```

Adapter 实现。

---

# 55. Event Envelope

Domain Event 在跨进程传播时包装：

```java
public record EventEnvelope(
    String eventId,
    String eventType,
    String aggregateType,
    String aggregateId,
    long sequence,
    Instant occurredAt,
    String correlationId,
    String causationId,
    Object payload
) {
}
```

Domain Event 与 Integration Event 分离。

---

# 56. Domain Event vs Integration Event

这是框架的重要边界。

```text
Domain Event
    ↓
Application
    ↓
Integration Event
    ↓
Message Broker
```

Domain Event：

```text
OrderPaid
```

Integration Event：

```text
OrderPaidIntegrationEvent
```

原因：

> 内部 Domain Model 不应该直接暴露为外部协议。

---

# 57. Outbox

框架支持 Outbox Pattern。

```text
Transaction
 ┌──────────────────────┐
 │ Aggregate            │
 │ Outbox Record        │
 └──────────────────────┘
          │
          ▼
       Commit
          │
          ▼
   Outbox Publisher
          │
          ▼
       Message Bus
```

目标：

避免：

```text
DB Commit = Success
MQ Publish = Failure
```

造成状态不一致。

---

# 58. Saga / Process Manager

不将 Saga 强行放入 Domain Model。

建议：

```text
Application / Process Layer
```

负责跨 Aggregate / 跨 Context 长事务。

结构：

```text
ProcessManager
 ├── State
 ├── Command
 ├── Event
 ├── Transition
 └── Compensation
```

---

# 59. Aggregate 边界原则

默认原则：

> 一个事务只修改一个 Aggregate。

跨 Aggregate：

```text
Aggregate A
    ↓ Event
Application
    ↓ Command
Aggregate B
```

而不是：

```text
Aggregate A
    ↓
Directly modify
    ↓
Aggregate B
```

---

# 60. Bounded Context Integration

Context 间通信：

```text
Context A
    │
    ├── Domain Event
    │
    ▼
Integration Adapter
    │
    ▼
Context B
```

可使用：

```text
REST
gRPC
Message
Event
```

---

# 61. Anti-Corruption Layer

框架支持 ACL。

```text
External Context
       │
       ▼
Adapter
       │
       ▼
ACL
       │
       ▼
Domain Model
```

ACL 负责：

```text
External DTO
      ↓
Translator
      ↓
Domain Model
```

---

# 62. Domain Model 与 DTO 隔离

禁止：

```java
public record OrderDTO(...) implements AggregateRoot<OrderId>
```

推荐：

```text
HTTP DTO
   ↓
Command
   ↓
Aggregate
```

Query：

```text
SQL Projection
   ↓
View
   ↓
Response DTO
```

---

# 63. CQRS Read Model

Query 不一定通过 Aggregate。

例如：

```text
GET /orders/{id}
```

可以：

```text
QueryHandler
    ↓
OrderReadRepository
    ↓
SQL
    ↓
OrderView
```

无需：

```text
OrderRepository
    ↓
Order Aggregate
    ↓
DTO
```

---

# 64. Middleware

Command / Query Pipeline 支持 Middleware。

```text
Command
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
 ↓
Event Dispatch
```

例如：

```java
public interface CommandMiddleware {

    <R> R execute(
        Command<R> command,
        CommandExecution next
    );
}
```

---

# 65. 推荐 Middleware 顺序

```text
Correlation
     ↓
Validation
     ↓
Authorization
     ↓
Idempotency
     ↓
Transaction
     ↓
Command Handler
     ↓
Event Collection
     ↓
Commit
```

---

# 66. Validation

分为两类。

## Application Validation

检查：

```text
Command 是否完整
格式是否正确
权限是否满足
```

## Domain Validation

检查：

```text
业务不变量
状态转换
领域规则
```

不要将 Domain Rule 全部放到 DTO Validation。

---

# 67. Exception Model

基础异常：

```text
DddException
 ├── DomainException
 ├── ApplicationException
 ├── InfrastructureException
 ├── ValidationException
 ├── AuthorizationException
 ├── ConcurrencyException
 └── IdempotencyException
```

原则：

```text
Domain Exception
≠
HTTP Exception
```

Interface Adapter 再负责映射：

```text
DomainException
      ↓
HTTP 409 / 422
```

---

# 68. Domain Error

推荐：

```java
public interface DomainError {

    String code();

    String message();
}
```

业务错误：

```text
ORDER_ALREADY_PAID
ORDER_CANNOT_CANCEL
INSUFFICIENT_BALANCE
```

错误 Code 应稳定。

---

# 69. Clock

Domain 不应该直接：

```java
Instant.now()
```

推荐：

```java
public interface Clock {

    Instant now();
}
```

测试：

```text
FixedClock
```

生产：

```text
SystemClock
```

---

# 70. ID Generator

```java
public interface IdGenerator {

    <T> T generate();
}
```

可实现：

```text
UUID
ULID
Snowflake
Database Sequence
```

Domain 不关心具体算法。

---

# 71. Time Model

统一使用：

```text
Instant
LocalDate
LocalDateTime
ZoneId
```

业务时间必须明确语义。

例如：

```text
PaymentDate
BusinessDate
CreatedAt
OccurredAt
```

不得随意混用。

---

# 72. Framework Package Model

推荐：

```text
io.github.regalpine.ddd
│
├── core
├── domain
├── application
├── cqrs
├── event
├── port
├── transaction
├── infrastructure
└── test
```

进一步：

```text
io.github.regalpine.ddd.core.entity
io.github.regalpine.ddd.core.value
io.github.regalpine.ddd.core.aggregate
io.github.regalpine.ddd.core.event
```

---

# 73. Domain Package Example

业务项目：

```text
com.example.order
│
├── domain
│   ├── model
│   │   └── order
│   │       ├── Order
│   │       ├── OrderId
│   │       ├── OrderItem
│   │       ├── Money
│   │       └── OrderStatus
│   │
│   ├── service
│   ├── event
│   └── repository
│
├── application
│   ├── command
│   ├── query
│   └── service
│
├── infrastructure
│   ├── persistence
│   ├── messaging
│   └── external
│
└── interfaces
    ├── rest
    └── messaging
```

---

# 74. Framework 不强制 Annotation

不要求：

```java
@Entity
@Service
@Component
@Repository
```

才能成为 DDD 对象。

DDD 语义应该首先来自：

```text
Type
Interface
Package
Module
Dependency
```

Annotation 只作为增强能力。

---

# 75. Annotation 使用原则

如果提供 Annotation，应属于：

```text
ddd-metadata
```

例如：

```java
@Aggregate
@DomainEntity
@ValueObject
@DomainService
@DomainEvent
```

这些 Annotation：

- 不产生 DSL
- 不要求代码生成
- 不改变 Java 语义
- 不承担运行时业务逻辑

---

# 76. 不采用传统贫血模型

不推荐：

```java
class Order {

    private OrderStatus status;

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
```

推荐：

```java
class Order {

    public void pay(Payment payment) {
        // invariant
        // state transition
        // domain event
    }
}
```

领域行为属于 Aggregate。

---

# 77. 不追求纯粹主义

框架允许：

```text
Rich Domain Model
Anemic Model
Transaction Script
CQRS
Event-driven
```

但提供能力使团队可以逐步演进。

默认推荐：

```text
Rich Domain Model
+
CQRS
+
Hexagonal
```

---

# 78. 模块化单体

DDD Framework 首要支持：

```text
Modular Monolith
```

结构：

```text
Application
├── Customer Context
├── Order Context
├── Payment Context
└── Inventory Context
```

Context 之间通过明确接口通信。

---

# 79. 微服务

当 Bounded Context 达到独立部署条件时，可以：

```text
Context
    ↓
Service
```

DDD Framework 本身不负责服务注册、配置中心等能力。

---

# 80. Observability

Framework 提供统一扩展点：

```text
Command Started
Command Completed
Query Started
Query Completed
Domain Event Published
Repository Operation
Transaction Started
Transaction Committed
```

但不强制绑定：

```text
OpenTelemetry
Micrometer
Prometheus
```

---

# 81. Audit

框架支持 Audit Port：

```java
public interface AuditRecorder {

    void record(AuditRecord record);
}
```

Audit Record：

```text
principal
tenant
operation
resource
timestamp
result
correlationId
```

Audit 不应该侵入 Domain Model。

---

# 82. Distributed Trace

统一：

```text
CorrelationId
CausationId
TraceId
```

关系：

```text
Request
 ↓
Command
 ↓
Domain Event
 ↓
Integration Event
 ↓
Command
```

形成因果链。

---

# 83. Domain Event Consistency

Event 必须满足：

```text
Event
    belongs to
Aggregate
```

并且：

```text
Aggregate Version
        ≥
Event Sequence
```

同一 Aggregate 的事件必须具有可排序语义。

---

# 84. Repository Consistency

Repository 必须：

1. 按 Aggregate Root 操作
2. 保证 Aggregate 完整加载
3. 支持版本检查
4. 不暴露 Persistence Model
5. 不泄漏 ORM API

---

# 85. Query Consistency

Query：

```text
Read Only
```

Query Handler：

```text
不得修改 Aggregate
不得产生 Domain Event
不得触发业务状态变化
```

如果确实需要副作用，应转换为 Command。

---

# 86. Command Consistency

Command：

```text
Intent
```

而不是：

```text
Database Update
```

推荐：

```text
PayOrder
CancelOrder
ApproveOrder
```

而不是：

```text
UpdateOrderStatus
```

---

# 87. Ontology Relationship Model

核心关系：

| Subject | Relationship | Object |
|---|---|---|
| DDDSystem | contains | BoundedContext |
| BoundedContext | contains | Aggregate |
| Aggregate | hasRoot | AggregateRoot |
| Aggregate | contains | Entity |
| Aggregate | contains | ValueObject |
| Aggregate | enforces | Invariant |
| Aggregate | emits | DomainEvent |
| DomainService | operatesOn | DomainObject |
| Repository | persists | Aggregate |
| Command | targets | UseCase |
| CommandHandler | handles | Command |
| QueryHandler | handles | Query |
| ApplicationService | invokes | Domain |
| Port | abstracts | ExternalCapability |
| Adapter | implements | Port |
| IntegrationEvent | derivesFrom | DomainEvent |

---

# 88. Relationship Cardinality

## BoundedContext

```text
DDDSystem
1 ───── N BoundedContext
```

## Aggregate

```text
BoundedContext
1 ───── N Aggregate
```

## Aggregate Root

```text
Aggregate
1 ───── 1 AggregateRoot
```

## Entity

```text
Aggregate
1 ───── N Entity
```

## Domain Event

```text
Aggregate
1 ───── N DomainEvent
```

---

# 89. Relationship Invariants

必须满足：

```text
AggregateRoot ⊂ Aggregate
```

```text
Entity ⊂ Aggregate
```

```text
DomainEvent → Aggregate
```

```text
Repository → AggregateRoot
```

```text
InfrastructureAdapter → Port
```

并禁止：

```text
Domain → Adapter
```

---

# 90. Dependency Ontology

定义：

```text
dependsOn
implements
invokes
persists
publishes
subscribes
contains
owns
references
```

其中：

```text
dependsOn
```

是架构级关系。

最重要的约束：

```text
Domain
    X
    │
    └── dependsOn → Infrastructure
```

必须不存在。

---

# 91. Domain Reference Rule

Aggregate 可以引用：

```text
自身 Entity
自身 Value Object
外部 Aggregate Identity
```

不建议直接引用：

```text
External Aggregate Object
```

推荐：

```java
CustomerId
```

而不是：

```java
Customer
```

---

# 92. Aggregate Reference Ontology

允许：

```text
Order
 └── customerId
```

而不是：

```text
Order
 └── Customer Aggregate
```

这是 Aggregate 解耦的重要机制。

---

# 93. Bounded Context Relationship

Context 关系类型：

```text
Partnership
CustomerSupplier
Conformist
AntiCorruptionLayer
OpenHostService
PublishedLanguage
SeparateWays
```

框架不要求全部实现为代码类型。

它们属于 Architecture Metadata。

---

# 94. Framework Runtime Model

运行时：

```text
Input Adapter
      ↓
Input Port
      ↓
CommandBus / QueryBus
      ↓
Middleware
      ↓
Handler
      ↓
Domain
      ↓
Output Port
      ↓
Output Adapter
```

---

# 95. Command Pipeline

```text
Command
  ↓
CommandBus
  ↓
Correlation Middleware
  ↓
Validation Middleware
  ↓
Authorization Middleware
  ↓
Idempotency Middleware
  ↓
Transaction Middleware
  ↓
CommandHandler
  ↓
Domain
  ↓
Repository
  ↓
Event Collector
  ↓
Commit
  ↓
Event Publisher
```

---

# 96. Query Pipeline

```text
Query
 ↓
QueryBus
 ↓
Validation
 ↓
Authorization
 ↓
QueryHandler
 ↓
Read Repository
 ↓
Projection
 ↓
Query Result
```

Query 不需要强制开启 Domain Transaction。

---

# 97. Transaction Boundary

Transaction 默认位于 Application Layer。

```text
CommandHandler
      │
      ▼
Transaction
      │
      ▼
Aggregate
```

而不是：

```text
Entity
 ↓
Transaction
```

---

# 98. Event Publication Strategy

支持三种：

### Synchronous

```text
Commit
 ↓
Publish
```

### Transactional Outbox

```text
Commit Aggregate + Outbox
 ↓
Async Publisher
```

### Event Store

```text
Append Event
 ↓
Projection
```

默认企业模式：

> Transactional Outbox。

---

# 99. Persistence Model

允许：

```text
Domain Model
        │
        ▼
Persistence Mapper
        │
        ▼
Persistence Model
```

即：

```text
Domain Order
      ↕
OrderRecord
```

不要求 Domain Entity 直接成为 ORM Entity。

---

# 100. ORM Adapter

JPA 示例：

```text
JpaOrderRepository
        │
        ▼
OrderJpaEntity
        │
        ▼
OrderMapper
        │
        ▼
Domain Order
```

JPA 不应该进入 Domain。

---

# 101. Database Transaction

Infrastructure Adapter 负责：

```text
BEGIN
 ↓
Repository
 ↓
Repository
 ↓
Outbox
 ↓
COMMIT
```

Application 只声明：

```text
Transaction Boundary
```

---

# 102. Testing Architecture

测试分层：

```text
Domain Unit Test
        ↓
Application Test
        ↓
Adapter Integration Test
        ↓
Architecture Test
        ↓
End-to-End Test
```

---

# 103. Domain Test

Domain Test 不需要：

```text
Spring
Database
Redis
Kafka
HTTP
```

例如：

```java
Order order = Order.create(...);

order.pay(...);

assertThat(order.status())
    .isEqualTo(PAID);
```

---

# 104. Architecture Test

框架应提供架构约束测试能力：

```text
Domain
cannot depend on
Infrastructure
```

```text
Domain
cannot depend on
Spring
```

```text
Query
cannot depend on
Command
```

---

# 105. 推荐技术栈

Framework Core：

```text
Java 17
Maven
JUnit 5
AssertJ
```

可选：

```text
Spring Boot
JPA
MyBatis
JDBC
Redis
Kafka
RabbitMQ
OpenTelemetry
```

全部作为 Adapter。

---

# 106. Java Module Dependency

核心依赖：

```text
ddd-core
   ↑
ddd-domain
   ↑
ddd-application
   ↑
ddd-cqrs
```

Infrastructure：

```text
ddd-infrastructure
       ↓
ddd-port
ddd-event
ddd-transaction
```

Spring：

```text
ddd-spring-boot
       ↓
ddd-application
ddd-cqrs
ddd-event
ddd-transaction
```

---

# 107. 禁止依赖

以下依赖不得出现在：

```text
ddd-core
ddd-domain
```

包括：

```text
spring-context
spring-beans
spring-data
hibernate
jakarta.persistence
kafka-clients
redis-client
jdbc-driver
```

---

# 108. API Stability

框架 API 分三级：

```text
CORE
STABLE
EXPERIMENTAL
```

## CORE

长期兼容：

```text
Entity
ValueObject
AggregateRoot
DomainEvent
Repository
Command
Query
```

## STABLE

经过生产验证：

```text
CommandBus
QueryBus
UnitOfWork
Outbox
```

## EXPERIMENTAL

例如：

```text
EventSourcing
Saga
ProcessManager
DistributedLock
```

---

# 109. 版本策略

建议：

```text
0.x
Architecture Development

1.0
Core API Stable

1.x
Backward Compatible

2.0
Major Architectural Change
```

不要通过大量版本不断扩展概念。

原则：

> Framework 应优先收敛核心抽象，而不是不断增加新的 DDD 类型。

---

# 110. 第一阶段必须收敛的核心抽象

最终核心 API 控制在以下集合：

```text
Entity
ValueObject
AggregateRoot
DomainEvent
DomainService
Repository

Command
CommandHandler
Query
QueryHandler

CommandBus
QueryBus

UnitOfWork
TransactionBoundary

EventPublisher
EventSubscriber

Clock
IdGenerator

Specification
DomainException
ExecutionContext
```

这是框架的核心骨架。

---

# 111. 不进入 Core 的能力

以下能力不应该进入 Core：

```text
Kafka
Redis
JPA
Spring
HTTP
gRPC
GraphQL
Prometheus
OpenTelemetry
IAM
Workflow
Scheduler
```

这些都属于 Adapter / Extension。

---

# 112. Framework Extension Model

最终：

```text
ddd-core
      │
      ├── ddd-domain
      ├── ddd-application
      ├── ddd-cqrs
      ├── ddd-event
      └── ddd-transaction
               │
               ▼
         Adapter Ecosystem
               │
     ┌─────────┼──────────┐
     ▼         ▼          ▼
   Spring     JDBC       Kafka
```

---

# 113. 最小可运行模型

一个 Order 示例至少包含：

```text
OrderId
Order
OrderItem
Money
OrderStatus

OrderCreated
OrderPaid

OrderRepository

CreateOrderCommand
CreateOrderCommandHandler

PayOrderCommand
PayOrderCommandHandler

GetOrderQuery
GetOrderQueryHandler
```

即可形成完整 DDD + CQRS 应用。

---

# 114. 最终开发体验

开发人员应该可以直接：

```java
public final class Order
        implements AggregateRoot<OrderId> {

    private final OrderId id;

    private OrderStatus status;

    public void pay(Payment payment) {

        if (status != OrderStatus.CREATED) {
            throw new OrderAlreadyPaidException();
        }

        this.status = OrderStatus.PAID;

        // register domain event
    }
}
```

然后：

```java
public final class PayOrderHandler
        implements CommandHandler<
            PayOrderCommand,
            Void
        > {

    private final OrderRepository repository;

    @Override
    public Void handle(
        PayOrderCommand command
    ) {

        Order order =
            repository.findById(command.orderId())
                .orElseThrow();

        order.pay(command.payment());

        repository.save(order);

        return null;
    }
}
```

不需要：

```text
编写 DSL
编译 DSL
生成代码
学习新语言
```

---

# 115. 本框架与传统 DDD 框架区别

传统：

```text
DDD
 ↓
团队自行实现
 ↓
每个项目重复实现
```

本框架：

```text
DDD
 ↓
统一 Core
 ↓
统一 Application Runtime
 ↓
统一 CQRS
 ↓
统一 Event
 ↓
统一 Transaction
 ↓
统一 Adapter SPI
```

---

# 116. 与 Spring 的关系

不是：

```text
DDD Framework = Spring Extension
```

而是：

```text
DDD Framework
       │
       ├── Spring Adapter
       ├── Quarkus Adapter
       ├── Jakarta Adapter
       └── Plain Java
```

这样可以保持框架独立性。

---

# 117. 与 ORM 的关系

不是：

```text
DDD Entity = JPA Entity
```

而是：

```text
Domain Entity
      │
      ▼
Persistence Mapper
      │
      ▼
JPA Entity
```

---

# 118. 与 CQRS 的关系

CQRS 是 Application Architecture。

DDD Framework 提供：

```text
Command
CommandHandler
CommandBus

Query
QueryHandler
QueryBus
```

但不规定：

```text
Database topology
```

因此可以：

```text
Same DB
```

也可以：

```text
Write DB + Read DB
```

也可以：

```text
Event Store + Projection
```

---

# 119. 与 Event Sourcing 的关系

Event Sourcing：

```text
Optional Extension
```

而不是：

```text
DDD Requirement
```

这是重要设计决策。

---

# 120. 与微服务的关系

DDD Framework：

```text
Supports Microservices
```

但：

```text
DDD ≠ Microservices
```

推荐先：

```text
Bounded Context
+
Modular Monolith
```

必要时再：

```text
Context → Microservice
```

---

# 121. Framework Architecture Summary

```text
                       ┌───────────────────────┐
                       │     Interface        │
                       │ REST / RPC / MQ / UI │
                       └──────────┬────────────┘
                                  │
                                  ▼
                       ┌───────────────────────┐
                       │     Application       │
                       │ Command / Query / UC  │
                       └──────────┬────────────┘
                                  │
                  ┌───────────────┴───────────────┐
                  ▼                               ▼
          ┌───────────────┐              ┌───────────────┐
          │ Command Side  │              │  Query Side   │
          └───────┬───────┘              └───────┬───────┘
                  │                              │
                  └──────────────┬───────────────┘
                                 ▼
                       ┌───────────────────────┐
                       │        Domain         │
                       │ Aggregate / Entity    │
                       │ VO / Service / Event  │
                       └──────────┬────────────┘
                                  │
                                  ▼
                            ┌───────────┐
                            │   Ports   │
                            └─────┬─────┘
                                  │
               ┌──────────────────┼──────────────────┐
               ▼                  ▼                  ▼
          Persistence         Messaging           External
          Adapter             Adapter             Adapter
               │                  │                  │
               ▼                  ▼                  ▼
              DB                  MQ                API
```

---

# 122. 核心设计结论

本框架最终形成以下关系：

```text
DDD
│
├── Strategic Modeling
│     └── Bounded Context
│
├── Tactical Modeling
│     ├── Aggregate
│     ├── Entity
│     ├── Value Object
│     ├── Domain Service
│     ├── Domain Event
│     └── Repository
│
├── Application Architecture
│     ├── Command
│     ├── Query
│     ├── Use Case
│     └── Transaction
│
├── Hexagonal Architecture
│     ├── Input Port
│     ├── Output Port
│     └── Adapter
│
└── CQRS
      ├── Command Side
      └── Query Side
```

---

# 123. v0.1 结论

`io.github.regalpine.ddd` 第一阶段不追求成为一个庞大的 Enterprise Framework。

核心目标只有一个：

> **建立稳定、技术无关、可组合、可测试的 DDD Runtime Foundation。**

核心架构：

```text
DDD
+
Four-Layer
+
Hexagonal
+
CQRS
+
Domain Events
+
Transaction Boundary
+
Ports & Adapters
```

核心原则：

```text
Domain First
Technology Independent
Aggregate Boundary
Explicit Use Case
Separated Read/Write
Ports over Implementations
Adapters over Framework Coupling
```

---

# 124. 下一阶段建议

下一阶段不继续堆叠新的 DDD 概念，而应该进入**框架正式化阶段**，依次完成：

```text
Phase I
DDD Ontology Formal Model
        ↓
Phase II
Core Java API Specification
        ↓
Phase III
Maven Multi-Module Specification
        ↓
Phase IV
Command/Query Runtime Specification
        ↓
Phase V
Domain Event & Outbox Specification
        ↓
Phase VI
Transaction & UnitOfWork Specification
        ↓
Phase VII
Persistence Port Specification
        ↓
Phase VIII
Spring Boot Adapter Specification
        ↓
Phase IX
Test Framework Specification
        ↓
Phase X
Reference Implementation
        ↓
DDD Framework 1.0
```

其中 **Phase I 的本体论只作为框架语义基础，不产生 DSL**。

最终交付物应该是一个真实的：

```text
io.github.regalpine.ddd
```

Java 17 Maven Framework，而不是另一套需要学习的领域建模语言。