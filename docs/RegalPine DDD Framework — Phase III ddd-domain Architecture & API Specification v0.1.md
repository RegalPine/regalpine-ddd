# RegalPine DDD Framework
## Phase III — `ddd-domain` Architecture & API Specification v0.1

**GroupId:** `io.github.regalpine.ddd`  
**Language:** Java 17+  
**Build:** Maven  
**Architecture:** DDD + Four-Layer Architecture + Hexagonal Architecture + CQRS  
**Nature:** Developer Framework  
**Status:** Architecture Baseline  
**Module:** `ddd-domain`

---

# 1. 本阶段目标

Phase II 已经定义：

> `ddd-core = Pure DDD Semantic Kernel`

本阶段进一步定义：

> `ddd-domain = Domain Modeling & Domain Rules Layer`

`ddd-domain` 负责把纯 DDD 基础语义组合成可实际开发的领域模型。

本阶段解决：

1. Maven 多模块依赖关系；
2. Domain Model 的正式边界；
3. Aggregate Repository Contract；
4. Domain Service；
5. Domain Policy；
6. Domain Factory；
7. Domain Specification；
8. Domain Rule；
9. Aggregate 生命周期；
10. Domain Event 生命周期；
11. Domain 与 Port 的边界；
12. Domain 与 Application 的边界；
13. Domain 与 Infrastructure 的隔离；
14. 为后续 CQRS 提供稳定基础。

---

# 2. 总体架构定位

框架采用：

```text
DDD
+
Classic Four-Layer Architecture
+
Hexagonal Architecture
+
CQRS
```

四层逻辑结构：

```text
┌──────────────────────────────────────────────┐
│ Interface / Adapter Layer                   │
│ REST / RPC / Messaging / CLI / Scheduler    │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│ Application Layer                            │
│ Command / Query / Handler / Use Case         │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│ Domain Layer                                 │
│ Aggregate / Entity / VO / Policy / Service   │
│ Repository Contract / Domain Event           │
└───────────────────────┬──────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────┐
│ Infrastructure Layer                         │
│ DB / ORM / MQ / HTTP / Cache / IAM / Files   │
└──────────────────────────────────────────────┘
```

但实际依赖必须遵循：

```text
Infrastructure
      │
      ▼
Application ─────► Domain ─────► Core
      │
      ▼
   Port / SPI
```

而不是：

```text
Domain ───► Infrastructure   ❌
Domain ───► Spring            ❌
Domain ───► JPA               ❌
Domain ───► REST              ❌
Domain ───► MQ                ❌
```

---

# 3. Maven Module Architecture

建议正式模块：

```text
io.github.regalpine.ddd
│
├── ddd-bom
│
├── ddd-core
│
├── ddd-domain
│
├── ddd-application
│
├── ddd-cqrs
│
├── ddd-event
│
├── ddd-transaction
│
├── ddd-port
│
├── ddd-infrastructure
│
├── ddd-test
│
└── ddd-spring-boot
```

其中本阶段重点：

```text
ddd-core
   │
   ▼
ddd-domain
```

---

# 4. 模块职责

| Module | 职责 |
|---|---|
| `ddd-core` | DDD 最基础语义 |
| `ddd-domain` | 领域模型与领域规则 |
| `ddd-application` | 用例、Command、Query、Handler |
| `ddd-cqrs` | CommandBus / QueryBus |
| `ddd-event` | Domain Event Dispatch / Integration Event / Outbox |
| `ddd-transaction` | Transaction / Unit of Work |
| `ddd-port` | 外部系统 Port / SPI |
| `ddd-infrastructure` | 技术实现 |
| `ddd-test` | 测试工具 |
| `ddd-spring-boot` | Spring Boot 集成 |

---

# 5. 严格依赖 DAG

正式依赖图：

