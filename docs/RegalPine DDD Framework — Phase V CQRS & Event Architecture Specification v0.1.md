# RegalPine DDD Framework
## Phase V — CQRS & Event Architecture Specification v0.1

**GroupId:** `io.github.regalpine.ddd`  
**Java:** 17+  
**Build:** Maven  
**Architecture:** DDD + Four-Layer + Hexagonal + CQRS + Event-Driven Architecture  
**Status:** Architecture Baseline  
**Primary Modules:** `ddd-cqrs` / `ddd-event`

---

# 1. 本阶段目标

Phase IV 已确定 Application Layer：

```text
Command
Query
Use Case
Handler
Transaction
Authorization
Idempotency
```

Phase V 在此基础上建立：

```text
CommandBus
QueryBus
Middleware
Pipeline
Handler Registry
Domain Event
Integration Event
Event Handler
Event Bus
Outbox
Inbox
Projection
Read Model
Event Ordering
Event Idempotency
Event Versioning
Schema Evolution
```

目标是形成：

```text
DDD
+
CQRS
+
Event-Driven
```

的完整执行主干。

---

# 2. 核心设计原则

本阶段冻结以下原则：

```text
CommandBus ≠ Domain
QueryBus ≠ Domain
EventBus ≠ Domain
Outbox ≠ Domain
Inbox ≠ Domain
Projection ≠ Domain
ReadModel ≠ Domain
```

Domain 只负责：

```text
Business Behavior
Business State
Business Rules
Domain Events
```

---

# 3. CQRS 总体架构

```text
                         ┌──────────────┐
                         │   Client     │
                         └──────┬───────┘
                                │
                  ┌─────────────┴─────────────┐
                  │                           │
                  ▼                           ▼
             Command                      Query
                  │                           │
                  ▼                           ▼
             CommandBus                   QueryBus
                  │                           │
                  ▼                           ▼
             Middleware                 Middleware
                  │                           │
                  ▼                           ▼
          CommandHandler                QueryHandler
                  │                           │
                  ▼                           ▼
              Domain                    Read Model
                  │                           ▲
                  ▼                           │
           Domain Event                      │
                  │                           │
                  ▼                           │
               Outbox                        │
                  │                           │
                  ▼                           │
             Event Bus ───────────────► Projection
```

---

# 4. CQRS 两侧

## Command Side

```text
Command
   ↓
CommandBus
   ↓
CommandMiddleware
   ↓
CommandHandler
   ↓
Domain
   ↓
AggregateRepository
```

## Query Side

```text
Query
   ↓
QueryBus
   ↓
QueryMiddleware
   ↓
QueryHandler
   ↓
ReadModel
```

两者共享：

```text
Application Context
Authorization
Observability
Error Model
```

但不共享 Domain State。

---

# 5. CommandBus

正式职责：

> 将 Command 路由到对应 CommandHandler。

接口：

```java
public interface CommandBus {

    <R> R dispatch(Command<R> command);
}
```

---

# 6. CommandBus 不负责业务

CommandBus 不允许：

```text
加载 Aggregate
修改 Aggregate
执行 Domain Rule
访问数据库
发布 Domain Event
```

它只负责：

```text
Routing
Pipeline
Middleware
Handler Invocation
```

---

# 7. CommandHandler Registry

CommandBus 必须能够根据 Command 类型找到 Handler。

逻辑模型：

```text
Command Type
      │
      ▼
Handler Registry
      │
      ▼
CommandHandler
```

例如：

```text
PayOrderCommand
       ↓
PayOrderHandler
```

---

# 8. Handler 唯一性

默认规则：

```text
One Command Type
        ↓
One Active Handler
```

如果发现：

```text
PayOrderCommand
   ↓
Handler A
Handler B
```

启动阶段必须失败。

不能运行时随机选择。

---

# 9. QueryBus

正式接口：

```java
public interface QueryBus {

    <R> R dispatch(Query<R> query);
}
```

职责：

