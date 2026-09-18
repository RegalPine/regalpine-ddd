# RegalPine DDD Framework

## Phase IX — Integration & Messaging Specification

**Version:** v0.1  
**Status:** Draft for Convergence  
**GroupId:** `io.github.regalpine.ddd`  
**Java:** 17+  
**Build:** Maven  
**Architecture:** DDD + Hexagonal Architecture + CQRS + Event-Driven Architecture

---

# 1. 文档目的

Phase IX 定义 RegalPine DDD Framework 的：

- Integration Event
- Message
- Event Envelope
- Message Routing
- Topic / Queue
- Consumer Group
- Delivery Semantics
- Retry
- Dead Letter Queue
- Inbox
- Idempotency
- Event Ordering
- Schema Compatibility
- Consumer Versioning
- Message Security
- Message Observability
- Broker Adapter

本阶段的核心目标是：

> 将 Domain 内部事件与跨边界 Integration Messaging 正式分离，并建立稳定的消息基础设施契约。

Phase IX 不重新定义：

- Entity
- Value Object
- Aggregate
- Aggregate Root
- Domain Event
- Repository
- Command
- Query
- Command Handler
- Query Handler
- Transaction
- UnitOfWork
- CQRS
- Runtime

---

# 2. 架构定位

完整消息链路：

```text
                    DDD Domain
                        │
                        ▼
                 Domain Event
                        │
                        ▼
                Event Mapping
                        │
                        ▼
              Integration Event
                        │
                        ▼
                  Event Envelope
                        │
                        ▼
                     Outbox
                        │
                Transaction Commit
                        │
                        ▼
               Message Dispatcher
                        │
                        ▼
                  Broker Adapter
                        │
                        ▼
              ┌──────────────────┐
              │ Message Broker   │
              └──────────────────┘
                        │
          ┌─────────────┴─────────────┐
          ▼                           ▼
   Consumer Group A            Consumer Group B
          │                           │
          ▼                           ▼
       Inbox                     Inbox
          │                           │
          ▼                           ▼
   Integration Handler         Integration Handler
          │                           │
          ▼                           ▼
       Command                    Command
          │                           │
          ▼                           ▼
      Aggregate                  Aggregate
```

核心原则：

```text
Domain Event
≠
Integration Event
≠
Transport Message
```

三者不得混淆。

---

# 3. 设计原则

## 3.1 Domain 与 Messaging 解耦

Domain 不得依赖：

- Kafka
- RabbitMQ
- Pulsar
- RocketMQ
- JMS
- AMQP
- MQTT
- Redis Streams
- HTTP Messaging
- Broker SDK

Domain Event 是领域模型的一部分，而 Messaging 是基础设施能力。

---

# 4. 消息分层模型

定义五层：

```text
Domain Event
      ↓
Integration Event
      ↓
Message Envelope
      ↓
Transport Message
      ↓
Broker
```

## 4.1 Domain Event

表示：

> Aggregate 内部发生了一个具有业务意义的事实。

例如：

```text
OrderCreated
OrderPaid
OrderCancelled
```

Domain Event 不需要知道：

- Topic
- Queue
- Broker
- Consumer
- Serialization
- Retry

---

# 5. Integration Event

Integration Event 表示：

> 一个边界已经发生并需要被其他边界感知的业务事实。

建议：

```java
public interface IntegrationEvent {

    String eventType();

    int eventVersion();
}
```

示例：

```java
public record OrderCreatedIntegrationEvent(
        String orderId,
        String customerId,
        Instant createdAt
) implements IntegrationEvent {

    @Override
    public String eventType() {
        return "order.created";
    }

    @Override
    public int eventVersion() {
        return 1;
    }
}
```

---

# 6. Domain Event → Integration Event

禁止直接将所有 Domain Event 自动暴露为 Integration Event。

推荐：

```text
Domain Event
    ↓
Integration Event Mapper
    ↓
Integration Event
```

接口：

```java
public interface IntegrationEventMapper<D, I> {

    I map(D domainEvent);
}
```

原因：

1. Domain Event 属于内部模型。
2. Integration Event 属于稳定外部契约。
3. Domain Model 可以演进。
4. Integration Contract 应独立演进。
5. 防止内部模型泄漏。

---

# 7. Message Envelope

统一消息封装：

```java
public record MessageEnvelope(
        String messageId,
        String messageType,
        int schemaVersion,
        String source,
        String subject,
        String aggregateType,
        String aggregateId,
        Long sequence,
        Instant occurredAt,
        Instant publishedAt,
        String correlationId,
        String causationId,
        String tenantId,
        Map<String, String> headers,
        byte[] payload
) {}
```

---

# 8. Message ID

每条 Integration Message 必须具有唯一：