```text
                    ddd-core
                       ▲
                       │
                 ddd-domain
                  ▲    ▲
                  │    │
          ┌───────┘    └────────┐
          │                     │
   ddd-application          ddd-port
          ▲                     ▲
          │                     │
       ddd-cqrs          ddd-infrastructure
          │                     │
          └──────────┬──────────┘
                     │
              ddd-spring-boot
```

辅助模块：

```text
ddd-event
ddd-transaction
ddd-test
ddd-bom
```

必须遵循以下原则：

```text
Core → Domain → Application
```

不能反向。

---

# 6. Dependency Rules

## 6.1 Core

`ddd-core`：

```text
NO framework dependency
NO database dependency
NO Spring dependency
NO Jakarta dependency
NO ORM dependency
NO messaging dependency
```

---

## 6.2 Domain

`ddd-domain`：

```text
ALLOW:
    ddd-core

FORBIDDEN:
    Spring
    Spring Boot
    JPA
    Hibernate
    JDBC
    Redis
    Kafka
    RabbitMQ
    REST
    HTTP Client
```

---

## 6.3 Application

`ddd-application`：

```text
ALLOW:
    ddd-domain
    ddd-core
    ddd-port
```

Application 可以依赖 Port，但 Domain 不应依赖 Application。

---

# 7. Domain Layer Formal Model

领域层的核心本体：

```text
Domain
│
├── Aggregate
│   ├── AggregateRoot
│   ├── Entity
│   └── ValueObject
│
├── DomainService
│
├── DomainPolicy
│
├── DomainFactory
│
├── Specification
│
├── DomainRule
│
├── Repository
│
└── DomainEvent
```

核心关系：

```text
Domain
 └── contains ──► Aggregate

Aggregate
 ├── hasRoot ────► AggregateRoot
 ├── contains ───► Entity
 ├── contains ───► ValueObject
 └── produces ───► DomainEvent

Repository
 └── manages ────► Aggregate

DomainService
 └── operatesOn ─► DomainConcept

DomainPolicy
 └── evaluates ──► BusinessCondition

Specification
 └── evaluates ──► DomainObject
```

---

# 8. Aggregate Boundary

Aggregate 是 Domain Model 中最重要的事务一致性边界。

定义：

```text
Aggregate =
    AggregateRoot
    +
    Internal Entities
    +
    Value Objects
    +
    Invariants
    +
    Domain Events
```

Aggregate 的核心职责：

```text
维护身份
维护状态
维护不变量
执行领域行为
产生领域事件
```

Aggregate 不负责：

```text
持久化
事务管理
消息发布
HTTP 调用
数据库访问
日志基础设施
缓存
```

---

# 9. AggregateRoot Contract

Phase II 已定义：

```java
public interface AggregateRoot<I extends Identifier>
        extends Entity<I>, DomainEventSource {
}
```

因此：

```text
Entity
   ▲
   │
AggregateRoot
```

AggregateRoot 必须具备：

```text
Identity
+
Behavior
+
Invariant
+
Domain Event Source
```

---

# 10. Aggregate 行为原则

禁止：

```java
order.setStatus("PAID");
```

推荐：

```java
order.pay(paymentId);
```

禁止：

```java
account.setBalance(
    account.getBalance().add(amount)
);
```

推荐：

```java
account.deposit(amount);
```

领域对象必须：

> 优先暴露业务行为，而不是暴露状态修改器。

---

# 11. Aggregate Invariant

Invariant 是 Aggregate 必须持续满足的业务不变量。

例如：

```text
Order.totalAmount >= 0

Order:
    CREATED → PAID
    PAID → SHIPPED
    SHIPPED → COMPLETED
```

禁止：

```text
CREATED → COMPLETED
```

Invariant 应由 Aggregate 自身维护。

---

# 12. Repository Contract

Repository 是 Domain Layer 的持久化抽象。

正式定义：

```java
public interface Repository<
        A extends AggregateRoot<I>,
        I extends Identifier> {
}
```

但不建议让 Repository 变成万能 CRUD API。

---

# 13. AggregateRepository

推荐正式 API：

