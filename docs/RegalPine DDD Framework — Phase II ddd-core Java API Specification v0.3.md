# RegalPine DDD Framework
## Phase II — ddd-core Java API Specification

**GroupId:** `io.github.regalpine.ddd`  
**Artifact:** `ddd-core`  
**Version:** `0.3`  
**Java:** 17+  
**Build:** Maven  
**Status:** Core API Baseline  
**Architecture:** DDD + Four-Layer + Hexagonal + CQRS  
**DSL:** None  

---

# 1. 文档定位

`ddd-core` 是 RegalPine DDD Framework 的最底层模块。

它承载最稳定的 DDD 基础语义：

```text
Identifier
Entity
ValueObject
AggregateRoot
DomainEvent
DomainError
DomainException
Specification
Version
```

`ddd-core` 必须：

- 不依赖 Spring
- 不依赖 Jakarta
- 不依赖 ORM
- 不依赖数据库
- 不依赖消息中间件
- 不依赖 Web
- 不依赖 JSON
- 不依赖反射框架
- 不依赖任何具体基础设施

目标：

> `ddd-core` 在纯 Java 17 环境中即可使用。

---

# 2. Maven Coordinates

```xml
<dependency>
    <groupId>io.github.regalpine.ddd</groupId>
    <artifactId>ddd-core</artifactId>
    <version>${ddd.version}</version>
</dependency>
```

---

# 3. Core Package

推荐包结构：

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

# 4. Core API Design Principles

## 4.1 Interface First

优先使用：

```java
interface
```

表达 Framework Contract。

---

## 4.2 Record First for Immutable Data

Java 17 中：

```java
record
```

优先用于：

```text
Value Object
Identifier
Error
Event Metadata
```

---

## 4.3 Sealed Interface

仅当类型层次需要严格控制时使用：

```java
sealed interface
```

不为了“先进”而滥用。

---

# 5. Identifier

Identifier 是 DDD Framework 的统一身份抽象。

```java
public interface Identifier {

    String value();
}
```

---

# 6. Identifier Requirements

Identifier 必须：

- 不可变
- 可比较
- 可序列化
- 不依赖数据库
- 不依赖 ORM

推荐：

```java
public record OrderId(String value)
        implements Identifier {

    public OrderId {
        Objects.requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException(
                "Identifier value must not be blank"
            );
        }
    }
}
```

---

# 7. Identifier Equality

Identifier 使用值相等：

```text
Identifier A
=
Identifier B
```

当且仅当：

```text
A.value() == B.value()
```

并且：

```text
A.type == B.type
```

例如：

```text
OrderId("001")
≠
CustomerId("001")
```

---

# 8. Typed Identifier

框架推荐 Typed Identifier。

```java
public record CustomerId(String value)
        implements Identifier {
}
```

而不是：

```java
String customerId
```

原因：

避免：

```java
pay(
    orderId,
    customerId,
    accountId
)
```

出现类型混淆。

---

# 9. Identifier Factory

Core 不强制具体 ID 算法。

提供：

```java
public interface IdentifierGenerator<I extends Identifier> {

    I generate();
}
```

具体实现可以位于：

```text
ddd-infrastructure
```

例如：

```text
UUID
ULID
Snowflake
```

---

# 10. Entity

正式 API：

```java
public interface Entity<I extends Identifier> {

    I id();
}
```

---

# 11. Entity Equality

Entity 不要求：

```java
equals()
```

必须使用 Framework 默认实现。

但是语义必须遵守：

```text
same type
+
same identity
=
same entity
```

推荐领域实体自行实现 value-safe equality。

---

# 12. Entity Identity Constraint

Entity：

```text
id != null
```

并且：

```text
identity immutable
```

原则：

> Entity 可以改变业务状态，但不能在生命周期中改变身份。

禁止：

```java
order.setId(otherId);
```

---

# 13. ValueObject

最小接口：

```java
public interface ValueObject {
}
```

Value Object 本身不提供 identity API。

---

# 14. Value Object Contract

Value Object 必须：

```text
Immutable
+
Value Equality
+
Side Effect Free
```

推荐 Java：

```java
public record EmailAddress(
    String value
) implements ValueObject {

    public EmailAddress {
        Objects.requireNonNull(value);
    }
}
```

---

# 15. Value Object 不强制 equals

Java `record` 已经天然提供：

```text
value-based equality
```

因此 Framework 不需要重新定义：

