# RegalPine DDD Framework

## Phase XI — Reference Implementation Specification

**Version:** v0.1  
**Status:** Reference Implementation Baseline  
**GroupId:** `io.github.regalpine.ddd`  
**Java:** 17+  
**Build:** Maven  
**License:** TBD  
**Architecture:** DDD + Hexagonal Architecture + CQRS + Event-Driven Architecture

---

# 1. 文档定位

Phase XI 将 Phase I–X 定义的 Framework Contract 转化为：

```text
Java API
+
Reference Implementation
+
Test Infrastructure
+
Example Application
```

本阶段的核心目标：

> **Freeze Architecture → Implement Contract → Verify Contract**

不再增加新的核心 DDD 抽象。

---

# 2. Reference Implementation 原则

Reference Implementation 必须满足：

```text
Java 17
No mandatory Spring
No mandatory ORM
No mandatory Broker
No mandatory Cloud SDK
No vendor lock-in
```

最小运行环境：

```text
JDK 17
Maven 3.9+
```

即可运行：

```text
Domain
Application
CQRS
Runtime
In-Memory Repository
In-Memory Transaction
In-Memory Event
```

---

# 3. 最终 Maven Reactor

```text
regalpine-ddd/
│
├── pom.xml
│
├── ddd-core/
├── ddd-domain/
├── ddd-application/
├── ddd-cqrs/
├── ddd-event/
├── ddd-transaction/
├── ddd-runtime/
├── ddd-messaging/
│
├── ddd-infrastructure/
│
├── ddd-test/
├── ddd-conformance/
│
├── ddd-infrastructure-jdbc/
├── ddd-messaging-kafka/
├── ddd-messaging-rabbitmq/
│
├── ddd-spring-boot/
│
└── examples/
    └── order-service/
```

---

# 4. 模块分类

## 4.1 Core

```text
ddd-core
ddd-domain
ddd-application
```

## 4.2 Runtime

```text
ddd-cqrs
ddd-event
ddd-transaction
ddd-runtime
ddd-messaging
```

## 4.3 Infrastructure

```text
ddd-infrastructure
ddd-infrastructure-jdbc
```

## 4.4 Adapter

```text
ddd-messaging-kafka
ddd-messaging-rabbitmq
ddd-spring-boot
```

## 4.5 Test

```text
ddd-test
ddd-conformance
```

---

# 5. Root POM

根项目：

```xml
<groupId>io.github.regalpine.ddd</groupId>
<artifactId>regalpine-ddd</artifactId>
<version>1.0.0</version>
<packaging>pom</packaging>
```

统一：

```text
Java Version
Maven Compiler
Encoding
Dependency Management
Plugin Management
Test Configuration
```

---

# 6. Dependency Management

所有官方模块统一由 Root POM 管理版本。

禁止：

```text
Module A → 自己定义 Framework Version
Module B → 自己定义 Framework Version
```

统一：

```text
Root
 ↓
Dependency Management
 ↓
Modules
```

---

# 7. ddd-core Package

```text
io.github.regalpine.ddd.core
│
├── identifier
├── entity
├── aggregate
├── value
├── event
├── specification
├── error
├── exception
└── version
```

---

# 8. Identifier

```java
public interface Identifier {

    String value();
}
```

要求：

```text
Immutable
Stable
Comparable by value
```

---

# 9. String Identifier

Reference Implementation：

```java
public record StringIdentifier(String value)
        implements Identifier {

    public StringIdentifier {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Identifier value must not be blank");
        }
    }
}
```

---

# 10. Entity

```java
public interface Entity<I extends Identifier> {

    I id();
}
```

Entity 的 identity：

```text
immutable
```

不得在生命周期内改变。

---

# 11. Value Object

```java
public interface ValueObject {
}
```

推荐：

```java
record
```

实现。

例如：

```java
public record Money(
        BigDecimal amount,
        Currency currency
) implements ValueObject {
}
```

---

# 12. Aggregate Root

```java
public interface AggregateRoot<I extends Identifier>
        extends Entity<I>, DomainEventSource {
}
```

---

# 13. Domain Event Source

```java
public interface DomainEventSource {

    List<DomainEvent> pendingEvents();

    void clearPendingEvents();
}
```

---

# 14. Domain Event

```java
public interface DomainEvent {

    EventId eventId();

    AggregateType aggregateType();

    String aggregateId();

    Version aggregateVersion();

    Instant occurredAt();
}
```

---

# 15. Event ID