```java
public interface AggregateRepository<
        A extends AggregateRoot<I>,
        I extends Identifier> {

    Optional<A> findById(I id);

    A save(A aggregate);

    void delete(A aggregate);
}
```

---

# 14. Repository 语义

## findById

```java
Optional<A> findById(I id);
```

语义：

```text
读取 Aggregate 完整一致状态
```

而不是：

```text
查询任意数据库记录
```

---

# 15. save

```java
A save(A aggregate);
```

语义：

```text
保存 Aggregate 当前完整状态
```

Repository 不负责：

```text
publish event
send message
execute command
start transaction
```

---

# 16. delete

```java
void delete(A aggregate);
```

删除必须是：

```text
Application Use Case
        │
        ▼
Aggregate Behavior
        │
        ▼
Repository.delete()
```

而不是：

```text
REST DELETE
    │
    ▼
Repository.deleteById()
```

直接删除。

---

# 17. 禁止 Generic CRUD Repository

不提供：

```java
CrudRepository
JpaRepository
BaseRepository
GenericRepository
```

这种框架级抽象。

原因：

```text
DDD Repository ≠ DAO
DDD Repository ≠ CRUD
DDD Repository ≠ ORM abstraction
```

Repository 是：

> Aggregate 生命周期的持久化抽象。

---

# 18. Repository 查询边界

禁止：

```java
List<Order> findByStatusAndAmountGreaterThanAnd...
```

Repository 不应承担复杂 Query Model。

复杂查询应该进入：

```text
CQRS Query Model
```

即：

```text
Command Side
    │
    ▼
AggregateRepository

Query Side
    │
    ▼
QueryRepository / ReadModel
```

---

# 19. Repository Optimistic Concurrency

Aggregate 应支持版本控制。

例如：

```text
Order
version = 7
```

保存：

```text
expectedVersion = 7
```

如果数据库当前：

```text
version = 8
```

则：

```text
Concurrency Conflict
```

必须失败。

---

# 20. Concurrency Semantics

正式语义：

```text
load(version = N)
      │
      ▼
mutate
      │
      ▼
save(expectedVersion = N)
      │
      ├── current = N
      │       │
      │       ▼
      │     success
      │
      └── current != N
              │
              ▼
       concurrency conflict
```

禁止静默覆盖。

---

# 21. DomainService

Domain Service 用于：

> 无法自然归属于单个 Aggregate / Entity / Value Object 的领域行为。

例如：

```text
TransferService
PricingService
EligibilityService
ExchangePolicyService
```

---

# 22. DomainService 特征

Domain Service 应：

```text
stateless
business-oriented
domain-only
framework-independent
```

不应：

```text
@Service
@Component
@Transactional
@Autowired
```

这些属于技术层。

---

# 23. DomainService 示例

```java
public final class TransferService {

    public void transfer(
            Account source,
            Account target,
            Money amount) {

        source.withdraw(amount);
        target.deposit(amount);
    }
}
```

这里业务规则属于 Domain。

---

# 24. DomainService 与 ApplicationService

必须严格区分。

### Domain Service

负责：

```text
Business Rule
Business Calculation
Domain Decision
```

### Application Service

负责：

```text
Load Aggregate
Call Domain
Save Aggregate
Transaction
Publish/Dispatch
```

即：

```text
Application Service
       │
       ├── load
       │
       ▼
Domain Service
       │
       ▼
Aggregate
       │
       ▼
save
```

---

# 25. DomainPolicy

Policy 表示：

> 一个独立、明确、可复用的业务决策规则。

例如：

```text
CreditApprovalPolicy
DiscountPolicy
OrderCancellationPolicy
RiskPolicy
```

推荐：

```java
public interface DomainPolicy<T, R> {

    R evaluate(T input);
}
```

---

# 26. Policy 与 Specification

两者必须区分。

### Specification

回答：

```text
是否满足条件？
```

例如：

```java
Specification<Customer>
```

结果：

```java
boolean
```

---

### Policy

回答：