```text
messageId
```

要求：

- 全局唯一
- 不复用
- 不因重试改变
- 不因重新投递改变

例如：

```text
01K...
```

或：

```text
UUID
```

推荐支持：

```text
UUIDv7
ULID
```

---

# 9. Correlation ID

`correlationId` 表示：

> 一个业务流程的关联标识。

例如：

```text
OrderCreated
    correlationId = C1

PaymentRequested
    correlationId = C1

PaymentCompleted
    correlationId = C1
```

用于：

- 分布式追踪
- Saga
- Process Manager
- 审计
- 故障分析

---

# 10. Causation ID

`causationId` 表示：

> 当前消息由哪一条消息直接导致。

例如：

```text
OrderCreated
messageId = M1

PaymentRequested
causationId = M1
```

因此：

```text
Correlation
=
同一业务流程

Causation
=
直接因果关系
```

两者不得混淆。

---

# 11. Source

每条 Integration Event 应标识来源：

```text
source = order-service
```

推荐 URI 风格：

```text
service://order
service://payment
service://inventory
```

---

# 12. Subject

Subject 用于表示消息作用对象：

```text
subject = order/12345
```

推荐：

```text
aggregateType + aggregateId
```

例如：

```text
order/12345
```

---

# 13. Event Type

Event Type 必须稳定。

推荐：

```text
order.created
order.paid
order.cancelled
payment.completed
inventory.reserved
```

不推荐：

```text
OrderCreatedEventV1
```

版本应通过：

```text
messageType
schemaVersion
```

分离。

---

# 14. Schema Version

消息 Schema 必须版本化。

例如：

```text
messageType:
order.created

schemaVersion:
1
```

升级：

```text
order.created
version 2
```

不得因为内部 Java Class 名称改变而自动改变消息契约。

---

# 15. Message Contract

定义：

```java
public interface MessageContract {

    String messageType();

    int schemaVersion();

    Compatibility compatibility();
}
```

Compatibility：

```java
public enum Compatibility {
    BACKWARD,
    FORWARD,
    FULL,
    NONE
}
```

---

# 16. Schema Compatibility

## 16.1 Backward Compatible

新 Consumer 可以读取旧消息。

常见安全变化：

```text
新增 optional field
```

---

## 16.2 Forward Compatible

旧 Consumer 可以处理新消息。

---

## 16.3 Full Compatible

同时满足：

```text
Backward
+
Forward
```

---

## 16.4 Breaking Change

以下变化默认视为 Breaking：

```text
删除字段
改变字段类型
改变字段语义
改变单位
改变枚举含义
改变字段必填性
改变 ID 语义
改变事件业务含义
```

---

# 17. Message Serializer

定义：

```java
public interface MessageSerializer {

    byte[] serialize(Object message);

    <T> T deserialize(
            byte[] payload,
            Class<T> targetType);
}
```

框架不得强制：

- JSON
- Avro
- Protobuf
- XML

但必须允许 Adapter 实现。

---

# 18. Content Type

Message Envelope 应支持：

```text
contentType
```

例如：

```text
application/json
application/protobuf
application/avro
```

Content Type 属于 Transport / Integration 层，不属于 Domain。

---

# 19. Topic

Topic 用于消息发布逻辑分类。

例如：

```text
domain.order
integration.order
integration.payment
```

框架定义抽象：

```java
public interface Topic {

    String name();
}
```

---

# 20. Queue

Queue 表示消息消费目标。

```java
public interface Queue {

    String name();
}
```

Topic 与 Queue 不强制一一对应。

---

# 21. Consumer Group

Consumer Group 表示：

> 一组共享消费职责的消费者实例。

例如：

```text
order-created
    ↓
┌─────────────────────┐
│ payment-consumers   │
└─────────────────────┘

┌─────────────────────┐
│ inventory-consumers │
└─────────────────────┘
```

不同 Consumer Group 可以独立消费同一 Integration Event。

---

# 22. Message Routing

定义：

```java
public interface MessageRouter {

    Route route(MessageEnvelope message);
}
```

Route：

```java
public record Route(
        String topic,
        String queue,
        String consumerGroup
) {}
```

Routing 可以基于：

```text
messageType
tenant
source
aggregateType
header
```

但业务路由规则不得进入 Domain。

---

# 23. Publisher

定义：

```java
public interface MessagePublisher {

    PublishResult publish(MessageEnvelope message);
}
```

Publisher 只负责：

```text
Message
→ Broker
```

不负责：

- Domain Logic
- Transaction
- Aggregate
- Repository
- Saga

---

# 24. Broker Adapter

定义：

```java
public interface BrokerAdapter {

    PublishResult publish(
            MessageEnvelope message);

    void subscribe(
            Subscription subscription);
}
```