```java
public record EventId(String value) {

    public EventId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Event ID must not be blank");
        }
    }
}
```

---

# 16. Aggregate Type

```java
public record AggregateType(String value) {

    public AggregateType {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Aggregate type must not be blank");
        }
    }
}
```

---

# 17. Version

```java
public record Version(long value) {

    public Version {
        if (value < 0) {
            throw new IllegalArgumentException(
                    "Version must not be negative");
        }
    }

    public Version next() {
        return new Version(value + 1);
    }
}
```

---

# 18. Specification

```java
@FunctionalInterface
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);

    default Specification<T> and(
            Specification<T> other) {

        return candidate ->
                isSatisfiedBy(candidate)
                && other.isSatisfiedBy(candidate);
    }

    default Specification<T> or(
            Specification<T> other) {

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

# 19. ddd-domain

Package：

```text
io.github.regalpine.ddd.domain
```

结构：

```text
domain/
├── repository
├── service
├── policy
├── factory
├── specification
└── event
```

---

# 20. Repository

```java
public interface AggregateRepository<
        A extends AggregateRoot<I>,
        I extends Identifier> {

    Optional<A> findById(I id);

    void save(A aggregate);

    void delete(A aggregate);
}
```

Repository 表示：

> Aggregate 持久化访问抽象。

不是：

```text
Generic CRUD Framework
```

---

# 21. Repository Contract

Repository 必须保证：

```text
Identity
Version
Concurrency
Transaction Participation
```

---

# 22. Domain Service

```java
public interface DomainService {
}
```

Domain Service 只承载：

```text
无法自然属于单一 Entity / Aggregate
```

的领域行为。

不得成为：

```text
万能 Service
```

---

# 23. Domain Factory

Factory 负责：

```text
Complex Aggregate Creation
```

例如：

```java
public interface OrderFactory {

    Order create(
            CustomerId customerId,
            List<OrderLine> lines);
}
```

---

# 24. ddd-application

Package：

```text
io.github.regalpine.ddd.application
```

结构：

```text
application/
├── command
├── query
├── handler
├── middleware
├── authorization
├── idempotency
└── process
```

---

# 25. Command

```java
public interface Command<R> {
}
```

Command 表示：

```text
Intent
```

例如：

```java
public record CreateOrderCommand(
        String customerId,
        List<OrderLineInput> lines
) implements Command<String> {
}
```

---

# 26. Query

```java
public interface Query<R> {
}
```

Query：

```text
Read Intent
```

---

# 27. Command Handler

```java
public interface CommandHandler<
        C extends Command<R>, R> {

    R handle(C command);
}
```

---

# 28. Query Handler

```java
public interface QueryHandler<
        Q extends Query<R>, R> {

    R handle(Q query);
}
```

---

# 29. Command Bus

```java
public interface CommandBus {

    <R> R dispatch(Command<R> command);
}
```

---

# 30. Query Bus

```java
public interface QueryBus {

    <R> R dispatch(Query<R> query);
}
```

---

# 31. Handler Registry

内部 Runtime：

```java
public interface CommandHandlerRegistry {

    <R> void register(
            Class<? extends Command<R>> type,
            CommandHandler<? extends Command<R>, R> handler);

    CommandHandler<?, ?> resolve(
            Class<?> type);
}
```

Query 同理。

---

# 32. Handler Resolution

默认：

```text
Exact Type Match
```

即：

```text
Command Class
→
Exact Handler
```

不得默认使用复杂继承匹配。

原因：

```text
Predictability
Performance
Startup Validation
```

---

# 33. Duplicate Handler

如果：

```text
CreateOrderCommand
```

已经注册 Handler，再注册第二个：

```text
Startup / Registration Error
```

---

# 34. Middleware

```java
public interface Middleware {

    <R> R execute(
            InvocationContext context,
            InvocationChain chain);
}
```

---

# 35. Invocation Context

```java
public interface InvocationContext {

    Object request();

    Class<?> requestType();

    Map<String, Object> attributes();
}
```

`attributes` 属于 Application Runtime Context。

不得成为 Domain Service Locator。

---

# 36. Invocation Chain

```java
public interface InvocationChain {

    <R> R proceed(InvocationContext context);
}
```

---

# 37. ddd-cqrs

Package：

```text
io.github.regalpine.ddd.cqrs
```

负责：

```text
Command Bus
Query Bus
Pipeline
Handler Registry
Middleware
```

---

# 38. Default Command Pipeline

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
UnitOfWork
 ↓
Handler
```