```text
业务应该做什么？
```

例如：

```java
DiscountPolicy
```

结果可能是：

```text
Discount
NoDiscount
ManualReview
Rejected
```

因此：

```text
Specification = Predicate

Policy = Decision
```

---

# 27. DomainDecision

可进一步定义：

```java
public interface DomainDecision {
}
```

具体领域可以实现：

```java
public record ApprovalDecision(
        boolean approved,
        String reason
) implements DomainDecision {
}
```

---

# 28. Specification

Phase II 已定义：

```java
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);
}
```

Domain Layer 允许组合：

```text
AND
OR
NOT
```

例如：

```java
Specification<Order> paid =
        order -> order.status() == PAID;

Specification<Order> cancellable =
        order -> order.status() == CREATED
              || order.status() == PAID;
```

---

# 29. Specification 组合

推荐支持：

```java
Specification<T> and(
    Specification<T> other
);

Specification<T> or(
    Specification<T> other
);

Specification<T> not();
```

语义：

```text
A AND B
A OR B
NOT A
```

---

# 30. DomainRule

Specification 主要回答：

```text
是否满足？
```

Domain Rule 更强调：

```text
违反业务规则时发生什么？
```

例如：

```text
Order cannot be paid twice
Account cannot withdraw beyond limit
User cannot approve own request
```

可定义：

```java
public interface DomainRule<T> {

    void validate(T target);
}
```

---

# 31. DomainRule 与 Exception

Rule 失败：

```text
DomainRule
    │
    ▼
DomainError
    │
    ▼
DomainException
```

例如：

```java
throw new DomainException(
    OrderErrors.ALREADY_PAID
);
```

Domain 层不抛：

```text
SQLException
PersistenceException
HttpException
FeignException
KafkaException
```

---

# 32. DomainFactory

Factory 用于：

> 创建复杂 Aggregate / Entity。

当创建过程简单时：

```java
new Order(...)
```

即可。

复杂创建过程才使用 Factory。

---

# 33. Factory Contract

推荐：

```java
@FunctionalInterface
public interface DomainFactory<I, O> {

    O create(I input);
}
```

例如：

```java
public final class OrderFactory
        implements DomainFactory<CreateOrderData, Order> {

    @Override
    public Order create(CreateOrderData data) {
        // validate
        // construct
        // establish invariants
        // return aggregate
    }
}
```

---

# 34. Factory 禁止事项

Factory 不负责：

```text
Repository
Transaction
HTTP
Database
Messaging
```

例如禁止：

```java
orderRepository.save(...)
```

---

# 35. Domain Event

Domain Event 表示：

> 已经发生的业务事实。

例如：

```text
OrderCreated
OrderPaid
OrderShipped
AccountDebited
CustomerRegistered
```

---

# 36. Domain Event 语义

Domain Event 必须是：

```text
Immutable
Past-tense
Business meaningful
Domain-owned
```

例如：

```java
OrderPaid
```

优于：

```java
PayOrderCommand
```

两者语义不同：

```text
Command = 要做什么

Event = 已经发生什么
```

---

# 37. Aggregate Event Collection

Aggregate：

```text
perform behavior
       │
       ▼
change state
       │
       ▼
record event
```

例如：

```java
public void pay(PaymentId paymentId) {

    ensurePayable();

    this.status = OrderStatus.PAID;

    recordEvent(
        new OrderPaid(
            this.id(),
            paymentId
        )
    );
}
```

---

# 38. Event 不直接 Publish

禁止：

```java
order.pay();

eventBus.publish(...);
```

由 Aggregate 自己：

```java
eventBus.publish(...)
```

更禁止。

Aggregate 只负责：

```text
record
```

而不是：

```text
publish
```

---

# 39. Event Lifecycle

完整生命周期：

```text
Domain Behavior
      │
      ▼
State Change
      │
      ▼
Domain Event Recorded
      │
      ▼
Aggregate Saved
      │
      ▼
Transaction Commit
      │
      ▼
Event Dispatch
```