```text
Routing
Middleware
QueryHandler Invocation
```

---

# 10. Query Handler Registry

```text
Query Type
    │
    ▼
Handler Registry
    │
    ▼
QueryHandler
```

同样要求：

```text
One Query Type
        ↓
One Active Handler
```

---

# 11. Command Pipeline

CommandBus 内部：

```text
Command
  │
  ▼
Middleware 1
  │
  ▼
Middleware 2
  │
  ▼
Middleware N
  │
  ▼
Handler
```

---

# 12. Middleware

Middleware 用于处理横切关注点：

```text
Authorization
Logging
Tracing
Metrics
Idempotency
Transaction
Retry
Timeout
Validation
```

---

# 13. Middleware Contract

推荐：

```java
public interface CommandMiddleware {

    <R> R execute(
            Command<?> command,
            CommandExecutionChain chain);
}
```

Chain：

```java
public interface CommandExecutionChain {

    <R> R proceed(Command<?> command);
}
```

---

# 14. Middleware 顺序

默认推荐：

```text
Inbound
  ↓
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

但实际顺序必须允许应用根据语义调整。

---

# 15. Transaction Middleware

推荐：

```text
CommandBus
   ↓
TransactionMiddleware
   ↓
Handler
```

而不是：

```text
CommandBus
   ↓
Handler
   ↓
transaction.begin()
```

事务由 Application Infrastructure 提供。

---

# 16. Authorization Middleware

推荐：

```text
Command
   ↓
AuthorizationMiddleware
   ↓
Handler
```

授权失败：

```text
AuthorizationDenied
```

不进入 Domain。

---

# 17. Validation Middleware

Validation 分为：

```text
Structural Validation
Business Validation
```

Middleware 只负责：

```text
Structural Validation
```

例如：

```text
null
format
required field
length
```

Business Validation 仍然属于 Domain。

---

# 18. Idempotency Middleware

典型：

```text
Command
   ↓
IdempotencyMiddleware
   ↓
check commandId
   │
   ├── exists → return previous result
   │
   └── absent → continue
```

---

# 19. Idempotency Result

如果 Command 已执行：

```text
commandId = C001
result = success
```

再次收到：

```text
C001
```

返回相同业务结果。

不能重新执行 Aggregate Behavior。

---

# 20. Query Middleware

Query 同样可以使用：

```text
Tracing
Validation
Authorization
Metrics
Caching
```

例如：

```text
Query
 ↓
Tracing
 ↓
Authorization
 ↓
Cache
 ↓
QueryHandler
```

---

# 21. Query Middleware 不默认开启 Transaction

Query 默认：

```text
No Transaction
```

除非：

```text
数据库一致性读取
特殊隔离级别
复杂查询生命周期
```

确实需要。

---

# 22. Domain Event

Domain Event 是：

> Aggregate 已经发生的业务事实。

例如：

```text
OrderCreated
OrderPaid
OrderCancelled
CustomerRegistered
```

Domain Event 由 Aggregate 产生。

---

# 23. Event Lifecycle

```text
Aggregate Behavior
       ↓
State Change
       ↓
Domain Event
       ↓
Aggregate Pending Events
       ↓
Repository Save
       ↓
Transaction Commit
```

此时：

```text
Event
```

已经成为业务事实。

---

# 24. Event 与 Publication

必须区分：

```text
Event Recorded
```

和：

```text
Event Published
```

两者不是同一个操作。

```text
Recorded
   ↓
Persisted
   ↓
Published
```

---

# 25. Event Bus

Event Bus 用于：

> 向 Event Handler 分发事件。

接口：

```java
public interface EventBus {

    void publish(IntegrationEvent event);
}
```

但 Domain Event 与 Integration Event 不应该强制使用同一个 Bus。

---

# 26. DomainEventDispatcher

推荐内部概念：

```java
public interface DomainEventDispatcher {