```java
ValueObject.equals()
```

---

# 16. AggregateRoot

正式 API：

```java
public interface AggregateRoot<
        I extends Identifier>
    extends Entity<I> {

    long version();
}
```

---

# 17. AggregateRoot Semantics

AggregateRoot：

```text
Entity
+
Consistency Boundary
+
Event Boundary
```

因此：

```text
AggregateRoot ⊂ Entity
```

---

# 18. Version

版本用于 Optimistic Concurrency。

```java
public record Version(long value) {

    public Version {
        if (value < 0) {
            throw new IllegalArgumentException(
                "Version must be >= 0"
            );
        }
    }

    public Version next() {
        return new Version(value + 1);
    }
}
```

---

# 19. AggregateRoot Version API

这里有两种设计：

### Option A

```java
long version();
```

### Option B

```java
Version version();
```

Framework 正式采用：

```java
Version version();
```

原因：

- 防止裸 `long` 语义污染
- 后续可扩展版本语义
- 与其他 Numeric Primitive 解耦

最终：

```java
public interface AggregateRoot<
        I extends Identifier>
    extends Entity<I> {

    Version version();
}
```

---

# 20. Aggregate

Aggregate 本身不需要成为一个强制 Java Runtime Object。

这是一个重要设计决定。

Aggregate 是：

```text
Architectural / Domain Boundary
```

而 AggregateRoot 是：

```text
Runtime Object
```

因此：

```text
Aggregate ≠ Aggregate class
```

---

# 21. Aggregate Representation

例如：

```java
public final class Order
        implements AggregateRoot<OrderId> {

    private final OrderId id;

    private Version version;

    private OrderStatus status;

    @Override
    public OrderId id() {
        return id;
    }

    @Override
    public Version version() {
        return version;
    }
}
```

`Order` 即 Aggregate Root，同时代表 Aggregate 的 Runtime Model。

---

# 22. DomainEvent

正式 API：

```java
public interface DomainEvent {

    EventId eventId();

    Instant occurredAt();

    AggregateType aggregateType();

    Identifier aggregateId();

    Version aggregateVersion();
}
```

---

# 23. EventId

```java
public record EventId(String value)
        implements Identifier {
}
```

EventId 与 AggregateId 必须类型分离。

---

# 24. AggregateType

```java
public record AggregateType(
    String value
) {
}
```

例如：

```text
Order
Customer
Payment
```

---

# 25. DomainEvent Example

```java
public record OrderPaid(
    EventId eventId,
    Instant occurredAt,
    AggregateType aggregateType,
    OrderId aggregateId,
    Version aggregateVersion,
    PaymentId paymentId
) implements DomainEvent {
}
```

---

# 26. Event Immutability

Domain Event 必须：

```text
Immutable
```

推荐：

```java
record
```

禁止：

```java
class MutableDomainEvent
```

---

# 27. Event Temporal Semantics

DomainEvent：

```text
occurredAt
```

表示：

> 业务事实发生的时间。

不是：

```text
publishTime
```

也不是：

```text
persistTime
```

---

# 28. DomainEvent Metadata

不要让 Core Event 无限扩张。

基础 Event 只要求：

```text
eventId
occurredAt
aggregateType
aggregateId
aggregateVersion
```

以下内容属于 Application/Event Infrastructure：

```text
correlationId
causationId
tenantId
traceId
principalId
```

---

# 29. DomainError

正式 API：

```java
public interface DomainError {

    String code();

    String message();
}
```

---

# 30. DomainError Code

Code 必须：

- 稳定
- 可机器识别
- 不包含技术异常
- 不直接使用 HTTP Status

例如：

```text
ORDER_NOT_FOUND
ORDER_ALREADY_PAID
ORDER_CANNOT_CANCEL
INSUFFICIENT_BALANCE
```

---

# 31. DomainException

```java
public class DomainException
        extends RuntimeException {

    private final DomainError error;

    public DomainException(
        DomainError error
    ) {
        super(error.message());
        this.error = error;
    }

    public DomainError error() {
        return error;
    }
}
```

---

# 32. DomainException 原则

DomainException：

```text
Domain Layer
```

可以抛出：

```java
throw new DomainException(
    OrderErrors.alreadyPaid()
);
```

但是不得直接产生：

```text
HTTP 400
HTTP 404
HTTP 409
```

---

# 33. Domain Error Example