---

# 39. Default Query Pipeline

```text
Tracing
 ↓
Validation
 ↓
Authorization
 ↓
Query Handler
```

默认不进入 Domain UoW。

---

# 40. Command Bus Reference Implementation

```java
public final class DefaultCommandBus
        implements CommandBus {

    private final CommandHandlerRegistry registry;
    private final List<Middleware> middleware;

    @Override
    public <R> R dispatch(Command<R> command) {

        InvocationContext context =
                new DefaultInvocationContext(command);

        InvocationChain chain =
                new DefaultInvocationChain(
                        registry,
                        middleware);

        return chain.proceed(context);
    }
}
```

实际实现必须保证：

```text
No recursive pipeline
No uncontrolled allocation
Deterministic middleware order
```

---

# 41. ddd-event

Package：

```text
io.github.regalpine.ddd.event
```

职责：

```text
Domain Event Dispatch
Event Mapping
Event Registration
Event Handler
```

---

# 42. Domain Event Handler

```java
public interface DomainEventHandler<E extends DomainEvent> {

    void handle(E event);
}
```

---

# 43. Domain Event Dispatcher

```java
public interface DomainEventDispatcher {

    void dispatch(List<DomainEvent> events);
}
```

注意：

> DomainEventDispatcher 不等于 Broker Publisher。

---

# 44. Event Mapping

```java
public interface IntegrationEventMapper<
        D extends DomainEvent,
        I extends IntegrationEvent> {

    I map(D event);
}
```

---

# 45. ddd-transaction

Package：

```text
io.github.regalpine.ddd.transaction
```

---

# 46. Transaction Definition

```java
public record TransactionDefinition(
        Propagation propagation,
        Isolation isolation,
        boolean readOnly,
        Duration timeout
) {
}
```

---

# 47. Transaction Adapter

```java
public interface TransactionAdapter {

    <T> T execute(
            TransactionDefinition definition,
            TransactionCallback<T> callback);
}
```

---

# 48. Transaction Callback

```java
@FunctionalInterface
public interface TransactionCallback<T> {

    T execute();
}
```

---

# 49. UnitOfWork

```java
public interface UnitOfWork {

    void register(
            AggregateRoot<?, ?> aggregate);

    void commit();

    void rollback();
}
```

---

# 50. UnitOfWork Manager

```java
public interface UnitOfWorkManager {

    boolean hasCurrent();

    UnitOfWork current();

    <T> T execute(
            UnitOfWorkCallback<T> callback);
}
```

---

# 51. Transaction / UoW Runtime

最终执行：

```text
TransactionAdapter
        ↓
UnitOfWorkManager
        ↓
CommandHandler
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

# 52. ddd-runtime

Package：

```text
io.github.regalpine.ddd.runtime
```

结构：

```text
runtime/
├── DddRuntime
├── DddRuntimeBuilder
├── RuntimeComponent
├── RuntimeContext
├── ComponentRegistry
├── RuntimeState
├── bootstrap
├── lifecycle
├── configuration
├── health
└── metrics
```

---

# 53. Runtime Component

```java
public interface RuntimeComponent {

    String name();

    void start(RuntimeContext context);

    void stop(RuntimeContext context);
}
```

---

# 54. Runtime Context

```java
public interface RuntimeContext {

    <T> Optional<T> get(Class<T> type);

    <T> T require(Class<T> type);
}
```

仅供：

```text
Runtime Components
Infrastructure
Adapters
```

使用。

---

# 55. Component Registry

```java
public interface ComponentRegistry {

    <T> void register(
            Class<T> type,
            T component);

    <T> Optional<T> find(
            Class<T> type);

    <T> T require(
            Class<T> type);
}
```

---

# 56. Runtime State

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

# 57. DddRuntime

```java
public interface DddRuntime {

    void start();

    RuntimeState state();

    void shutdown();
}
```

---

# 58. Runtime Builder

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

# 59. Runtime Bootstrap

```text
BUILD
 ↓
Validate Configuration
 ↓
Register Components
 ↓
Resolve Dependencies
 ↓
Validate Handlers
 ↓
Validate Repositories
 ↓
Initialize Adapters
 ↓
Initialize Messaging
 ↓
RUNNING
```

---

# 60. Dependency Graph

Runtime Component 可以声明：

```java
public interface ComponentDependency {

    Set<Class<?>> dependencies();
}
```

启动时：

```text
Dependency Graph
 ↓