如果使用 Outbox：

```text
Aggregate
   │
   ├── State
   │
   └── Domain Event
          │
          ▼
       Outbox
          │
          ▼
      Dispatcher
          │
          ▼
   Integration Event
```

---

# 40. Domain Event 与 Integration Event

必须区分。

```text
Domain Event
      │
      ▼
Domain Layer
```

表示领域内部事实。

而：

```text
Integration Event
      │
      ▼
Event / Infrastructure Layer
```

用于：

```text
Bounded Context
Service
System
Message Broker
```

之间通信。

---

# 41. Domain Event 不等于消息

例如：

```text
OrderPaid
```

在 Domain 中首先是：

```text
Business Fact
```

它不是天然：

```text
Kafka Message
RabbitMQ Message
HTTP Event
```

消息化属于后续 Layer。

---

# 42. Domain Event Metadata Boundary

Domain Event 可以包含：

```text
eventId
occurredAt
aggregateId
aggregateType
```

但以下内容原则上不进入核心 Domain Event：

```text
traceId
spanId
tenantId
transportId
Kafka partition
HTTP headers
JWT claims
```

这些属于：

```text
Application / Event / Infrastructure
```

---

# 43. Aggregate Lifecycle

正式生命周期：

```text
┌─────────┐
│ Create  │
└────┬────┘
     ▼
┌─────────┐
│ Active  │
└────┬────┘
     │
     │ behavior
     ▼
┌─────────┐
│ Mutated │
└────┬────┘
     │
     │ record events
     ▼
┌──────────────┐
│ Pending Save │
└──────┬───────┘
       │
       │ repository.save
       ▼
┌──────────────┐
│ Persisted    │
└──────┬───────┘
       │
       │ commit
       ▼
┌──────────────┐
│ Committed    │
└──────┬───────┘
       │
       │ dispatch
       ▼
┌──────────────┐
│ Published    │
└──────────────┘
```

---

# 44. Repository 与 Transaction

Repository 不拥有 Transaction。

错误：

```java
repository.save(order);
repository.commit();
```

正确：

```text
Application Service
      │
      ▼
Transaction
      │
      ├── repository.load
      │
      ├── aggregate.behavior
      │
      ├── repository.save
      │
      └── commit
```

Transaction 属于 Application / Transaction infrastructure。

---

# 45. Domain Layer 与 Port

Hexagonal Architecture 中：

```text
Domain
   │
   │ domain abstractions
   ▼
Ports
   │
   ▼
Adapters
```

但是必须避免建立一个巨大的：

```java
Port
```

接口集合。

---

# 46. Port 分类

推荐分为：

```text
Inbound Port
Outbound Port
```

### Inbound Port

外部世界调用 Application：

```text
REST
RPC
Message
CLI
Scheduler
```

### Outbound Port

Application 访问外部能力：

```text
Payment
IAM
Email
Storage
External API
```

---

# 47. Domain 是否依赖 Port？

默认：

```text
Domain ──X──► ddd-port
```

原因：

`ddd-port` 是 Application/Hexagonal Boundary 的概念，不应该成为 Domain 的通用依赖容器。

---

# 48. Domain Repository 的特殊性

Repository 是特殊的 Domain Abstraction。

它属于：

```text
Domain
```

因为：

```text
Aggregate
      │
      ▼
Repository
```

二者共同描述 Domain Model。

因此：

```text
ddd-domain
    └── Repository Contract
```

而不是：

```text
ddd-infrastructure
    └── Repository Contract
```

---

# 49. Repository Implementation

例如：

```text
ddd-domain
    │
    │ AggregateRepository
    ▼
ddd-infrastructure
    │
    └── JpaOrderRepository
```

Infrastructure 实现 Domain Contract。

---

# 50. Dependency Inversion

正式结构：