Adapter 可以实现：

```text
Kafka
RabbitMQ
Pulsar
RocketMQ
Redis Streams
JMS
```

但这些实现必须位于基础设施模块。

---

# 25. Delivery Semantics

框架标准支持：

```java
public enum DeliverySemantics {

    AT_MOST_ONCE,
    AT_LEAST_ONCE
}
```

默认：

```text
AT_LEAST_ONCE
```

原因：

- 数据可靠性优先
- 允许重复
- 通过 Inbox / Idempotency 消除重复影响

框架不得宣称：

```text
End-to-End Exactly Once
```

除非实际基础设施与业务事务能够证明该语义。

---

# 26. At-Most-Once

流程：

```text
Receive
 ↓
Mark Consumed
 ↓
Process
```

Process 失败可能造成消息丢失。

因此只适用于：

- 非关键通知
- 可丢失遥测
- Best Effort 场景

---

# 27. At-Least-Once

流程：

```text
Receive
 ↓
Process
 ↓
Commit
```

失败后：

```text
Retry
```

因此可能出现：

```text
M1
M1
M1
```

消费者必须幂等。

---

# 28. Inbox

Inbox 用于实现消费者幂等。

定义：

```java
public interface InboxStore {

    boolean exists(
            String consumerId,
            String messageId);

    void record(
            String consumerId,
            String messageId);
}
```

唯一约束：

```text
consumerId + messageId
```

---

# 29. Inbox Transaction

正确模型：

```text
BEGIN
   │
   ├── Check Inbox
   │
   ├── Execute Handler
   │
   ├── Business State Change
   │
   └── Record Inbox
   │
 COMMIT
```

禁止：

```text
Record Inbox
    ↓
Business Transaction
```

否则业务失败后消息可能被永久标记为已处理。

---

# 30. Consumer

定义：

```java
public interface MessageConsumer {

    void consume(MessageEnvelope message);
}
```

标准处理：

```text
Receive
 ↓
Deserialize
 ↓
Validate
 ↓
Inbox Check
 ↓
Handler
 ↓
Business Transaction
 ↓
Inbox Commit
```

---

# 31. Integration Event Handler

```java
public interface IntegrationEventHandler<E> {

    void handle(E event);
}
```

Handler 不应该直接依赖 Broker API。

---

# 32. Message → Command

跨 Aggregate 操作推荐：

```text
Integration Event
        ↓
Process Handler
        ↓
Command
        ↓
CommandBus
        ↓
Aggregate
```

而不是：

```text
Integration Event
        ↓
直接修改 Aggregate
```

这样能够保持：

```text
Command
=
业务意图

Event
=
已经发生的事实
```

---

# 33. Retry

定义：

```java
public interface RetryPolicy {

    RetryDecision next(
            int attempt,
            Throwable error);
}
```

支持：

```text
Fixed
Exponential
Exponential + Jitter
```

推荐默认：

```text
Exponential + Jitter
```

---

# 34. Retryable Failure

默认可以 Retry：

```text
Network timeout
Broker unavailable
Temporary database unavailable
Transient connection failure
Optimistic concurrency conflict
```

---

# 35. Non-Retryable Failure

默认不得无限 Retry：

```text
ValidationException
AuthorizationException
SchemaValidationException
BusinessRuleViolation
UnsupportedMessageVersion
InvalidPayload
```

---

# 36. Retry 次数

必须存在有限上限：

```text
maxAttempts
```

禁止：

```text
无限 Retry
```

---

# 37. Dead Letter Queue

超过 Retry 上限：

```text
Message
 ↓
Retry
 ↓
Retry
 ↓
Retry
 ↓
DLQ
```

定义：

```java
public interface DeadLetterPublisher {

    void publish(
            MessageEnvelope message,
            Throwable cause,
            int attempts);
}
```

---

# 38. DLQ Message

DLQ 必须保留：

```text
originalMessageId
originalMessageType
originalPayload
failureReason
failureType
attemptCount
firstReceivedAt
lastFailedAt
consumerId
```

用于：

- 故障诊断
- 人工处理
- Replay
- 审计

---

# 39. Replay

DLQ 可以 Replay：

```text
DLQ
 ↓
Replay
 ↓
Original Queue
```

Replay 必须生成新的：

```text
delivery attempt
```

但不得修改：

```text
originalMessageId
```

除非明确建立新的业务事件。

---

# 40. Replay 安全

Replay 不应默认触发不可逆外部副作用。

例如：

```text
发送短信
支付
扣款
删除外部资源
```

必须具备额外：

```text
Replay Policy
```

---

# 41. Ordering

框架默认：

```text
No Global Ordering
```