```java
public enum OrderError
        implements DomainError {

    ALREADY_PAID(
        "ORDER_ALREADY_PAID",
        "Order has already been paid"
    );

    private final String code;
    private final String message;

    OrderError(
        String code,
        String message
    ) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
```

---

# 34. Specification

正式 API：

```java
@FunctionalInterface
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);
}
```

---

# 35. Specification Composition

Framework 提供默认组合：

```java
default Specification<T> and(
    Specification<T> other
) {
    return candidate ->
        isSatisfiedBy(candidate)
        && other.isSatisfiedBy(candidate);
}
```

---

# 36. OR

```java
default Specification<T> or(
    Specification<T> other
) {
    return candidate ->
        isSatisfiedBy(candidate)
        || other.isSatisfiedBy(candidate);
}
```

---

# 37. NOT

```java
default Specification<T> not() {
    return candidate ->
        !isSatisfiedBy(candidate);
}
```

---

# 38. Specification Null Policy

默认：

```text
null candidate
=
false
```

或者抛异常？

正式采用：

> Specification 不负责 Null Validation。

因此：

```java
isSatisfiedBy(null)
```

是否允许由具体 Specification 定义。

推荐：

```java
Objects.requireNonNull(candidate);
```

---

# 39. Domain Invariant

Invariant 不单独作为必须继承的 Java Interface。

原因：

Invariant 本质上是：

```text
Business Constraint
```

不是 Runtime Service。

例如：

```java
private void validateCanPay() {
    if (status != CREATED) {
        throw new DomainException(...);
    }
}
```

---

# 40. Aggregate Behavior

Aggregate 必须通过行为改变状态：

```java
public void pay(Payment payment) {
    validateCanPay();

    this.status = PAID;

    raise(new OrderPaid(...));
}
```

而不是暴露：

```java
setStatus(...)
```

---

# 41. Domain Event Collection

Core 是否直接支持：

```java
raise(event)
```

这是一个关键设计点。

正式采用：

```text
AggregateRoot
+
DomainEventSource
```

---

# 42. DomainEventSource

```java
public interface DomainEventSource {

    List<DomainEvent> domainEvents();

    void clearDomainEvents();
}
```

AggregateRoot：

```java
public interface AggregateRoot<
        I extends Identifier>
    extends Entity<I>,
            DomainEventSource {

    Version version();
}
```

---

# 43. Event Collection Semantics

Aggregate：

```text
raise(Event)
```

之后：

```text
domainEvents()
```

可以读取事件。

但事件不能在 Aggregate 内直接：

```text
publish
```

---

# 44. Event Collection Lifecycle

```text
Aggregate
   ↓
raise(event)
   ↓
Pending Events
   ↓
UnitOfWork
   ↓
Commit
   ↓
Publication
   ↓
clear
```

---

# 45. Event Ordering

同一 Aggregate 的 Pending Events：

```text
List<DomainEvent>
```

而不是：

```text
Set<DomainEvent>
```

因为事件具有顺序语义。

---

# 46. Event List Mutability

`domainEvents()` 不得允许外部修改内部集合。

推荐：

```java
return List.copyOf(events);
```

---

# 47. DomainEventSource API

完整建议：

```java
public interface DomainEventSource {

    List<DomainEvent> domainEvents();

    void clearDomainEvents();
}
```

不直接暴露：

```java
addEvent()
```

给外部调用者。

---

# 48. Event Registration

Aggregate 内部：

```java
protected final void raise(
    DomainEvent event
) {
    events.add(event);
}
```

这里不要求 Core 提供 AbstractAggregateRoot。

但是可以提供可选基类：

```text
ddd-core
    AggregateRootSupport
```

---

# 49. AggregateRootSupport

可选：

```java
public abstract class AggregateRootSupport<
        I extends Identifier>
    implements AggregateRoot<I> {

    private final List<DomainEvent> events =
        new ArrayList<>();

    protected final void raise(
        DomainEvent event
    ) {
        events.add(
            Objects.requireNonNull(event)
        );
    }

    @Override
    public final List<DomainEvent> domainEvents() {
        return List.copyOf(events);
    }

    @Override
    public final void clearDomainEvents() {
        events.clear();
    }
}
```

---

# 50. Interface vs Base Class

正式设计：

```text
Interface = Contract
Base Class = Convenience
```

因此：

```text
AggregateRoot
```

是核心 Interface。

而：

```text
AggregateRootSupport
```

是可选实现。

---