Topological Sort
```

如果发现：

```text
A → B
B → A
```

立即失败。

---

# 61. ddd-messaging

Package：

```text
io.github.regalpine.ddd.messaging
```

---

# 62. Integration Event

```java
public interface IntegrationEvent {

    String eventType();

    int eventVersion();
}
```

---

# 63. Message Envelope

Reference：

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
) {
}
```

---

# 64. Message Publisher

```java
public interface MessagePublisher {

    PublishResult publish(
            MessageEnvelope message);
}
```

---

# 65. Message Consumer

```java
public interface MessageConsumer {

    void consume(
            MessageEnvelope message);
}
```

---

# 66. Inbox

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

---

# 67. Outbox

```java
public interface OutboxStore {

    void append(
            OutboxRecord record);

    List<OutboxRecord> loadPending(
            int limit);

    void markPublished(
            String messageId);
}
```

---

# 68. Retry

```java
public interface RetryPolicy {

    RetryDecision next(
            int attempt,
            Throwable error);
}
```

---

# 69. In-Memory Infrastructure

Reference Implementation 必须提供：

```text
io.github.regalpine.ddd.infrastructure.memory
```

结构：

```text
memory/
├── InMemoryRepository
├── InMemoryTransaction
├── InMemoryUnitOfWork
├── InMemoryOutboxStore
├── InMemoryInboxStore
├── InMemoryMessageBus
└── InMemoryClock
```

---

# 70. InMemoryRepository

```java
public final class InMemoryRepository<
        A extends AggregateRoot<I>,
        I extends Identifier>
        implements AggregateRepository<A, I> {

    private final Map<I, A> store =
            new ConcurrentHashMap<>();

    @Override
    public Optional<A> findById(I id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(A aggregate) {
        store.put(aggregate.id(), aggregate);
    }

    @Override
    public void delete(A aggregate) {
        store.remove(aggregate.id());
    }
}
```

Reference Implementation 中还必须补充：

```text
Version Checking
Copy Semantics
Concurrency Test
```

不能把简单 Map 版本直接当作完整生产实现。

---

# 71. Aggregate Copy

为了避免测试中绕过 Repository 直接修改内存对象：

```text
Repository
 ↓
Stored State
 ↓
Load
 ↓
Detached Aggregate
```

In-Memory 实现应支持明确的 Copy Strategy。

---

# 72. InMemoryTransaction

事务状态：

```text
NONE
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

# 73. InMemoryOutbox

Outbox 必须模拟：

```text
Pending
Published
Failed
Retry
```

---

# 74. InMemoryInbox

必须验证：

```text
consumerId + messageId
```

唯一性。

---

# 75. InMemoryMessageBus

用于：

```text
Integration Test
```

支持：

```text
Publish
Subscribe
Delivery
Duplicate
Retry
DLQ
```

---

# 76. Test Framework

Package：

```text
io.github.regalpine.ddd.test
```

结构：

```text
test/
├── aggregate
├── command
├── query
├── event
├── transaction
├── messaging
├── fixture
└── assertion
```

---

# 77. Aggregate Test Fixture

```java
public final class AggregateTestFixture<A> {

    public GivenStage<A> given(
            DomainEvent... events) {
        // ...
    }
}
```

使用：

```java
fixture
    .given(new OrderCreated(...))
    .when(new PayOrderCommand(...))
    .then()
    .expectEvent(OrderPaid.class);
```

---

# 78. Test Philosophy

测试重点：

```text
Business Behavior
```

而不是：

```text
Private Implementation
```

禁止大量测试：

```text
private method
getter
setter
internal registry
```

---

# 79. Domain Test

```text
Given
 ↓
When
 ↓
Then
```

示例：

```text
Given:
Order status = CREATED

When:
PayOrder

Then:
Order status = PAID

And:
OrderPaid event emitted
```

---

# 80. Application Test

测试：

```text
Command
 ↓
Handler
 ↓
Repository
 ↓