```text
                 ┌───────────────────┐
                 │    ddd-domain     │
                 │                   │
                 │ Repository<T>     │
                 └─────────▲─────────┘
                           │
                           │ implements
                           │
                 ┌─────────┴─────────┐
                 │ ddd-infrastructure │
                 │                   │
                 │ Jpa...Repository  │
                 └───────────────────┘
```

因此 Domain 不依赖数据库。

---

# 51. Domain Model Purity

Domain Model 必须满足：

```text
Framework Independent
Persistence Ignorant
Transport Independent
Technology Independent
```

即：

```text
Domain Model
     │
     ├── 不知道 Spring
     ├── 不知道 Hibernate
     ├── 不知道 PostgreSQL
     ├── 不知道 Kafka
     ├── 不知道 REST
     └── 不知道 Redis
```

---

# 52. Package Structure

建议：

```text
io.github.regalpine.ddd.domain
│
├── aggregate
│
├── entity
│
├── value
│
├── service
│
├── policy
│
├── factory
│
├── specification
│
├── rule
│
├── repository
│
├── event
│
└── error
```

---

# 53. Repository Package

```text
domain.repository
```

示例：

```java
package io.github.regalpine.ddd.domain.repository;

public interface AggregateRepository<
        A extends AggregateRoot<I>,
        I extends Identifier> {

    Optional<A> findById(I id);

    A save(A aggregate);

    void delete(A aggregate);
}
```

---

# 54. Repository 与 Query

禁止：

```java
OrderRepository.search(...)
OrderRepository.count(...)
OrderRepository.groupBy(...)
```

这些应该进入 Query Side。

正式原则：

```text
AggregateRepository
    =
Command Side Persistence

QueryRepository
    =
Query Side Read Access
```

---

# 55. Domain Service Package

```text
domain.service
```

示例：

```java
package io.github.regalpine.ddd.domain.service;

public final class OrderPricingService {

    public Money calculate(
            Order order,
            PricingPolicy policy) {

        return policy.calculate(order);
    }
}
```

---

# 56. Policy Package

```text
domain.policy
```

例如：

```text
PricingPolicy
CreditPolicy
EligibilityPolicy
CancellationPolicy
```

Policy 应保持：

```text
Explicit
Named
Testable
Composable
```

---

# 57. Factory Package

```text
domain.factory
```

Factory 主要解决：

```text
Complex Construction
Invariant Establishment
Domain Creation Logic
```

---

# 58. Specification Package

```text
domain.specification
```

支持：

```text
Simple Specification
Composite Specification
Reusable Specification
```

但不要把所有业务逻辑都变成 Specification。

---

# 59. Rule Package

```text
domain.rule
```

适用于：

```text
Invariant Validation
Business Rule Enforcement
State Transition Rule
```

---

# 60. Domain Event Package

```text
domain.event
```

用于：

```text
Domain Event Definition
Event Registration
Event Semantics
```

但不包含：

```text
Kafka Publisher
Rabbit Publisher
Outbox DAO
Message Serializer
```

---

# 61. 四层职责最终定义

| Layer | 主要职责 |
|---|---|
| Interface | 接收/转换外部请求 |
| Application | 编排用例 |
| Domain | 表达业务规则 |
| Infrastructure | 实现技术能力 |

---

# 62. Hexagonal 对应关系

```text
                 External World
                      │
        ┌─────────────┴─────────────┐
        ▼                           ▼
 Inbound Adapter              Outbound Adapter
        │                           ▲
        ▼                           │
 Inbound Port                 Outbound Port
        │                           ▲
        └─────────────┬─────────────┘
                      ▼
                 Application
                      │
                      ▼
                   Domain
```

Domain 是业务核心。

---

# 63. CQRS 对 Domain 的影响

CQRS 不应该污染 Domain Model。

Domain 不知道：

```text
Command
Query
CommandBus
QueryBus
ReadModel
Projection
```

Domain 只知道：

```text
Business State
Business Behavior
Business Rules
Domain Events
```

---

# 64. Command Side

```text
Command
   │
   ▼
CommandHandler
   │
   ▼
Application Service
   │
   ▼
AggregateRepository
   │
   ▼
Aggregate
```