同一个 Aggregate 可以要求：

```text
aggregateId + sequence
```

例如：

```text
Order/100
sequence=1 Created

Order/100
sequence=2 Paid

Order/100
sequence=3 Shipped
```

---

# 42. Ordering Key

定义：

```java
public interface OrderingKeyProvider {

    String orderingKey(MessageEnvelope message);
}
```

推荐：

```text
aggregateType + ":" + aggregateId
```

例如：

```text
Order:100
```

---

# 43. Sequence

Sequence 必须来自可靠的业务写入顺序。

不能使用：

```text
consumer timestamp
```

作为业务顺序依据。

---

# 44. Event Gap

如果收到：

```text
sequence=3
```

但：

```text
sequence=2
```

尚未到达：

```text
Consumer
 ↓
Detect Gap
 ↓
Wait / Retry
```

不能直接假设：

```text
sequence=2 永远不会到达
```

---

# 45. Consumer Concurrency

允许：

```text
不同 Aggregate 并行
```

但同一个 Aggregate 的有序消息：

```text
Order/100
1 → 2 → 3
```

必须保持顺序。

---

# 46. Consumer Versioning

支持：

```text
Consumer V1
Consumer V2
```

可以同时运行。

例如：

```text
order.created.v1
```

逐步迁移：

```text
Consumer V1
      ↓
Consumer V2
      ↓
V1 retired
```

不要求 Producer 与 Consumer 同时升级。

---

# 47. Consumer Compatibility

Consumer 必须声明支持：

```java
public interface MessageCompatibility {

    boolean supports(
            String messageType,
            int schemaVersion);
}
```

不支持的消息不得进入业务处理。

---

# 48. Schema Registry

Schema Registry 是可选基础设施能力。

定义：

```java
public interface SchemaRegistry {

    SchemaDefinition get(
            String messageType,
            int version);

    void register(
            SchemaDefinition schema);
}
```

Schema：

```java
public record SchemaDefinition(
        String messageType,
        int version,
        String contentType,
        byte[] definition
) {}
```

---

# 49. Schema Registry 原则

Schema Registry：

```text
Integration Layer
```

不是：

```text
Domain Layer
```

Domain 不得依赖 Schema Registry。

---

# 50. Security

消息系统必须支持：

```text
Authentication
Authorization
Encryption
Integrity
Tenant Isolation
Audit
```

---

# 51. Transport Security

Broker Adapter 必须支持：

```text
TLS
```

必要时：

```text
mTLS
```

---

# 52. Message Integrity

高安全场景支持：

```text
Digital Signature
Message Digest
HMAC
```

但：

> 签名机制属于 Infrastructure / Security Adapter，不进入 Domain Event。

---

# 53. Sensitive Data

消息中不得默认携带：

```text
Password
Access Token
Private Key
Session Credential
完整支付卡数据
不必要的个人敏感数据
```

---

# 54. Tenant Isolation

多租户系统中：

```text
tenantId
```

属于消息上下文的重要字段。

Consumer 必须验证：

```text
Message Tenant
=
Authorized Tenant Context
```

不得仅依赖 Payload 中业务字段判断租户。

---

# 55. Message Authorization

消息消费需要区分：

```text
Transport Authorization
```

与：

```text
Business Authorization
```

Transport Authorization：

```text
谁可以发布/消费 Topic
```

Business Authorization：

```text
谁可以执行业务操作
```

两者不得混淆。

---

# 56. Message Observability

必须支持：

```text
messageId
correlationId
causationId
consumerId
consumerGroup
messageType
schemaVersion
```

用于诊断。

---

# 57. Metrics

至少提供：

```text
messages.published
messages.consumed
messages.failed
messages.retried
messages.dead_lettered
messages.duplicated
messages.replayed
message.processing.duration
message.lag
```

---

# 58. Broker Metrics

基础设施应提供：

```text
publish latency
consume latency
connection failures
broker errors
consumer lag
queue depth
```

---

# 59. Logging

允许记录：

```text
messageId
messageType
schemaVersion
source
consumerId
correlationId
causationId
```

默认不得记录完整敏感 Payload。

---

# 60. Distributed Tracing

消息传播：

```text
HTTP Request
   ↓
Command
   ↓
Domain Event
   ↓
Integration Event
   ↓
Message
   ↓
Consumer
```

Trace Context 应通过：

```text
Message Headers
```

传播。

---

# 61. Trace 与 Correlation 的区别

```text
Trace ID
=
技术调用链

Correlation ID
=
业务流程

Causation ID
=
直接业务因果
```

三者必须独立。

---

# 62. Message Failure Model

统一：

```text
SUCCESS
RETRY
DEAD_LETTER
DROP
```

定义：