    void dispatch(
            DomainEvent event);
}
```

主要用于：

```text
同一 Application / Bounded Context
```

内的事件处理。

---

# 27. IntegrationEvent

Integration Event 用于：

```text
Bounded Context
Service
System
```

之间通信。

例如：

```text
OrderPaidIntegrationEvent
```

可以发送到：

```text
Kafka
RabbitMQ
Pulsar
HTTP
```

---

# 28. Domain Event → Integration Event

推荐：

```text
Domain Event
      │
      ▼
Event Mapping
      │
      ▼
Integration Event
      │
      ▼
Outbox
```

而不是：

```text
Aggregate
   ↓
Kafka
```

---

# 29. Outbox Pattern

核心目的：

> 保证业务状态变更与事件记录具有同一事务语义。

错误：

```text
DB Commit
   ↓
Kafka Publish
```

可能：

```text
DB Success
Kafka Failure
```

造成事件丢失。

---

# 30. Outbox 正确模型

```text
Transaction
│
├── Aggregate State
│
└── Outbox Event
│
└── COMMIT
       │
       ▼
Outbox Dispatcher
       │
       ▼
Message Broker
```

---

# 31. Outbox Record

推荐逻辑字段：

```text
outboxId
eventId
eventType
eventVersion
aggregateType
aggregateId
payload
occurredAt
createdAt
status
attempts
publishedAt
```

---

# 32. Outbox 不属于 Domain

Outbox 是：

```text
Infrastructure / Event Infrastructure
```

Domain 不知道：

```text
Outbox
Kafka
Message Broker
Serialization
Retry Queue
```

---

# 33. Outbox Module

建议：

```text
ddd-event
```

负责：

```text
Event abstraction
Event mapping
Event dispatch
Outbox contract
Inbox contract
Projection contract
```

具体数据库实现：

```text
ddd-infrastructure
```

---

# 34. Outbox Port

推荐：

```java
public interface OutboxStore {

    void append(
            OutboxRecord record);

    List<OutboxRecord> loadPending(
            int limit);

    void markPublished(
            EventId eventId);
}
```

---

# 35. Outbox Dispatcher

```text
Outbox
  │
  ▼
Dispatcher
  │
  ├── load
  │
  ├── publish
  │
  └── markPublished
```

---

# 36. Outbox Delivery Semantics

默认：

```text
At-Least-Once Delivery
```

而不是：

```text
Exactly-Once
```

原因：

```text
Network
Process Crash
Broker Retry
Database Commit
```

使端到端 Exactly-Once 非常昂贵且容易产生错误假设。

---

# 37. Event Consumer

消费者必须：

```text
Idempotent
```

例如：

```text
Event E100
Event E100
```

第二次必须不会产生第二次业务效果。

---

# 38. Inbox Pattern

Inbox 用于：

> 防止重复消费事件造成重复业务操作。

```text
Incoming Event
      ↓
Inbox
      │
      ├── processed → ignore
      │
      └── new
           ↓
        Handler
```

---

# 39. Inbox Record

建议：

```text
eventId
consumerId
eventType
receivedAt
processedAt
status
attempts
error
```

唯一键：

```text
consumerId + eventId
```

---

# 40. Consumer Idempotency

不能只依赖：

```text
eventId
```

因为：

```text
Consumer A
Consumer B
```

都可以处理同一个 Event。

因此唯一性通常：

```text
Consumer Identity
+
Event Identity
```

---

# 41. Event Ordering

事件顺序是重要问题。

同一 Aggregate：

```text
OrderCreated
OrderPaid
OrderShipped
```

必须尽量保持：

```text
Created
  ↓
Paid
  ↓