---

# 65. Query Side

```text
Query
   │
   ▼
QueryHandler
   │
   ▼
ReadModel / QueryRepository
   │
   ▼
Database / Search / Cache
```

Query Side 不需要加载 Aggregate。

---

# 66. 典型订单流程

```text
POST /orders/{id}/pay
          │
          ▼
     PayOrderCommand
          │
          ▼
   PayOrderHandler
          │
          ▼
   OrderRepository
          │
          ▼
       Order
          │
          ▼
      order.pay()
          │
          ▼
    OrderPaid Event
          │
          ▼
       save()
          │
          ▼
      commit()
```

---

# 67. Domain Model 示例

```java
public final class Order
        extends AggregateRootSupport<OrderId> {

    private OrderStatus status;

    public void pay(PaymentId paymentId) {

        if (status != OrderStatus.CREATED) {
            throw new DomainException(
                OrderErrors.NOT_PAYABLE
            );
        }

        this.status = OrderStatus.PAID;

        recordEvent(
            new OrderPaid(
                id(),
                paymentId
            )
        );
    }
}
```

---

# 68. 不允许的 Domain Model

```java
@Entity
@Table(name = "orders")
public class Order {

    @Autowired
    private PaymentService paymentService;

    @Transactional
    public void pay() {
        ...
    }
}
```

原因：

```text
Domain polluted by Infrastructure
```

---

# 69. Domain Dependency Matrix

| Domain 使用对象 | Allowed |
|---|---:|
| Entity | ✅ |
| Value Object | ✅ |
| Aggregate | ✅ |
| Domain Event | ✅ |
| Specification | ✅ |
| Domain Rule | ✅ |
| Domain Policy | ✅ |
| Domain Service | ✅ |
| Repository Contract | ✅ |
| Spring | ❌ |
| JPA | ❌ |
| JDBC | ❌ |
| Redis | ❌ |
| Kafka | ❌ |
| REST | ❌ |
| HTTP | ❌ |
| JSON | ❌ |

---

# 70. Domain API 稳定性原则

`ddd-domain` 必须比 Application API 更稳定。

稳定层级：

```text
ddd-core
    ▲
    │
ddd-domain
    ▲
    │
ddd-application
    ▲
    │
adapters
```

越靠下：

```text
变化越频繁
```

越靠上：

```text
稳定性越高
```

---

# 71. Versioning Policy

推荐：

```text
ddd-core
ddd-domain
```

遵循严格 Semantic Versioning。

例如：

```text
1.0.0
1.1.0
1.1.1
2.0.0
```

Domain API 删除/修改属于高风险 breaking change。

---

# 72. Framework 不应强迫开发者实现所有接口

不要求：

```java
implements Entity
implements DomainService
implements DomainPolicy
implements Factory
implements Specification
```

全部存在。

原则：

> Framework 提供语义能力，而不是制造接口仪式。

---

# 73. 最小领域模型

一个最简单的 Aggregate：

```java
public final class Customer
        extends AggregateRootSupport<CustomerId> {

    private CustomerName name;

    public void rename(CustomerName newName) {
        this.name = newName;

        recordEvent(
            new CustomerRenamed(id(), newName)
        );
    }
}
```

只依赖：

```text
ddd-core
ddd-domain
```

---

# 74. Domain Layer 最小 API

Phase III 后，核心 API 控制在：

```text
AggregateRepository
DomainService
DomainPolicy
DomainFactory
Specification
DomainRule
DomainEvent
```

其余能力尽量复用：

```text
ddd-core
```

避免 Domain Framework 膨胀。

---

# 75. 本阶段正式边界

本阶段确定：

```text
ddd-core
    =
DDD Semantic Kernel

ddd-domain
    =
Business Domain Modeling

ddd-application
    =
Use Case Orchestration

ddd-cqrs
    =
Command / Query Infrastructure

ddd-event
    =
Event Infrastructure

ddd-transaction
    =
Transaction Infrastructure

ddd-port
    =
External Capability Boundary

ddd-infrastructure
    =
Technology Implementation

ddd-spring-boot
    =
Framework Integration
```