Aggregate
```

---

# 81. CQRS Test

验证：

```text
CommandBus
QueryBus
Middleware
Handler Resolution
```

---

# 82. Transaction Test

必须验证：

```text
Successful Commit
Rollback
Nested Execution
Concurrency Conflict
Outbox Atomicity
```

---

# 83. Messaging Test

必须验证：

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

# 84. Conformance Module

```text
ddd-conformance
```

不是业务测试。

它验证：

> Adapter 是否遵守 Framework Contract。

---

# 85. Repository Conformance

抽象测试：

```java
public abstract class RepositoryContractTest<
        A extends AggregateRoot<I>,
        I extends Identifier> {

    protected abstract AggregateRepository<A, I>
            createRepository();

    @Test
    void saveAndFind() {}

    @Test
    void delete() {}

    @Test
    void identityMustRemainStable() {}

    @Test
    void optimisticConcurrency() {}
}
```

---

# 86. Transaction Conformance

```text
commitMustPersist
rollbackMustNotPersist
nestedTransactionMustFollowPropagation
contextMustBeCleared
```

---

# 87. Messaging Conformance

```text
messageMustBeDelivered
duplicateMustBeDetected
retryMustBeBounded
dlqMustReceivePoisonMessage
```

---

# 88. Adapter Certification

一个 Adapter：

```text
implements SPI
```

并不等于：

```text
Conformant
```

只有：

```text
SPI
+
Conformance Tests
```

全部通过，才可以声明：

```text
RegalPine DDD Compatible
```

---

# 89. Example Application

建立：

```text
examples/order-service
```

用于验证全部核心能力。

---

# 90. Order Aggregate

```text
Order
├── OrderId
├── CustomerId
├── OrderStatus
├── OrderLine
└── Money
```

状态：

```text
CREATED
PAID
CANCELLED
SHIPPED
```

---

# 91. Order Commands

```text
CreateOrderCommand
PayOrderCommand
CancelOrderCommand
ShipOrderCommand
```

---

# 92. Order Queries

```text
GetOrderQuery
ListCustomerOrdersQuery
```

---

# 93. Order Events

Domain：

```text
OrderCreated
OrderPaid
OrderCancelled
OrderShipped
```

Integration：

```text
order.created
order.paid
order.cancelled
order.shipped
```

---

# 94. Order Flow

```text
CreateOrderCommand
        ↓
CreateOrderHandler
        ↓
Order.create()
        ↓
OrderCreated
        ↓
Repository.save()
        ↓
Outbox
        ↓
Commit
```

---

# 95. Payment Flow

```text
OrderPaid
    ↓
Payment Process
    ↓
CapturePaymentCommand
    ↓
Payment Aggregate
```

---

# 96. Inventory Flow

```text
OrderCreated
    ↓
ReserveInventoryCommand
    ↓
Inventory Aggregate
    ↓
InventoryReserved
```

---

# 97. Read Model

Projection：

```text
Order
 ↓
OrderSummaryProjection
 ↓
OrderSummaryReadModel
```

Read Model：

```java
public record OrderSummary(
        String orderId,
        String customerId,
        String status,
        BigDecimal total
) {}
```

---

# 98. Example Architecture

```text
order-service
│
├── domain
│   └── order
│
├── application
│   ├── command
│   ├── query
│   └── projection
│
├── infrastructure
│   ├── persistence
│   └── messaging
│
└── bootstrap
```

---

# 99. Example Bootstrap

概念：

```java
var runtime =
    DddRuntime.builder()
        .configuration(configuration)
        .register(orderRepository)
        .register(createOrderHandler)
        .register(payOrderHandler)
        .register(getOrderHandler)
        .build();

runtime.start();
```

应用程序不需要直接：

```text
new Transaction
new Outbox
new Inbox
new Broker Connection
```

---

# 100. Production Adapter

生产环境：

```text
InMemory
   ↓
JDBC / JPA
   ↓
PostgreSQL
```

Messaging：

```text
InMemory
   ↓
Kafka / RabbitMQ / Pulsar
```

Framework Contract 保持不变。

---

# 101. JDBC Adapter

未来：

```text
ddd-infrastructure-jdbc
```

负责：

```text
Repository
Transaction
Outbox
Inbox
```

不得将 JDBC API 引入：

```text
ddd-domain
```

---

# 102. Database Schema Boundary

Reference JDBC Schema 至少包括：

```text
aggregate tables
outbox
inbox
```

Outbox：

```text
message_id
message_type
schema_version
aggregate_type
aggregate_id
sequence
payload
headers
occurred_at
published_at
status
attempts
last_error
```

Inbox：

```text
consumer_id
message_id
processed_at
```

唯一约束：

```text
UNIQUE(consumer_id, message_id)
```

---

# 103. Optimistic Concurrency

Aggregate persistence：

```sql
UPDATE orders
SET ...
    version = version + 1
WHERE id = ?
  AND version = ?
```

如果：

```text
affected rows = 0
```

则：

```text
ConcurrencyConflict
```

---

# 104. Outbox Atomicity

数据库事务：

```text
BEGIN