Shipped
```

---

# 42. Aggregate Event Sequence

推荐增加：

```text
aggregateId
aggregateVersion
sequence
```

例如：

```text
Order-100
version=10
sequence=10
OrderPaid
```

消费者可以检查：

```text
expected = 10
received = 10
```

---

# 43. Event Ordering Boundary

框架保证：

> 同一 Aggregate 的事件可以建立确定性顺序。

框架不保证：

> 不同 Aggregate 的全局顺序。

例如：

```text
Order-1 Event A
Order-2 Event B
```

没有必要强制：

```text
A < B
```

---

# 44. Event Version

Event Schema 必须版本化。

例如：

```text
OrderPaid
v1
```

后续：

```text
OrderPaid
v2
```

推荐事件类型标识：

```text
OrderPaid.v1
OrderPaid.v2
```

或者：

```text
eventType = OrderPaid
eventVersion = 2
```

---

# 45. Event Schema Evolution

允许：

```text
v1 → v2
```

但必须保证：

```text
Old Event
```

仍然能够被历史消费者处理，或者存在明确 Migration / Upcasting 策略。

---

# 46. Breaking Event Change

以下属于 Breaking Change：

```text
删除字段
修改字段语义
改变字段类型
改变 Aggregate Identity
改变事件业务含义
```

不能仅修改 Java Class 就认为兼容。

---

# 47. Event Upcaster

可以提供：

```java
public interface EventUpcaster {

    EventEnvelope upcast(
            EventEnvelope event);
}
```

例如：

```text
OrderPaid v1
      ↓
Upcaster
      ↓
OrderPaid v2
```

---

# 48. Event Envelope

推荐：

```java
public record EventEnvelope(
        EventId eventId,
        String eventType,
        int eventVersion,
        String aggregateType,
        String aggregateId,
        long sequence,
        Instant occurredAt,
        byte[] payload
) {
}
```

注意：

`EventEnvelope` 属于 Event Infrastructure，不是 Domain Entity。

---

# 49. Event Metadata

Envelope 可以包含：

```text
correlationId
causationId
tenantId
traceId
producer
```

这些属于：

```text
Event Infrastructure
```

而不是业务 Event 本身。

---

# 50. Correlation ID

用于：

> 标识同一个业务流程。

例如：

```text
CreateOrder
   correlationId = C100
      ↓
OrderCreated
      ↓
PaymentRequested
      ↓
PaymentCompleted
```

---

# 51. Causation ID

用于：

> 表示一个事件/消息由哪个上游消息导致。

例如：

```text
Command C1
   ↓
Event E1
   ↓
Command C2
   ↓
Event E2
```

关系：

```text
E1.causationId = C1
C2.causationId = E1
E2.causationId = C2
```

---

# 52. Event Handler

接口：

```java
public interface EventHandler<E> {

    void handle(E event);
}
```

Event Handler 应：

```text
短小
幂等
可重试
可观测
```

---

# 53. Event Handler 不修改其他 Aggregate？

不是绝对禁止。

可以：

```text
Event
 ↓
Handler
 ↓
Command
 ↓
Another Aggregate
```

但推荐：

```text
Event
 ↓
Application Process
 ↓
Command
 ↓
Aggregate
```

而不是直接：

```text
Event Handler
 ↓
aggregate.setX()
```

---

# 54. Event-Driven Aggregate Interaction

推荐：

```text
OrderPaid
    ↓
PaymentProcess
    ↓
CapturePaymentCommand
    ↓
Payment Aggregate
```

而不是：

```text
OrderPaidHandler
    ↓
PaymentAggregate.internalMutation()
```

这样保持：

```text
Command
=
Intent
```

---

# 55. Projection

Projection 将事件转换为 Read Model。

```text
Event
  ↓
Projection
  ↓
Read Model
```

例如：

```text
OrderPaid
    ↓
OrderSummaryProjection
    ↓
order_summary
```

---

# 56. Projection Contract

```java
public interface Projection<E> {