# 51. DomainEvent Validation

Framework Core 应验证：

```text
eventId != null
occurredAt != null
aggregateType != null
aggregateId != null
aggregateVersion != null
```

具体 Event Payload 验证属于 Domain。

---

# 52. DomainEvent Constructor Policy

Framework 不强制所有 Event 使用 Base Class。

推荐：

```java
record
```

例如：

```java
public record OrderCreated(
    EventId eventId,
    Instant occurredAt,
    AggregateType aggregateType,
    OrderId aggregateId,
    Version aggregateVersion
) implements DomainEvent {
}
```

---

# 53. Core Clock

Core 不提供：

```java
SystemClock
```

只定义抽象：

```java
public interface DomainClock {

    Instant now();
}
```

但这一接口是否应该进入 Core？

正式决定：

> `Clock` 属于 `ddd-port`，不属于 `ddd-core`。

原因：

时间获取属于外部依赖。

Domain 可以在构造业务对象时接收：

```text
Instant
```

而不是偷偷读取系统时间。

---

# 54. Core ID Generation

同样：

```text
IdGenerator
```

不属于 `ddd-core`。

原因：

生成 ID 是 Infrastructure Capability。

Core 只定义：

```text
Identifier
```

---

# 55. Core 不包含 Repository

Repository 不进入 `ddd-core`。

原因：

Repository 是：

```text
Domain Port
```

因此进入：

```text
ddd-port
```

或者：

```text
ddd-domain
```

最终建议：

```text
ddd-domain
```

定义领域 Repository Contract。

---

# 56. Core 不包含 Command

Command 属于：

```text
Application Layer
```

因此进入：

```text
ddd-application
```

---

# 57. Core Dependency Graph

最终：

```text
ddd-core
   │
   ├── Java 17
   │
   └── NO framework dependency
```

---

# 58. ddd-core API

核心 API 最终控制为：

```text
io.github.regalpine.ddd.core
│
├── identifier
│   ├── Identifier
│   └── EventId
│
├── entity
│   └── Entity
│
├── aggregate
│   ├── AggregateRoot
│   └── AggregateRootSupport
│
├── value
│   └── ValueObject
│
├── event
│   ├── DomainEvent
│   └── AggregateType
│
├── specification
│   └── Specification
│
├── version
│   └── Version
│
├── error
│   └── DomainError
│
└── exception
    └── DomainException
```

---

# 59. Core API Count

稳定核心接口：

```text
Identifier
Entity
ValueObject
AggregateRoot
DomainEventSource
DomainEvent
Specification
DomainError
```

稳定核心类型：

```text
Version
EventId
AggregateType
DomainException
AggregateRootSupport
```

控制在：

```text
8 interfaces
+
6 classes/records
```

避免 Framework Core 膨胀。

---

# 60. Domain Model Example

```java
public final class Order
        extends AggregateRootSupport<OrderId> {

    private final OrderId id;

    private Version version;

    private OrderStatus status;

    public Order(
        OrderId id,
        Version version
    ) {
        this.id = Objects.requireNonNull(id);
        this.version =
            Objects.requireNonNull(version);
        this.status = OrderStatus.CREATED;
    }

    @Override
    public OrderId id() {
        return id;
    }

    @Override
    public Version version() {
        return version;
    }

    public void pay(
        PaymentId paymentId,
        Instant occurredAt
    ) {

        if (status != OrderStatus.CREATED) {
            throw new DomainException(
                OrderError.ALREADY_PAID
            );
        }

        status = OrderStatus.PAID;

        raise(
            new OrderPaid(
                new EventId(UUID.randomUUID().toString()),
                occurredAt,
                new AggregateType("Order"),
                id,
                version,
                paymentId
            )
        );
    }
}
```

---

# 61. UUID 使用边界

上面的示例可以使用 UUID，但：

> `ddd-core` 不负责 ID 生成。

生产代码推荐由：

```text
IdGenerator
```

注入。

示例只是说明 Domain Event 的结构。

---

# 62. Aggregate Construction

Aggregate 可以通过：

```text
Factory Method
Constructor
Domain Factory
```

创建。

推荐：

```java
Order.create(...)
```

而不是：

```java
new Order(...)
```

直接暴露给 Application Layer。

---

# 63. Factory

Domain Factory 不进入 Core。

如果复杂 Factory：

```text
ddd-domain
```

负责。

---

# 64. Immutability Rules

Core 类型：