UPDATE aggregate

INSERT outbox

COMMIT
```

不得：

```text
UPDATE aggregate
COMMIT

INSERT outbox
```

---

# 105. Inbox Atomicity

Consumer：

```text
BEGIN

check inbox

business operation

insert inbox

insert outbox

COMMIT
```

然后：

```text
ACK
```

---

# 106. Architecture Verification

CI 必须验证：

```text
ddd-core
    ↓
no framework dependency

ddd-domain
    ↓
no infrastructure dependency

ddd-application
    ↓
no broker implementation

ddd-messaging
    ↓
no specific broker dependency
```

---

# 107. Package Architecture Tests

必须检查：

```text
core cannot access domain
domain cannot access infrastructure
domain cannot access runtime
application cannot access adapter implementation
messaging cannot access broker SDK
```

---

# 108. Public API Audit

Release 前执行：

```text
Public API Scan
```

检查：

```text
Unexpected Public Class
Unexpected Public Method
Leaked Infrastructure Type
Leaked Third-party Type
```

---

# 109. Dependency Audit

Release 必须验证：

```text
ddd-core
```

依赖数量应保持极低。

目标：

```text
Framework Dependencies = 0
```

---

# 110. Runtime Dependency Policy

Core：

```text
JDK
```

Runtime 可以使用：

```text
JDK
SLF4J / logging abstraction
```

但 Logging API 是否进入 Core，需要以最终实现决定。

推荐：

```text
No Logging Dependency in ddd-core
```

---

# 111. Error Handling

Framework Exception：

```java
public class FrameworkException
        extends RuntimeException {

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}
```

子分类：

```text
ConfigurationException
HandlerNotFoundException
DuplicateHandlerException
ConcurrencyConflictException
TransactionException
MessagingException
RuntimeException
```

---

# 112. Exception Translation

Infrastructure：

```text
SQLException
KafkaException
JPA Exception
```

必须转换为：

```text
Framework / Application Exception
```

不得向 Domain 泄漏。

---

# 113. Logging Policy

Core：

```text
No Logging
```

Runtime：

```text
Lifecycle Logging
```

Infrastructure：

```text
Technical Failure Logging
```

Application：

```text
Business Operation Logging
```

敏感数据：

```text
Never log by default
```

---

# 114. Clock

```java
public interface ClockProvider {

    Instant now();
}
```

测试：

```java
FixedClockProvider
```

生产：

```java
SystemClockProvider
```

---

# 115. Identifier Generator

```java
public interface IdentifierGenerator {

    String generate();
}
```

实现：

```text
UUID
UUIDv7
ULID
Snowflake
```

均属于 Adapter。

---

# 116. Test Clock

测试中：

```java
var clock =
    new FixedClockProvider(
        Instant.parse("2026-01-01T00:00:00Z"));
```

保证：

```text
Deterministic Test
```

---

# 117. Configuration

Reference：

```java
public interface DddConfiguration {

    <T> T get(
            ConfigurationKey<T> key);
}
```

Configuration Key：

```java
public record ConfigurationKey<T>(
        String name,
        Class<T> type,
        T defaultValue
) {}
```

---

# 118. Runtime Configuration

至少支持：

```text
runtime.name
runtime.shutdown.timeout
command.timeout
query.timeout
transaction.timeout
messaging.retry.max-attempts
messaging.outbox.batch-size
```

---

# 119. Default Configuration

Framework 必须提供安全默认值。

例如：

```text
Retry = bounded
Logging = no sensitive payload
Transaction = REQUIRED for command
Query = read-only
Messaging = AT_LEAST_ONCE
```

---

# 120. Production Readiness

Reference Implementation 完成并不意味着：

```text
Production Ready
```

必须经过：

```text
Load Test
Failure Test
Concurrency Test
Long-running Test
Security Test
Compatibility Test
```

---

# 121. Benchmark

至少 Benchmark：

```text
Command dispatch
Query dispatch
Handler lookup
Middleware pipeline
Aggregate load
Aggregate save
Event dispatch
Outbox append
Inbox lookup
Message dispatch
```

---

# 122. Performance Metrics

记录：

```text
Throughput
p50
p95
p99
Allocation
GC
CPU
Memory
```

---

# 123. Framework Overhead

必须测量：

```text
Plain Java
vs
RegalPine DDD Runtime
```

目标：

> Framework 抽象不应产生不可接受的额外运行时成本。

---

# 124. Concurrency Benchmark

至少验证：

```text
10
100
1,000
10,000
```

并发 Command。

重点：

```text
Handler Registry
Repository
Transaction
UnitOfWork
Outbox
```

---

# 125. Failure Testing

必须模拟：

```text
Database unavailable
Broker unavailable
Consumer crash
Transaction rollback
Concurrency conflict
Outbox failure
Inbox conflict
Timeout
```

---

# 126. Recovery Testing

验证：

```text
Failure
 ↓