```java
public enum MessageHandlingResult {

    SUCCESS,
    RETRY,
    DEAD_LETTER,
    DROP
}
```

`DROP` 必须具备明确策略和审计能力。

---

# 63. Message Processing Pipeline

标准 Pipeline：

```text
Receive
 ↓
Authentication
 ↓
Deserialize
 ↓
Schema Validation
 ↓
Compatibility Check
 ↓
Inbox Check
 ↓
Business Handler
 ↓
Domain / Command
 ↓
Transaction
 ↓
Inbox Commit
 ↓
ACK
```

---

# 64. ACK

ACK 必须发生在业务处理成功之后。

正确：

```text
Business Commit
 ↓
ACK
```

错误：

```text
ACK
 ↓
Business Commit
```

否则可能丢失消息。

---

# 65. Transaction Boundary

消息消费：

```text
Message
 ↓
Consumer Transaction
 ├── Domain State
 ├── Outbox
 └── Inbox
 ↓
Commit
 ↓
ACK
```

这保证：

```text
Business State
+
Inbox
+
Outbox
```

在同一事务中保持一致。

---

# 66. Integration Event 与 Outbox

标准发布模型：

```text
Aggregate
 ↓
Domain Event
 ↓
Integration Mapping
 ↓
Outbox
 ↓
Commit
 ↓
Dispatcher
 ↓
Broker
```

不得：

```text
Aggregate
 ↓
Broker
```

---

# 67. Outbox 与 Message ID

Outbox Record 必须具有稳定：

```text
messageId
```

Dispatcher Retry 时：

```text
messageId 不变
```

从而允许消费者 Inbox 去重。

---

# 68. Publisher Retry

Publisher 失败：

```text
Outbox remains pending
```

然后：

```text
retry
```

不得因为 Broker 临时失败而回滚已经提交的业务事务。

---

# 69. Consumer Retry

Consumer 失败：

```text
Inbox not committed
```

重新消费：

```text
same messageId
```

最终：

```text
success → Inbox commit
```

---

# 70. Message Lifecycle

完整生命周期：

```text
CREATED
   ↓
OUTBOXED
   ↓
COMMITTED
   ↓
PUBLISHED
   ↓
RECEIVED
   ↓
PROCESSING
   ↓
PROCESSED
```

异常：

```text
PROCESSING
   ↓
FAILED
   ↓
RETRYING
   ↓
PROCESSING
```

最终：

```text
DEAD_LETTER
```

---

# 71. Message State

建议：

```java
public enum MessageState {

    CREATED,
    OUTBOXED,
    PUBLISHED,
    RECEIVED,
    PROCESSING,
    PROCESSED,
    RETRYING,
    DEAD_LETTERED
}
```

状态仅表示 Messaging 生命周期。

不得将其混入 Domain Entity 状态。

---

# 72. Message Metadata

Metadata 推荐：

```text
messageId
messageType
schemaVersion
source
subject
aggregateType
aggregateId
sequence
occurredAt
publishedAt
correlationId
causationId
tenantId
traceId
headers
```

---

# 73. Message Header

Header 适合：

```text
Trace Context
Locale
Content Type
Security Context
Routing Metadata
```

不适合：

```text
Business State
```

业务事实必须进入 Payload。

---

# 74. Message Payload

Payload 必须：

```text
self-contained
versioned
serializable
contract-defined
```

不得直接序列化：

```text
Aggregate
Entity Graph
JPA Entity
Domain Service
Repository
```

---

# 75. Aggregate Snapshot 禁止默认作为 Integration Event

不建议：

```text
Order Aggregate JSON
```

直接作为 Integration Event。

应该：

```text
OrderPaidIntegrationEvent
```

仅暴露所需业务事实。

---

# 76. Anti-Corruption Boundary

外部系统事件：

```text
External Message
 ↓
Anti-Corruption Layer
 ↓
Internal Command / Domain Concept
```

不得让外部模型直接进入 Domain。

---

# 77. External Message Adapter

定义：

```java
public interface ExternalMessageMapper<E, I> {

    I translate(E externalMessage);
}
```

例如：

```text
SAP Order
 ↓
ExternalOrderMapper
 ↓
OrderImportCommand
```

---

# 78. Message Contract Governance

每个 Integration Event 至少定义：

```text
Name
Version
Owner
Source
Schema
Compatibility
Security Classification
Retention
Ordering
Delivery Semantics
Consumer
```

---

# 79. Retention

消息保留策略由 Infrastructure 决定。

例如：

```text
Hot
Cold
Archive
Delete
```

Domain 不关心 Broker Retention。

---

# 80. Message Replay Governance

Replay 必须支持：

```text
Who
When
Why
Which Message
Which Consumer
Which Version
Result
```