    void project(E event);
}
```

Projection 不属于 Domain。

---

# 57. Projection Idempotency

Projection 必须支持重复事件：

```text
E100
E100
```

最终：

```text
ReadModel(E100)
```

只应用一次。

---

# 58. Projection Offset

推荐维护：

```text
projectionId
aggregateId
lastSequence
updatedAt
```

例如：

```text
OrderSummaryProjection
Order-100
sequence=12
```

收到：

```text
sequence=11
```

应忽略。

收到：

```text
sequence=13
```

则：

```text
apply
```

如果收到：

```text
sequence=15
```

但：

```text
expected=14
```

则可能存在：

```text
Event Gap
```

必须进入重试/等待/恢复流程。

---

# 59. Read Model Rebuild

Projection 必须允许：

```text
Delete Read Model
        ↓
Replay Events
        ↓
Rebuild
```

这是 Event-Driven CQRS 的重要能力。

---

# 60. Projection 不应反向修改 Domain

禁止：

```text
Projection
   ↓
Aggregate
```

推荐：

```text
Domain Event
   ├── Projection
   └── Integration
```

---

# 61. Event Bus 与 Message Broker

必须区分：

```text
EventBus
```

和：

```text
Kafka/RabbitMQ/Pulsar
```

EventBus 是框架抽象。

Broker 是 Infrastructure 技术。

---

# 62. Broker Adapter

例如：

```text
ddd-event
    │
    ▼
EventPublisher
    ▲
    │
KafkaEventPublisher
```

Infrastructure 实现：

```text
Kafka
RabbitMQ
Pulsar
JMS
HTTP
```

---

# 63. Event Retry

事件处理失败：

```text
Event
 ↓
Handler
 ↓
Failure
```

可以：

```text
Retry
 ↓
Retry
 ↓
Retry
 ↓
Dead Letter
```

但 Retry 策略不属于 Domain。

---

# 64. Dead Letter

Dead Letter 用于：

```text
Poison Message
Permanent Failure
Schema Failure
Business Retry Exhausted
```

应保留：

```text
eventId
consumerId
failureReason
attempts
firstFailedAt
lastFailedAt
payload/reference
```

---

# 65. Event Error Classification

分为：

```text
Transient
Permanent
Business
Infrastructure
```

例如：

```text
DB Timeout
    → Transient

Invalid Schema
    → Permanent

Business Rejected
    → Business

Broker Unavailable
    → Infrastructure/Transient
```

---

# 66. Event Retry 原则

不是所有错误都应该无限 Retry。

```text
Transient
    → Retry

Permanent
    → DLQ

Business Rejection
    → Business handling
```

---

# 67. Event Ordering 与 Retry

如果：

```text
E10
E11
E12
```

E10 失败时：

```text
E11
E12
```

是否继续取决于：

```text
Ordering Requirement
```

同一 Aggregate 强顺序消费者通常：

```text
Block
```

直到：

```text
E10
```

恢复。

---

# 68. Event Delivery Semantics

框架默认：

```text
Producer
    At-Least-Once

Consumer
    Idempotent

Projection
    Idempotent

Ordering
    Per Aggregate

Global Ordering
    Not Guaranteed
```

---

# 69. Command Delivery Semantics

Command 默认：

```text
At-Least-Once Invocation
```

因此：

```text
Command Idempotency
```

是 Application 层重要能力。

---

# 70. Exactly-Once

框架不声称：

```text
Exactly Once
```

而是：

```text
At-Least-Once
+
Idempotent Processing
+
Transactional Boundaries
```

实现业务上的：

```text
Effectively Once
```

---

# 71. Event Storage

框架不强制 Event Store。

支持：

```text
Relational DB
Document DB
Event Store
Message Broker
Object Storage
```

但是：

```text
Event Store
```

与：

```text
Outbox
```

不是同一概念。

---

# 72. Outbox vs Event Store

### Outbox

保存：

```text
待发布消息
```

### Event Store

保存：

```text
Aggregate Event History
```

因此：

```text
Outbox ≠ Event Sourcing
```

---

# 73. Event Sourcing

本框架：

```text
NOT DEFAULT
```

DDD + CQRS：

```text
does not imply
Event Sourcing
```

默认仍采用：

```text
State Persistence
+
Domain Events
+
Outbox
```

只有明确需要：

```text
Complete Event History
Temporal Reconstruction
Audit-grade Event Stream
```

才启用 Event Sourcing。

---

# 74. Event Sourcing 独立能力

未来可设计：

```text
ddd-event-sourcing
```

但当前：

```text
不加入默认依赖
```

避免核心框架过度复杂。

---

# 75. CQRS Module Structure

建议：

```text
ddd-cqrs
└── io.github.regalpine.ddd.cqrs
    ├── command
    │   ├── CommandBus
    │   ├── CommandHandlerRegistry
    │   └── CommandMiddleware
    │
    ├── query
    │   ├── QueryBus
    │   ├── QueryHandlerRegistry
    │   └── QueryMiddleware
    │
    ├── pipeline
    └── error