Retry
 ↓
Recovery
 ↓
Continue
```

---

# 127. Chaos Boundary

Chaos Test 不进入 Framework Core。

测试：

```text
Infrastructure
Messaging
Runtime
```

---

# 128. Release Pipeline

正式 Release：

```text
Compile
 ↓
Unit Test
 ↓
Architecture Test
 ↓
Conformance
 ↓
Integration Test
 ↓
Benchmark
 ↓
Security Scan
 ↓
Dependency Scan
 ↓
API Compatibility
 ↓
Package
 ↓
Release
```

---

# 129. Release Artifacts

发布：

```text
Maven Artifacts
Javadoc
Sources
Checksums
SBOM
Release Notes
Compatibility Report
Conformance Report
```

---

# 130. SBOM

正式版本必须生成：

```text
Software Bill of Materials
```

用于：

```text
Supply Chain Security
Vulnerability Management
License Governance
```

---

# 131. Security Baseline

必须检查：

```text
Dependency Vulnerability
Secret Leakage
Unsafe Serialization
Deserialization
SQL Injection
Message Injection
Tenant Isolation
Authorization
```

---

# 132. Serialization Security

禁止：

```text
Arbitrary Java Object Deserialization
```

推荐：

```text
Explicit Message Schema
```

例如：

```text
JSON
Protobuf
Avro
```

具体格式由 Adapter 决定。

---

# 133. Compatibility Test

每个 Release 必须验证：

```text
Old Application
+
New Framework
```

以及：

```text
New Application
+
Supported Adapter
```

---

# 134. Framework Version Compatibility

建立矩阵：

| Framework | Java | Spring Adapter | JDBC | Messaging |
|---|---|---|---|---|
| 1.x | 17 | Supported | Supported | Supported |

具体 Adapter 版本独立维护。

---

# 135. 1.0 API Freeze

在：

```text
1.0.0
```

发布前冻结：

```text
ddd-core
ddd-domain
ddd-application
ddd-cqrs
ddd-event
ddd-transaction
ddd-runtime
ddd-messaging
```

---

# 136. 1.0 后允许演进的内容

允许：

```text
Bug Fix
Performance
New Adapter
New Test Utility
New Optional Feature
```

谨慎：

```text
Core API Change
Transaction Semantics
Event Semantics
Repository Semantics
```

---

# 137. 1.0 后禁止的演进方式

禁止：

```text
为了某个具体数据库增加 Core API
为了某个 Broker 增加 Domain API
为了 Spring 增加 Domain Annotation
为了某个项目增加核心抽象
```

---

# 138. Architecture Decision Record

每次重大改变必须建立：

```text
ADR
```

至少记录：

```text
Context
Decision
Alternatives
Consequences
Compatibility
Migration
```

---

# 139. Change Governance

Change 类型：

```text
PATCH
MINOR
MAJOR
```

必须明确：

```text
Why
Scope
Impact
Migration
```

---

# 140. Core Model Freeze

当前核心模型正式定义为：

```text
Entity
ValueObject
Aggregate
DomainEvent
Repository
Specification
DomainService
Factory
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

除非出现无法表达现有语义的真实需求，否则不再新增 Core Concept。

---

# 141. Framework 1.0 Definition

RegalPine DDD Framework 1.0 的目标：

```text
A Java 17 developer framework
for implementing enterprise DDD applications
using
Four-Layer Architecture
+
Hexagonal Architecture
+
CQRS
+
Event-Driven Integration
```

---

# 142. 1.0 最小能力集

必须：

```text
✓ Entity
✓ Value Object
✓ Aggregate
✓ Domain Event
✓ Repository
✓ Command
✓ Query
✓ Handler
✓ CQRS
✓ Transaction
✓ UnitOfWork
✓ Runtime
✓ Outbox
✓ Inbox
✓ Integration Event
✓ Messaging SPI
✓ In-Memory Implementation
✓ Test Framework
✓ Conformance Kit
```

---

# 143. 1.0 非必需能力

不强制：