因此 Replay 是：

```text
Governance + Infrastructure
```

而不是 Domain Capability。

---

# 81. Message Filtering

Consumer 可以过滤：

```text
messageType
tenant
aggregateType
headers
```

但不得将复杂业务规则写入 Broker Filter。

复杂业务规则进入 Application。

---

# 82. Message Ordering 与 Scaling

如果要求同 Aggregate 顺序：

```text
Ordering Key
=
Aggregate ID
```

扩容：

```text
Partition
   ↓
Aggregate Key
   ↓
Same Partition
```

具体 Partition 机制由 Broker Adapter 决定。

Framework 不绑定 Kafka Partition。

---

# 83. Message Backpressure

Consumer 必须支持：

```text
maxConcurrency
maxInFlight
batchSize
pollTimeout
processingTimeout
```

防止：

```text
Broker
 ↓
Consumer
 ↓
无限任务
```

导致内存耗尽。

---

# 84. Poison Message

无法成功处理且 Retry 无意义的消息称为：

```text
Poison Message
```

处理：

```text
Schema Invalid
       ↓
DLQ
```

不得无限 Retry。

---

# 85. Broker Failure

Broker unavailable：

```text
Outbox
 ↓
Pending
```

业务事务仍然可以成功提交。

恢复后：

```text
Dispatcher
 ↓
Republish
```

---

# 86. Consumer Failure

Consumer crash：

```text
Message
 ↓
No Inbox Commit
 ↓
Message Redelivery
```

因此：

```text
Consumer MUST be Idempotent
```

---

# 87. Duplicate Message

收到：

```text
messageId=M1
```

Inbox：

```text
M1 exists
```

则：

```text
skip business execution
ACK
```

---

# 88. Idempotency Key

消息幂等：

```text
consumerId + messageId
```

业务幂等：

```text
businessIdempotencyKey
```

两者不能混淆。

---

# 89. Message Bus

定义：

```java
public interface MessageBus {

    PublishResult publish(
            MessageEnvelope message);
}
```

MessageBus 是 Integration abstraction。

它不得成为 Domain Event Bus。

---

# 90. Event Bus 与 Message Bus

如果框架提供：

```text
DomainEventDispatcher
```

则其作用范围：

```text
Domain/Application Process
```

而：

```text
MessageBus
```

作用范围：

```text
Process / Integration / Infrastructure
```

二者必须分离。

---

# 91. Module Architecture

Phase IX 推荐：

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
ddd-messaging
   ↑
ddd-infrastructure
   ↑
┌──────────────┬───────────────┬──────────────┐
Kafka Adapter  Rabbit Adapter  Other Adapter
```

---

# 92. ddd-messaging

建议新增：

```text
ddd-messaging
```

职责：

```text
IntegrationEvent
MessageEnvelope
MessageContract
MessagePublisher
MessageConsumer
MessageRouter
RetryPolicy
Inbox
DLQ
Schema
Delivery Semantics
```

不包含：

```text
Kafka implementation
RabbitMQ implementation
Database implementation
```

---

# 93. ddd-infrastructure

负责：

```text
Outbox Persistence
Inbox Persistence
Broker Adapter
Serialization
Schema Registry
Retry Scheduler
Message Dispatcher
```

---

# 94. Adapter Modules

只有存在实际技术依赖时才拆分：

```text
ddd-messaging-kafka
ddd-messaging-rabbitmq
ddd-messaging-pulsar
ddd-messaging-rocketmq
```

不要求所有项目同时引入。

---

# 95. Maven Dependency Rules

推荐：

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
ddd-messaging
   ↑
ddd-infrastructure
```

Adapter：

```text
ddd-messaging-kafka
        ↓
ddd-messaging
```

而不是：

```text
ddd-messaging
        ↓
Kafka
```

---

# 96. Forbidden Dependencies

以下依赖禁止进入：

```text
ddd-core
ddd-domain
```

包括：

```text
Kafka Client
Rabbit Client
JPA
Hibernate
Spring Messaging
Spring Kafka
JMS
Redis Client
HTTP Client
```

---

# 97. Hexagonal Architecture

Messaging Adapter 属于：

```text
Infrastructure Adapter
```

模型：

```text
                 Application
                      │
              Integration Port
                      │
              ddd-messaging
                      │
              Broker Adapter
                      │
                   Broker
```

---

# 98. Port / Adapter

Inbound：

```text
Broker
 ↓
Message Consumer Adapter
 ↓
Integration Handler
```

Outbound：

```text
Outbox Dispatcher
 ↓
Message Publisher Port
 ↓
Broker Adapter
```

---

# 99. CQRS Integration

Integration Event 通常进入：