---

# 76. 最终依赖原则

必须长期保持：

```text
                 ┌──────────────┐
                 │  ddd-core    │
                 └──────▲───────┘
                        │
                 ┌──────┴───────┐
                 │ ddd-domain   │
                 └──────▲───────┘
                        │
              ┌─────────┴─────────┐
              │ ddd-application   │
              └─────────▲─────────┘
                        │
              ┌─────────┴─────────┐
              │    adapters       │
              │ infrastructure    │
              └───────────────────┘
```

依赖方向：

```text
Outside → Inside
```

而不是：

```text
Inside → Outside
```

---

# 77. Phase III Architecture Decision Records

## ADR-001

**Decision:**

Repository Contract 位于 Domain Layer。

**Reason:**

Repository 描述 Aggregate 持久化需求，而不是数据库技术。

---

## ADR-002

**Decision:**

Domain 不依赖 `ddd-port`。

**Reason:**

避免把所有 Port 抽象变成 Domain 的技术依赖。

---

## ADR-003

**Decision:**

Query Repository 不属于 Domain Repository。

**Reason:**

CQRS 下 Query Side 与 Aggregate Persistence 是不同模型。

---

## ADR-004

**Decision:**

Domain Event 不直接发布。

**Reason:**

避免 Domain 与 Messaging/Infrastructure 耦合。

---

## ADR-005

**Decision:**

Transaction 不属于 Domain。

**Reason:**

事务边界通常由 Application Use Case 决定。

---

## ADR-006

**Decision:**

不提供 Generic CRUD Repository。

**Reason:**

防止 DDD Framework 退化为 ORM abstraction。

---

# 78. Phase III 完成判定

Phase III 完成后：

```text
DDD Core             ✅
Domain Model         ✅
Aggregate            ✅
Repository           ✅
Domain Service       ✅
Policy               ✅
Factory              ✅
Specification        ✅
Domain Rule          ✅
Domain Event         ✅
CQRS Boundary        ✅
Hexagonal Boundary   ✅
Dependency DAG       ✅
```

下一阶段不再重新定义这些概念。

---

# 79. 下一阶段

进入：

# Phase IV — Application Layer & Use Case Specification

重点正式定义：

```text
Command
Query
CommandHandler
QueryHandler
ApplicationService
UseCase
Inbound Port
Outbound Port
Transaction Boundary
Authorization Boundary
Idempotency
Concurrency
Application Result
Application Error
```

并进一步建立：

```text
Command Side
       │
       ▼
Application
       │
       ▼
Domain
       │
       ▼
Repository
```

以及：

```text
Query Side
       │
       ▼
QueryHandler
       │
       ▼
Read Model
```

最终形成完整的：

```text
DDD
+
Hexagonal
+
CQRS
+
Enterprise Application Framework
```

而不是继续向 DSL 演进。

---

# 80. Phase III 收敛声明

从本阶段开始，以下内容视为**冻结的基础架构决策**：

```text
1. Java 17+
2. Maven
3. io.github.regalpine.ddd
4. ddd-core
5. ddd-domain
6. Classic DDD Four Layers
7. Hexagonal Architecture
8. CQRS
9. Repository 属于 Domain Contract
10. Query Repository 与 Aggregate Repository 分离
11. Domain Event 与 Integration Event 分离
12. Domain 不依赖 Infrastructure
13. Domain 不依赖 Spring
14. Domain 不直接 Publish Event
15. Domain 不包含 Transaction
16. Framework 不提供 Generic CRUD Repository
17. Framework 不引入新的 DSL
```

后续 Phase IV 及以后只允许：

```text
实现
细化
补充
验证
```

不得重新定义上述基础架构原则，除非发现真正的架构级矛盾。

---

# End of Phase III