```text
Kafka
RabbitMQ
Pulsar
JPA
Hibernate
Spring Boot
Redis
Kubernetes
Cloud Provider
```

这些属于：

```text
Optional Adapter
```

---

# 144. Final Developer Experience

开发者编写：

```text
Domain
Application
Repository Contract
```

Framework 自动承担：

```text
Handler Routing
Transaction
UnitOfWork
Concurrency
Outbox
Messaging
Retry
Inbox
Runtime Lifecycle
Observability
```

---

# 145. 最终执行模型

```text
                 Developer Code
                       │
             ┌─────────▼─────────┐
             │    Application    │
             └─────────┬─────────┘
                       │
                  CommandBus
                       │
                  Middleware
                       │
                Transaction
                       │
                  UnitOfWork
                       │
                  Domain Model
                       │
             ┌─────────┴─────────┐
             │                   │
         Repository         Domain Event
             │                   │
             ▼                   ▼
        Persistence           Outbox
                                 │
                               Commit
                                 │
                             Messaging
                                 │
                              Broker
                                 │
                              Inbox
                                 │
                           Application
```

---

# 146. Phase XI 完成矩阵

| Capability | Status |
|---|---|
| Maven Reactor | Complete |
| Java 17 Baseline | Complete |
| Core API | Complete |
| Domain API | Complete |
| Application API | Complete |
| CQRS API | Complete |
| Event API | Complete |
| Transaction API | Complete |
| Runtime API | Complete |
| Messaging API | Complete |
| In-Memory Adapter | Complete |
| Test Framework | Complete |
| Conformance Framework | Complete |
| Reference Application | Complete |
| Configuration | Complete |
| Error Model | Complete |
| Lifecycle | Complete |
| API Stability | Complete |
| Version Policy | Complete |
| Architecture Test | Complete |
| Release Pipeline | Complete |

---

# 147. Phase XI 结论

Phase XI 完成后，RegalPine DDD Framework 已经具备从规范进入实际工程实现的完整基础：

```text
Architecture
      ↓
Contracts
      ↓
SPI
      ↓
Runtime
      ↓
Reference Implementation
      ↓
Test
      ↓
Conformance
      ↓
Release
```

这意味着项目已经不应该继续通过增加新的：

```text
Phase XII
Phase XIII
Phase XIV
...
```

来无限扩张核心架构。

下一步应当进入一个**横向实施阶段**：

```text
Implementation
Testing
Benchmark
Security
Documentation
Reference Project
```

而不是继续创造新的 DDD 理论层。

---

# 148. 1.0 收敛门槛

正式进入 `1.0.0` 前必须满足：

```text
[ ] 所有 Core API 已实现
[ ] Domain API 已实现
[ ] Application API 已实现
[ ] CQRS 已实现
[ ] Transaction / UoW 已实现
[ ] Runtime 已实现
[ ] Messaging SPI 已实现
[ ] In-Memory Adapter 已实现
[ ] Test Framework 已实现
[ ] Conformance 已实现
[ ] Reference Application 已运行
[ ] Architecture Test 已通过
[ ] Concurrency Test 已通过
[ ] Failure Test 已通过
[ ] Security Test 已通过
[ ] Benchmark 已完成
[ ] API Compatibility 已验证
[ ] SBOM 已生成
[ ] Documentation 已完成
```

---

# 149. 下一工作阶段

下一阶段不再定义新的架构层。

推荐正式进入：

## Implementation Track A — `ddd-core`

首先实现：

```text
io.github.regalpine.ddd.core.identifier
io.github.regalpine.ddd.core.entity
io.github.regalpine.ddd.core.value
io.github.regalpine.ddd.core.aggregate
io.github.regalpine.ddd.core.event
io.github.regalpine.ddd.core.specification
io.github.regalpine.ddd.core.version
io.github.regalpine.ddd.core.exception
```

然后依次：

```text
Track B
ddd-domain

Track C
ddd-application

Track D
ddd-cqrs

Track E
ddd-event

Track F
ddd-transaction

Track G
ddd-runtime

Track H
ddd-messaging

Track I
ddd-infrastructure

Track J
ddd-test / ddd-conformance

Track K
Reference Application

Track L
Benchmark / Security / Release
```

**最终原则：**

```text
Architecture = Frozen
API = Implement
SPI = Implement
Adapters = Implement
Tests = Verify
Benchmark = Measure
Conformance = Certify
```

RegalPine DDD Framework 从这一节点开始，正式从“架构设计项目”转变为“可交付 Java Framework 项目”。