```text
Identifier
Version
EventId
AggregateType
DomainError
```

必须 immutable。

推荐：

```java
record
```

---

# 65. Nullability

Framework 不依赖：

```text
JSR-305
Jakarta Validation
Spring Nullable
```

Core API：

```text
Non-null by contract
```

如果需要表达可选值：

```java
Optional<T>
```

仅在返回值中使用。

---

# 66. Optional Policy

推荐：

```java
Optional<Order> find(...)
```

不推荐：

```java
return null;
```

但 `Optional` 不用于：

```text
Entity fields
DTO fields
Command fields
```

---

# 67. Collection Policy

Core API：

```text
read-only view
```

推荐：

```java
List.copyOf(...)
Set.copyOf(...)
Map.copyOf(...)
```

避免：

```java
Collections.mutable...
```

暴露内部状态。

---

# 68. Thread Safety

Core 不要求所有 Domain Object：

```text
thread-safe
```

原因：

Aggregate 通常属于：

```text
single transaction context
```

但 Framework Core Value Object 必须 immutable。

---

# 69. Serialization

Core 不强制：

```text
Serializable
Jackson
JSON-B
ProtoBuf
Avro
```

Domain Event 序列化由 Event Adapter 负责。

---

# 70. Persistence

Core 不包含：

```text
JPA annotations
Hibernate
JdbcTemplate
MyBatis
Mongo
```

---

# 71. Framework API Boundary

最终边界：

```text
              ddd-core
                 │
       Pure Domain Primitives
                 │
        ┌────────┴────────┐
        ▼                 ▼
   ddd-domain       ddd-application
        │                 │
        └────────┬────────┘
                 ▼
             ddd-port
                 │
                 ▼
            Infrastructure
```

---

# 72. Core API Compatibility

Java 17 是最低版本。

禁止使用：

```text
Preview Features
```

核心 API 只使用：

```text
Java 17 LTS
```

正式语言特性：

```text
record
sealed interface
pattern matching for instanceof
```

其中只有在确实改善 API 时使用。

---

# 73. Serialization Compatibility

Domain Event 的 API 演进不得依赖 Java serialization。

事件兼容性由：

```text
ddd-event
```

负责。

---

# 74. API Binary Compatibility

1.x：

```text
Backward Compatible
```

核心 Interface 不应随意：

```text
add abstract method
```

因为：

```text
implements AggregateRoot
```

的业务类会直接受到影响。

---

# 75. Default Method Strategy

对于未来扩展：

```java
default
```

优先于：

```java
abstract method
```

但不能滥用。

---

# 76. Sealed Type Policy

目前不对：

```text
Entity
ValueObject
DomainEvent
```

使用 sealed。

原因：

业务项目需要自由扩展。

---

# 77. DomainEvent Type Discovery

不要依赖：

```text
sealed DomainEvent
```

来发现事件。

使用：

```text
Java Class
```

作为默认事件类型。

Infrastructure 再定义：

```text
eventType
```

序列化名称。

---

# 78. AggregateType Policy

`AggregateType` 是领域事件元数据。

它不是：

```text
Java Class Name
```

必须允许：

```text
Order
```

而不是强制：

```text
com.example.order.Order
```

避免将内部代码结构暴露到 Integration Event。

---

# 79. Version Semantics

`Version(0)`：

```text
New Aggregate
```

第一次持久化后：

```text
Version(1)
```

或者：

```text
初始版本由 Persistence Adapter 决定
```

Framework 正式规定：

> `Version` 的数值语义由 Repository/Concurrency Port 解释；Core 只保证它是单调的非负版本值。

---

# 80. Version Monotonicity

同一个 Aggregate：

```text
V1 < V2 < V3
```

禁止：

```text
V3 → V2
```

正常更新流程。

---

# 81. DomainEvent Version

DomainEvent：

```text
aggregateVersion
```

表示：

> 产生该事件时 Aggregate 所处的业务版本。

不是：

```text
Event Version
```

二者概念分离。

---

# 82. Event Version

事件 Schema Version 不进入 Core DomainEvent。

例如：

```text
OrderPaid
schemaVersion = 2
```

属于：

```text
ddd-event
```

而非：

```text
ddd-core
```

---

# 83. Core Architecture Rule

`ddd-core` 不应该知道：

```text
CQRS
Repository
Transaction
Outbox
Saga
Message Broker
```

这些都在 Core 之上。

---