```text
Integration Event
 ↓
Application Process
 ↓
Command
 ↓
CommandBus
 ↓
Write Model
```

Query Side：

```text
Integration Event
 ↓
Projection
 ↓
Read Model
```

因此 Messaging 同时支持：

```text
Command-oriented Integration
Event-oriented Integration
Projection
Saga
Process Manager
```

---

# 100. Saga

Saga 不属于 Messaging 本身。

正确定位：

```text
ddd-messaging
       ↓
Integration Event
       ↓
ddd-application
       ↓
Saga / Process Manager
```

Messaging 负责：

```text
delivery
```

Saga 负责：

```text
business process
```

---

# 101. Distributed Transaction

Framework 不将：

```text
XA
2PC
Distributed Transaction
```

作为默认机制。

推荐：

```text
Local Transaction
+
Outbox
+
Inbox
+
Saga
```

---

# 102. Failure Matrix

| Failure | Strategy |
|---|---|
| Broker unavailable | Outbox retry |
| Network timeout | Retry |
| Consumer crash | Redelivery |
| Duplicate message | Inbox |
| Schema incompatible | DLQ |
| Business validation failure | DLQ / Reject |
| Temporary DB failure | Retry |
| Concurrency conflict | Retry |
| Poison message | DLQ |
| Unknown event type | DLQ |
| Unauthorized message | Reject / DLQ |

---

# 103. Formal Rules

## MSG-001

Integration Event 必须与 Domain Event 分离。

## MSG-002

Domain 不得依赖 Broker。

## MSG-003

Integration Event 必须可版本化。

## MSG-004

Message ID 必须稳定且唯一。

## MSG-005

Retry 不得改变 Message ID。

## MSG-006

Consumer 默认采用 At-Least-Once。

## MSG-007

At-Least-Once Consumer 必须支持幂等。

## MSG-008

Inbox 唯一键为：

```text
consumerId + messageId
```

## MSG-009

Inbox 与业务处理必须具有一致性边界。

## MSG-010

ACK 必须发生在成功处理之后。

## MSG-011

Outbox Dispatcher 不得修改已经提交的业务事务。

## MSG-012

Broker 不得成为 Domain 的依赖。

## MSG-013

消息顺序默认只保证显式声明的 Ordering Key。

## MSG-014

框架不得默认提供 Global Ordering。

## MSG-015

Retry 必须有限。

## MSG-016

Poison Message 必须能够进入 DLQ。

## MSG-017

Replay 必须可审计。

## MSG-018

Replay 不得默认产生不可逆外部副作用。

## MSG-019

Schema Breaking Change 必须升级版本或提供兼容机制。

## MSG-020

Consumer 不得依赖 Producer 的内部 Domain Model。

---

# 104. Security Rules

## MSG-SEC-001

消息传输必须支持 TLS。

## MSG-SEC-002

敏感 Payload 不得默认进入日志。

## MSG-SEC-003

Consumer 必须执行授权边界检查。

## MSG-SEC-004

Tenant Message 不得跨租户处理。

## MSG-SEC-005

Replay 必须经过授权。

## MSG-SEC-006

DLQ 必须受到访问控制。

## MSG-SEC-007

Schema Registry 必须受到变更权限控制。

---

# 105. Observability Rules

## MSG-OBS-001

每条消息必须可通过 Message ID 定位。

## MSG-OBS-002

支持 Correlation ID。

## MSG-OBS-003

支持 Causation ID。

## MSG-OBS-004

支持 Consumer 维度指标。

## MSG-OBS-005

必须监控 DLQ。

## MSG-OBS-006

必须监控 Retry。

## MSG-OBS-007

必须监控 Consumer Lag。

## MSG-OBS-008

不得记录敏感 Payload。

---

# 106. API Package

推荐：

```text
io.github.regalpine.ddd.messaging
├── IntegrationEvent
├── MessageEnvelope
├── MessageContract
├── MessagePublisher
├── MessageConsumer
├── MessageBus
├── MessageRouter
├── MessageHandler
│
├── delivery
│   ├── DeliverySemantics
│   └── PublishResult
│
├── retry
│   ├── RetryPolicy
│   └── RetryDecision
│
├── inbox
│   └── InboxStore
│
├── dlq
│   └── DeadLetterPublisher
│
├── schema
│   ├── SchemaRegistry
│   └── SchemaDefinition
│
├── routing
│   ├── Route
│   └── OrderingKeyProvider
│
└── security
    └── MessageSecurityPolicy
```

---

# 107. Reference Runtime

完整运行时：