```

---

# 76. Event Module Structure

```text
ddd-event
└── io.github.regalpine.ddd.event
    ├── domain
    ├── integration
    ├── dispatch
    ├── outbox
    ├── inbox
    ├── projection
    ├── envelope
    ├── retry
    └── error
```

---

# 77. CQRS Module Dependency

```text
ddd-cqrs
    ↓
ddd-application
    ↓
ddd-domain
    ↓
ddd-core
```

CQRS 不应：

```text
ddd-domain → ddd-cqrs
```

---

# 78. Event Module Dependency

建议：

```text
ddd-event
    ↓
ddd-domain
    ↓
ddd-core
```

Event Module 可以理解 Domain Event。

但：

```text
Domain
    X
ddd-event
```

Domain 不依赖 Event Infrastructure。

---

# 79. Infrastructure Dependency

```text
ddd-infrastructure
    ├── ddd-domain
    ├── ddd-application
    ├── ddd-cqrs
    └── ddd-event
```

具体依赖根据 Adapter 类型拆分。

---

# 80. 完整依赖图

```text
                         ddd-core
                            ▲
                            │
                       ddd-domain
                            ▲
              ┌─────────────┼─────────────┐
              │             │             │
              │             │             │
       ddd-application   ddd-event     ddd-port
              ▲             ▲             ▲
              │             │             │
           ddd-cqrs         │             │
              │             │             │
              └─────────────┼─────────────┘
                            │
                     ddd-infrastructure
                            │
                            ▼
                     ddd-spring-boot
```

---

# 81. Command 完整生命周期

```text
Client
  ↓
Command
  ↓
CommandBus
  ↓
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
CommandHandler
  ↓
Repository
  ↓
Aggregate
  ↓
Domain Behavior
  ↓
Domain Event
  ↓
Repository
  ↓
Outbox
  ↓
COMMIT
  ↓
Outbox Dispatcher
```

---

# 82. Query 完整生命周期

```text
Client
  ↓
Query
  ↓
QueryBus
  ↓
Tracing
  ↓
Validation
  ↓
Authorization
  ↓
Cache
  ↓
QueryHandler
  ↓
QueryRepository
  ↓
ReadModel
  ↓
Result
```

---

# 83. Event 完整生命周期

```text
Aggregate
  ↓
Domain Event
  ↓
Transaction
  ↓
Outbox
  ↓
Commit
  ↓
Dispatcher
  ↓
Broker
  ↓
Consumer
  ↓
Inbox
  ↓
Event Handler
  ├── Projection
  ├── Integration
  └── Command
```

---

# 84. Event → Command

允许：

```text
Event
  ↓
Application Process
  ↓
Command
```

这是跨 Aggregate 协作的重要方式。

例如：

```text
OrderPaid
   ↓
FulfillmentProcess
   ↓
CreateShipmentCommand
   ↓
Shipment Aggregate
```

---

# 85. Event → Aggregate Direct Call

禁止：

```java
public void handle(OrderPaid event) {

    shipment.changeState(...);
}
```

推荐：

```text
OrderPaid
    ↓
Process
    ↓
CreateShipmentCommand
    ↓
ShipmentHandler
    ↓