# 84. Core Purity Test

`ddd-core` 必须能够运行：

```bash
java
```

而无需：

```text
Spring
Database
Network
Container
```

---

# 85. Unit Testing

核心测试：

```text
IdentifierTest
VersionTest
SpecificationTest
DomainExceptionTest
AggregateRootSupportTest
DomainEventTest
```

全部：

```text
Pure Unit Test
```

---

# 86. Architecture Testing

必须保证：

```text
ddd-core
does not depend on
ddd-domain
ddd-application
ddd-port
ddd-infrastructure
```

这是 Maven Dependency Rule。

---

# 87. Maven Dependency

```xml
<dependency>
    <groupId>io.github.regalpine.ddd</groupId>
    <artifactId>ddd-core</artifactId>
</dependency>
```

Core 不允许出现：

```xml
<dependency>
    <groupId>org.springframework</groupId>
</dependency>
```

---

# 88. Package Visibility

推荐 Domain 内部实现使用：

```java
package-private
```

例如：

```text
domain.order
```

只暴露：

```text
Order
OrderId
OrderRepository
```

隐藏：

```text
OrderState
OrderCalculator
OrderValidator
```

---

# 89. Public API Principle

Framework 与业务项目均应遵循：

> Public API 越少越稳定。

因此：

```text
public
```

不是默认选择。

---

# 90. API Evolution

Core 后续扩展必须满足：

```text
New Concept
    ↓
Can existing concept express it?
    ↓
Yes → Don't add API
No  → Evaluate Extension
```

避免：

```text
每一个 DDD 模式
→
新增一个 Interface
```

---

# 91. Anti-Overengineering Rule

Core 不实现：

```text
GenericAggregateManager
GenericDomainEngine
UniversalRuleEngine
GenericEntityFactory
UniversalRepository
DomainObjectContainer
```

这些会导致 Framework 从：

```text
Foundation
```

演变成：

```text
Application Framework Monolith
```

---

# 92. Phase II Final Core Model

最终：

```text
                   ddd-core
                      │
       ┌──────────────┼──────────────┐
       ▼              ▼              ▼
   Identity        Domain          Events
       │           Objects            │
       │              │               │
       ▼        ┌─────┼─────┐         ▼
   Identifier  Entity   VO  Aggregate  Event
                   │
                   ▼
             AggregateRoot
```

辅助：

```text
Version
Specification
DomainError
DomainException
```

---

# 93. Phase II Exit Criteria

- [x] Identifier
- [x] Typed Identifier
- [x] Entity
- [x] ValueObject
- [x] AggregateRoot
- [x] AggregateRootSupport
- [x] DomainEvent
- [x] DomainEventSource
- [x] EventId
- [x] AggregateType
- [x] Version
- [x] Specification
- [x] DomainError
- [x] DomainException
- [x] Immutable core model
- [x] Nullability policy
- [x] Collection policy
- [x] Java 17 compatibility
- [x] Dependency isolation
- [x] API stability policy

---

# 94. Phase III

下一阶段进入：

# Maven Multi-Module Architecture & ddd-domain API Specification

正式定义：

```text
ddd-parent
│
├── ddd-core
│
├── ddd-domain
│
├── ddd-application
│
├── ddd-port
│
├── ddd-cqrs
│
├── ddd-event
│
├── ddd-transaction
│
├── ddd-infrastructure
│
├── ddd-test
│
└── ddd-spring-boot
```

重点确定：

1. Maven 精确依赖 DAG
2. `ddd-domain` API
3. Repository Contract
4. Domain Service Contract
5. Domain Policy
6. Domain Factory
7. Domain Specification
8. Aggregate 生命周期
9. Domain Event 生命周期
10. Domain ↔ Port 边界

并开始定义第一个真正可以用于业务项目的完整代码骨架。

---

# 95. Phase II Architecture Decision

本阶段最终冻结以下原则：

```text
ddd-core
=
Pure DDD Semantic Kernel
```

而不是：

```text
ddd-core
=
All DDD Features
```

因此后续：

```text
Repository
→ ddd-domain

Command / Query
→ ddd-application

CommandBus / QueryBus
→ ddd-cqrs

Event Dispatch / Outbox
→ ddd-event

Transaction / UnitOfWork
→ ddd-transaction

Technology Integration
→ ddd-infrastructure
```

这是整个 `io.github.regalpine.ddd` 能否长期收敛的关键架构决策。