```text
                 ┌───────────────┐
                 │    Domain     │
                 └───────┬───────┘
                         │
                  Domain Event
                         │
                         ▼
                Integration Mapper
                         │
                         ▼
                  Integration Event
                         │
                         ▼
                      Outbox
                         │
                    Transaction
                         │
                       Commit
                         │
                         ▼
                  Message Dispatcher
                         │
                         ▼
                  Message Publisher
                         │
                         ▼
                     Broker
                         │
                         ▼
                    Consumer
                         │
                         ▼
                      Inbox
                         │
                         ▼
                Integration Handler
                         │
               ┌─────────┴─────────┐
               ▼                   ▼
            Command             Projection
               │                   │
               ▼                   ▼
           Write Model         Read Model
```

---

# 108. Phase IX 完成度

| Capability | Status |
|---|---|
| Integration Event | Complete |
| Event Envelope | Complete |
| Message Contract | Complete |
| Schema Version | Complete |
| Serialization Contract | Complete |
| Message Routing | Complete |
| Topic | Complete |
| Queue | Complete |
| Consumer Group | Complete |
| Delivery Semantics | Complete |
| Publisher | Complete |
| Consumer | Complete |
| Inbox | Complete |
| Idempotency | Complete |
| Retry | Complete |
| DLQ | Complete |
| Replay | Complete |
| Ordering | Complete |
| Sequence | Complete |
| Schema Registry | Complete |
| Consumer Versioning | Complete |
| Message Security | Complete |
| Tenant Isolation | Complete |
| Observability | Complete |
| Broker Adapter | Complete |
| Outbox Integration | Complete |
| CQRS Integration | Complete |
| Saga Boundary | Complete |
| Anti-Corruption Boundary | Complete |

---

# 109. Phase IX 架构收敛结论

Phase IX 完成后，RegalPine DDD Framework 的核心通信模型已经形成：

```text
Domain
  ↓
Domain Event
  ↓
Integration Event
  ↓
Outbox
  ↓
Message
  ↓
Broker
  ↓
Inbox
  ↓
Application
  ↓
Command
  ↓
Aggregate
```

这条链路覆盖企业级 DDD Framework 最核心的跨边界通信场景。

特别重要的是：

```text
Domain
    不知道
        ↓
     Messaging
        ↓
      Broker
```

因此：

> Messaging 是 Framework Runtime 能力，而不是 Domain Model 能力。

---

# 110. 当前总体架构

Phase I–IX 后：

```text
┌──────────────────────────────────────────────┐
│                  Developer                   │
├──────────────────────────────────────────────┤
│             Application / CQRS               │
│       Command / Query / Handler / UseCase     │
├──────────────────────────────────────────────┤
│                    Domain                    │
│ Entity / VO / Aggregate / Event / Repository │
├──────────────────────────────────────────────┤
│              Transaction / UoW               │
├──────────────────────────────────────────────┤
│                    Runtime                   │
├──────────────────────────────────────────────┤
│               Messaging / Event              │
├──────────────────────────────────────────────┤
│               Infrastructure                 │
├──────────────────────────────────────────────┤
│        JDBC / JPA / Kafka / Redis / ...      │
└──────────────────────────────────────────────┘
```

依赖方向：

```text
Infrastructure
      ↓
Messaging
      ↓
Runtime
      ↓
Application
      ↓
Domain
      ↓
Core
```

Domain 永远不能反向依赖 Infrastructure。

---

# 111. 收敛原则

从 Phase IX 开始，不再因为增加一个具体技术能力而继续扩大核心抽象。

以下模型原则上冻结：

```text
Entity
ValueObject
Aggregate
DomainEvent
Repository
Command
Query
CommandHandler
QueryHandler
CommandBus
QueryBus
Transaction
UnitOfWork
IntegrationEvent
Message
Outbox
Inbox
Runtime
Adapter
```

后续阶段重点转向：

```text
Implementation
Testing
Conformance
Developer Experience
Documentation
Reference Applications
```

而不是继续创造新的核心 DDD 抽象。

---

# 112. Phase X 预定范围

Phase X 建议进入：

## Developer Framework Implementation & Conformance Specification

主要解决：

```text
1. Maven Multi-Module 最终结构
2. Java 17 API 完整实现
3. SPI / Adapter 机制
4. Annotation 使用边界
5. Runtime Bootstrap
6. Command / Query Handler Registration
7. Repository Registration
8. Transaction Adapter
9. Outbox / Inbox 实现
10. Messaging Adapter SPI
11. Test Framework
12. In-Memory Adapter
13. Reference Implementation
14. Conformance Test Kit
15. API Stability
16. Compatibility Policy
17. Framework Versioning
18. Developer Experience
```

Phase X 的目标不是继续“发明”架构，而是：

> **把 Phase I–IX 已经确定的架构真正落成一个可以被 Java 开发者直接使用的 RegalPine DDD Framework。**

---