Shipment Aggregate
```

保持 Command Side 统一。

---

# 86. Saga / Process Manager

当流程跨多个事务：

```text
Order
Payment
Inventory
Shipment
```

不应该创建：

```text
Mega Transaction
```

推荐：

```text
Saga / Process Manager
```

模型：

```text
OrderCreated
    ↓
Saga
    ↓
ReserveInventoryCommand
    ↓
InventoryReserved
    ↓
CapturePaymentCommand
    ↓
PaymentCaptured
    ↓
ShipOrderCommand
```

Saga 不属于 Domain Aggregate。

---

# 87. Saga 所属层

推荐：

```text
Application / Process Layer
```

而不是：

```text
Domain Aggregate
```

原因：

Saga 编排：

```text
Multiple Aggregates
Multiple Transactions
Events
Commands
Retries
Compensation
```

属于应用流程。

---

# 88. Compensation

分布式流程失败：

```text
Reserve Inventory
       ↓
Payment
       ↓
Payment Failed
```

可能执行：

```text
Release Inventory
```

这是：

```text
Compensating Command
```

不是数据库 Rollback。

---

# 89. Transaction 与 Event

必须区分：

```text
Local Transaction
```

和：

```text
Distributed Workflow
```

Local：

```text
Aggregate + Outbox
```

Distributed：

```text
Event + Command + Saga
```

---

# 90. Eventual Consistency

跨 Aggregate 默认：

```text
Eventually Consistent
```

框架不能让开发者误以为：

```text
Event Published
=
Other Aggregate Already Updated
```

---

# 91. CQRS Consistency Contract

每个 Query API 应明确：

```text
Consistency:
    Strong
    Read-Your-Writes
    Eventual
```

例如：

```text
GetOrder
    consistency = eventual
```

---

# 92. Read-Your-Writes

用户刚刚执行：

```text
PayOrder
```

立即查询：

```text
GetOrder
```

可能 Projection 尚未完成。

框架可以支持：

```text
Command Result
```

直接返回最新状态摘要。

或者：

```text
Read-Your-Writes Token
```

后续版本再实现。

---

# 93. Event Replay

Projection 应支持：

```text
from sequence
from timestamp
from eventId
from beginning
```

至少概念上支持：

```text
Replay
```

---

# 94. Event Replay 安全原则

Replay 不应：

```text
重新执行外部支付
重新发送邮件
重新创建资源
```

Projection 与 Side Effect Handler 必须分离。

---

# 95. Projection 与 Side Effect

```text
Event
 ├── Projection
 │      └── Read Model
 │
 └── Process
        └── Command
```

Projection：

```text
Pure-ish
Idempotent
Rebuildable
```

Side Effect：

```text
Command driven
Retryable
Compensatable
```

---

# 96. Event Handler 分类

推荐三类：

```text
Projection Handler
Integration Handler
Process Handler
```

---

# 97. Projection Handler

职责：

```text
Event
 ↓
Read Model
```

---

# 98. Integration Handler

职责：

```text
Domain Event
 ↓
Integration Event
```

---

# 99. Process Handler

职责：

```text
Event
 ↓
Application Process
 ↓
Command
```

---

# 100. Event Architecture Formal Rules

### EVT-001

Domain Event 只能由 Domain Behavior 产生。

### EVT-002

Domain Event 不直接依赖 Broker。

### EVT-003

Event Publication 不属于 Aggregate。

### EVT-004

Outbox 与 Aggregate State 应在同一事务语义内提交。

### EVT-005

Event Consumer 默认必须幂等。

### EVT-006

同一 Aggregate Event 应具备顺序标识。

### EVT-007

全局 Event Ordering 不默认保证。

### EVT-008

Event Schema 必须支持版本化。

### EVT-009

Projection 必须支持重复消费。

### EVT-010

Projection 应支持重建。

### EVT-011

Event Replay 不得默认产生不可逆外部副作用。

### EVT-012

Event Sourcing 不是 CQRS 默认组成部分。

---

# 101. CQRS Formal Rules

### CQRS-001

Command 表达意图。

### CQRS-002

Query 不改变业务状态。

### CQRS-003

Command Handler 修改 Write Model。

### CQRS-004

Query Handler 查询 Read Model。

### CQRS-005

CommandBus 不承担 Domain Logic。

### CQRS-006

QueryBus 不承担 Domain Logic。

### CQRS-007

Write Repository 与 Query Repository 分离。

### CQRS-008

CQRS 不强制物理数据库分离。

### CQRS-009

Read Model 可以非规范化。

### CQRS-010

Write Model 必须维护 Aggregate Invariants。

---

# 102. Framework API Boundary

最终：

```text
ddd-core
    ↓
DDD primitives

ddd-domain
    ↓
Business model

ddd-application
    ↓
Use cases

ddd-cqrs
    ↓
Command / Query execution

ddd-event
    ↓
Event lifecycle

ddd-infrastructure
    ↓
Technology
```

---

# 103. 本阶段不加入

为了防止 Framework 过度膨胀，本阶段明确不默认加入：

```text
Event Sourcing
Distributed Transaction
Global Event Ordering
Workflow DSL
Rule DSL
Process DSL
Database ORM
Message Broker SDK
Spring
```

---

# 104. Maven Module 关系

推荐：

```xml
<dependency>
    <groupId>io.github.regalpine.ddd</groupId>
    <artifactId>ddd-domain</artifactId>
</dependency>
```

CQRS：

```xml
<dependency>
    <groupId>io.github.regalpine.ddd</groupId>
    <artifactId>ddd-application</artifactId>
</dependency>
```

Event：

```xml
<dependency>
    <groupId>io.github.regalpine.ddd</groupId>
    <artifactId>ddd-domain</artifactId>
</dependency>
```

---

# 105. Phase V 收敛声明

本阶段正式冻结：

```text
CommandBus
QueryBus
CommandMiddleware
QueryMiddleware
Handler Registry
Domain Event
Integration Event
Event Handler
Event Envelope
Outbox
Inbox
Projection
Event Ordering
Event Idempotency
Event Versioning
Event Replay
Saga Boundary
```

并冻结以下语义：

```text
Command
    = Intent

Query
    = Read Request

Domain Event
    = Business Fact

Integration Event
    = External Communication Fact

Outbox
    = Reliable Publication Buffer

Inbox
    = Consumer Deduplication Boundary

Projection
    = Event → Read Model

Saga
    = Cross-Transaction Process
```

---

# 106. Phase V 完成判定

```text
CommandBus                 ✅
QueryBus                   ✅
Middleware                 ✅
Handler Registry            ✅
CQRS Pipeline               ✅
Domain Event                ✅
Integration Event           ✅
Outbox                      ✅
Inbox                       ✅
Projection                  ✅
Event Ordering              ✅
Event Idempotency           ✅
Event Versioning            ✅
Replay                      ✅
Retry                       ✅
DLQ                         ✅
Saga Boundary               ✅
Event Sourcing Boundary     ✅
```

---

# 107. 下一阶段

进入：

# Phase VI — Transaction, Unit of Work & Consistency Specification

重点解决：

```text
Transaction
UnitOfWork
Aggregate Tracking
Dirty Checking
Commit
Rollback
Optimistic Concurrency
Outbox Atomicity
Idempotency Atomicity
Nested Transaction
Propagation
Isolation
Retry
Failure Recovery
```

最终形成：

```text
Command
   ↓
CommandBus
   ↓
Transaction
   ↓
UnitOfWork
   ↓
Aggregate
   ↓
Repository
   ↓
Outbox
   ↓
Commit
```

这一阶段完成后，框架的 **DDD + Application + CQRS + Event** 主干将基本闭合；之后再进入 Infrastructure、Adapter、Persistence、Spring Boot 集成，而不是继续改变核心架构。

# End of Phase V