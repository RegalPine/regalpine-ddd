# RegalPine DDD Framework
## Phase IV — Application Layer & Use Case Specification v0.1

**GroupId:** `io.github.regalpine.ddd`  
**Java:** 17+  
**Build:** Maven  
**Architecture:** DDD + Four-Layer + Hexagonal + CQRS  
**Module:** `ddd-application`  
**Status:** Architecture Baseline

---

# 1. 本阶段目标

Phase III 已经确定：

```text
ddd-core
    ↓
ddd-domain
    ↓
ddd-application
```

其中：

- `ddd-core`：DDD Semantic Kernel
- `ddd-domain`：Domain Model
- `ddd-application`：Application / Use Case Layer

本阶段正式定义：

```text
Command
Query
Use Case
Command Handler
Query Handler
Application Service
Inbound Port
Outbound Port
Transaction Boundary
Authorization Boundary
Idempotency
Concurrency
Application Result
Application Error
```

本阶段不重新定义 Domain。

---

# 2. Application Layer 定位

Application Layer 的职责：

> 描述系统提供哪些业务用例，以及如何协调 Domain 完成这些用例。

Application Layer 是：

```text
Use Case Orchestration
```

不是：

```text
Business Rule Container
```

---

# 3. Application Layer 不应该做什么

Application Layer 不应该承载核心业务规则。

禁止：

```java
if (order.status() == PAID) {
    ...
}
```

如果这是业务不变量，应进入 Aggregate。

Application Layer 可以判断：

```text
调用哪个 Aggregate
调用哪个 Domain Service
是否允许执行该 Use Case
事务如何组织
如何处理幂等
如何转换输入输出
```

---

# 4. Application Layer Architecture

```text
                 Interface Adapter
                        │
                        ▼
                 ┌──────────────┐
                 │ Inbound Port │
                 └──────┬───────┘
                        │
                        ▼
                Application Use Case
                        │
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
     Repository     Domain Service   Port
          │             │             │
          └─────────────┼─────────────┘
                        ▼
                     Domain
```

---

# 5. CQRS Application Model

Application Layer 分成：

```text
Command Side
Query Side
```

即：

```text
                    Application
                    /          \
                   /            \
             Command            Query
                │                 │
                ▼                 ▼
           CommandHandler    QueryHandler
                │                 │
                ▼                 ▼
             Domain            Read Model
```

---

# 6. Command

Command 表示：

> 请求系统执行某个业务意图。

例如：

```text
CreateOrder
PayOrder
CancelOrder
ShipOrder
RegisterCustomer
ChangeCustomerAddress
```

Command 应表达：

```text
Intent
```

而不是数据库操作。

---

# 7. Command Contract

建议：

```java
package io.github.regalpine.ddd.application.command;

public interface Command<R> {
}
```

例如：

```java
public record PayOrderCommand(
        String orderId,
        String paymentId
) implements Command<PayOrderResult> {
}
```

---

# 8. Command 必须是 Immutable

推荐 Java Record：

```java
public record CancelOrderCommand(
        String orderId,
        String reason
) implements Command<CancelOrderResult> {
}
```

禁止：

```java
public class CancelOrderCommand {

    public void setOrderId(...) {
        ...
    }
}
```

原因：

```text
Command = Immutable Request
```

---

# 9. Command 不直接操作 Domain

禁止：

```java
command.execute();
```

Command 本身没有业务执行能力。

正确：

```text
Command
   ↓
CommandHandler
   ↓
Aggregate
```

---

# 10. CommandHandler

正式定义：

```java
public interface CommandHandler<C extends Command<R>, R> {

    R handle(C command);
}
```

职责：

```text
接受 Command
    ↓
验证输入
    ↓
加载 Aggregate
    ↓
调用 Domain
    ↓
保存 Aggregate
    ↓
返回结果
```

---

# 11. CommandHandler 示例

```java
public final class PayOrderHandler
        implements CommandHandler<
            PayOrderCommand,
            PayOrderResult> {

    private final OrderRepository repository;

    public PayOrderHandler(
            OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public PayOrderResult handle(
            PayOrderCommand command) {

        OrderId orderId =
                OrderId.of(command.orderId());

        Order order = repository
                .findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        order.pay(
                PaymentId.of(command.paymentId())
        );

        repository.save(order);

        return new PayOrderResult(order.id());
    }
}
```

---

# 12. CommandHandler 不承担业务规则

错误：

```java
if (order.status() == CREATED) {
    order.setStatus(PAID);
}
```

正确：

```java
order.pay(paymentId);
```

Handler 是：

```text
Orchestrator
```

不是：

```text
Domain Model
```

---

# 13. Query

Query 表示：

> 查询系统当前可见信息。

例如：

```text
GetOrder
SearchOrders
GetCustomerProfile
ListInvoices
GetOrderStatistics
```

Query 不改变业务状态。

---

# 14. Query Contract

```java
public interface Query<R> {
}
```

例如：

```java
public record GetOrderQuery(
        String orderId
) implements Query<OrderView> {
}
```

---

# 15. QueryHandler

```java
public interface QueryHandler<Q extends Query<R>, R> {

    R handle(Q query);
}
```

Query Handler：

```text
Query
  ↓
QueryHandler
  ↓
Read Model
  ↓
Result
```

---

# 16. Query 不加载 Aggregate

原则：

```text
Query Side ≠ Domain Aggregate
```

不推荐：

```java
Order order = repository.findById(id);
return OrderView.from(order);
```

对于简单查询可以工作，但 Framework 的 CQRS 模型不应以此为标准。

复杂查询应该：

```text
Query
  ↓
QueryRepository
  ↓
Read Model
```

---

# 17. Query Repository

Query Repository 可以定义：

```java
public interface OrderQueryRepository {

    Optional<OrderView> findById(String orderId);

    PageResult<OrderView> search(
            OrderSearchCriteria criteria);
}
```

它与：

```text
AggregateRepository
```

完全不同。

---

# 18. Command Repository vs Query Repository

```text
Command Side
────────────────────────

Command
   ↓
Handler
   ↓
AggregateRepository
   ↓
Aggregate
```

```text
Query Side
────────────────────────

Query
   ↓
Handler
   ↓
QueryRepository
   ↓
ReadModel
```

---

# 19. Use Case

Use Case 是 Application Layer 的核心概念。

定义：

> 一个完整的业务应用操作。

例如：

```text
Pay Order
Cancel Order
Create Customer
Submit Application
Approve Application
```

一个 Use Case 可以：

```text
调用多个 Domain Object
调用多个 Domain Service
访问多个 Port
使用一个事务
```

---

# 20. Use Case 与 Command

通常：

```text
Command = Input
Use Case = Business Application Capability
Handler = Execution
```

例如：

```text
PayOrderCommand
        │
        ▼
PayOrderUseCase
        │
        ▼
PayOrderHandler
```

不过 Framework 不强制每个 Command 再创建一个 UseCase 类型。

推荐：

> CommandHandler 本身可以作为 Use Case 的执行入口。

这样避免产生：

```text
Command
UseCase
Service
Handler
Executor
Manager
```

层层包装。

---

# 21. ApplicationService

ApplicationService 用于：

> 跨多个 Domain Object 的应用编排。

例如：

```java
public final class TransferMoneyService {

    public TransferResult transfer(
            TransferCommand command) {

        // load source
        // load target
        // call domain service
        // save aggregates
        // return result
    }
}
```

---

# 22. Handler 与 ApplicationService

建议：

```text
简单 Use Case：

Command
  ↓
Handler
  ↓
Aggregate
```

复杂 Use Case：

```text
Command
  ↓
Handler
  ↓
ApplicationService
  ├── Repository
  ├── DomainService
  ├── OutboundPort
  └── Repository
```

不要强制所有 Use Case 都经过 ApplicationService。

---

# 23. Inbound Port

Hexagonal Architecture 中：

> Inbound Port 定义应用可以被调用什么。

例如：

```java
public interface PayOrderUseCase {

    PayOrderResult execute(
            PayOrderCommand command);
}
```

Controller 只依赖：

```text
PayOrderUseCase
```

而不是具体 Handler。

---

# 24. Inbound Adapter

例如 REST：

```text
HTTP
 ↓
OrderController
 ↓
PayOrderUseCase
 ↓
Application
```

Controller 不应该：

```text
访问 Repository
访问 Aggregate
执行 Domain Rule
启动 Transaction
```

---

# 25. Outbound Port

Outbound Port 表示：

> Application 对外部能力的需求。

例如：

```java
public interface PaymentGateway {

    PaymentResult pay(
            PaymentRequest request);
}
```

Application 依赖：

```text
PaymentGateway
```

Infrastructure 实现：

```text
StripePaymentGateway
AlipayPaymentGateway
InternalPaymentGateway
```

Application 不知道具体技术。

---

# 26. Port 的依赖方向

```text
Application
    │
    ▼
Outbound Port
    ▲
    │
Infrastructure Adapter
```

即：

```text
Interface ← Port → Application
                     ↑
                     │
               Infrastructure
```

Infrastructure 实现 Port。

---

# 27. Application Dependency

`ddd-application` 可以：

```text
ddd-core
ddd-domain
ddd-port
```

不能：

```text
Spring
Hibernate
JPA
JDBC
Kafka
Redis
HTTP Client
```

---

# 28. Transaction Boundary

事务边界属于 Application Use Case。

典型：

```text
Command
  ↓
CommandHandler
  ↓
BEGIN TRANSACTION
  ↓
Load Aggregate
  ↓
Execute Domain Behavior
  ↓
Save Aggregate
  ↓
Persist Outbox
  ↓
COMMIT
```

---

# 29. Transaction 不进入 Domain

Domain 不应该：

```java
@Transactional
```

也不应该：

```java
transaction.begin();
transaction.commit();
```

Domain 只执行：

```text
Business Behavior
```

---

# 30. Transaction Port

Application 可以依赖：

```java
public interface TransactionExecutor {

    <R> R execute(
            TransactionCallback<R> callback);
}
```

例如：

```java
return transactionExecutor.execute(() -> {
    Order order = repository.findById(id)
            .orElseThrow();

    order.pay(paymentId);

    repository.save(order);

    return new PayOrderResult(order.id());
});
```

具体事务实现属于：

```text
ddd-transaction
```

---

# 31. Transaction Scope

默认：

```text
One Command
    =
One Application Transaction
```

但不是绝对规则。

例如：

```text
Long-running workflow
Saga
Batch
Async Process
```

可能拥有不同事务模型。

---

# 32. Aggregate Transaction Boundary

默认规则：

> 一个事务尽量修改一个 Aggregate。

例如：

```text
Order.pay()
```

通常：

```text
Transaction
  └── Order
```

而不是：

```text
Transaction
 ├── Order
 ├── Customer
 ├── Inventory
 ├── Payment
 └── Shipment
```

跨 Aggregate 协作优先考虑：

```text
Domain Event
Process Manager
Saga
```

这些将在后续阶段定义。

---

# 33. Idempotency

Command Side 必须支持幂等能力。

典型场景：

```text
HTTP retry
Message retry
Client retry
Network timeout
Consumer redelivery
```

例如：

```text
PayOrderCommand
commandId = C123
```

重复提交：

```text
C123
C123
C123
```

只能产生一次业务效果。

---

# 34. Idempotency Key

Command 可以拥有：

```java
public interface Command<R> {

    CommandId id();
}
```

但不建议把所有业务 Command 强制增加过多字段。

推荐 Framework 提供：

```text
ApplicationExecutionContext
```

承载：

```text
commandId
correlationId
causationId
principal
tenant
```

这些不是 Domain Model。

---

# 35. ApplicationExecutionContext

概念：

```java
public record ApplicationExecutionContext(
        String commandId,
        String correlationId,
        String causationId
) {
}
```

后续可以扩展：

```text
tenantId
principal
locale
deadline
```

但这些属于 Application Context。

---

# 36. Idempotency Port

推荐：

```java
public interface IdempotencyStore {

    Optional<IdempotencyResult> find(
            String key);

    void store(
            String key,
            IdempotencyResult result);
}
```

Infrastructure 实现：

```text
Redis
Database
Distributed KV
```

---

# 37. Idempotency 与 Transaction

必须考虑：

```text
业务事务
+
幂等记录
```

否则可能：

```text
Business Commit
      ↓
Idempotency Store Failed
      ↓
Retry
      ↓
Duplicate Business Effect
```

因此推荐：

```text
Command
 ↓
Idempotency Check
 ↓
Transaction
 ├── Domain Mutation
 ├── Repository
 └── Idempotency Record
 ↓
Commit
```

具体原子化策略由 Infrastructure / Transaction Layer 实现。

---

# 38. Concurrency

Application Layer 必须处理：

```text
Optimistic Concurrency
```

Domain：

```text
Aggregate.version
```

Repository：

```text
expectedVersion
```

Infrastructure：

```text
UPDATE ... WHERE version = ?
```

---

# 39. Concurrency Conflict

当：

```text
Expected Version = 7
Current Version = 8
```

Application 应转换为：

```text
ConcurrencyConflict
```

而不是向 API 暴露：

```text
OptimisticLockException
```

---

# 40. Application Error

Domain Error：

```text
Business Rule Failure
```

Application Error：

```text
Use Case Execution Failure
```

Infrastructure Error：

```text
Technology Failure
```

三者必须区分。

---

# 41. Error Mapping

```text
DomainException
      ↓
Application Error
      ↓
Adapter Error
      ↓
HTTP 409 / 400 / 403 ...
```

例如：

```text
OrderAlreadyPaid
        ↓
BUSINESS_RULE_VIOLATION
        ↓
HTTP 409
```

HTTP 不应该进入 Domain。

---

# 42. Authorization Boundary

授权不是 Domain Rule 的替代品。

两者不同：

```text
Authorization
=
Who may execute this Use Case?
```

```text
Domain Rule
=
Is this business operation valid?
```

例如：

```text
User A cannot approve invoice
```

可能是：

```text
Authorization
```

而：

```text
Invoice already approved
```

是：

```text
Domain Rule
```

---

# 43. Authorization Port

Application 可以依赖：

```java
public interface AuthorizationService {

    AuthorizationDecision authorize(
            AuthorizationRequest request);
}
```

例如：

```text
PayOrder
CancelOrder
ApproveOrder
```

---

# 44. Authorization 执行位置

推荐：

```text
Inbound Adapter
       ↓
Authentication
       ↓
Application Authorization
       ↓
Use Case
       ↓
Domain
```

但 Domain 仍然可以维护：

```text
business ownership invariant
```

两者不能混淆。

---

# 45. Tenant Boundary

多租户场景：

```text
Application Context
    │
    └── tenantId
```

Domain 不应该依赖：

```java
TenantContext.getCurrentTenant()
```

推荐：

```text
Application
    ↓
Repository
    ↓
Tenant-isolated Persistence
```

Domain 不读取 ThreadLocal TenantContext。

---

# 46. Authentication Boundary

Authentication：

```text
Who are you?
```

属于 Interface / IAM。

Authorization：

```text
Can you execute this use case?
```

属于 Application。

Business ownership：

```text
Does this domain operation satisfy business constraints?
```

属于 Domain。

---

# 47. Application Result

不建议所有 Handler 都返回：

```java
Object
```

推荐：

```java
public interface ApplicationResult {
}
```

或者直接使用：

```java
record PayOrderResult(
    OrderId orderId
) {
}
```

Application Result 不应该暴露：

```text
JPA Entity
Hibernate Proxy
Database Entity
```

---

# 48. DTO Boundary

建议：

```text
REST DTO
    ↓
Command
    ↓
Domain
    ↓
Result
    ↓
Response DTO
```

不要：

```text
REST DTO
    ↓
JPA Entity
```

---

# 49. Application Package

建议：

```text
io.github.regalpine.ddd.application
│
├── command
│   ├── Command
│   └── CommandHandler
│
├── query
│   ├── Query
│   └── QueryHandler
│
├── usecase
│
├── service
│
├── port
│   ├── inbound
│   └── outbound
│
├── transaction
│
├── idempotency
│
├── authorization
│
├── context
│
├── result
│
└── error
```

---

# 50. Command Package

```text
application.command
```

核心：

```java
public interface Command<R> {
}
```

```java
public interface CommandHandler<
        C extends Command<R>, R> {

    R handle(C command);
}
```

---

# 51. Query Package

```text
application.query
```

核心：

```java
public interface Query<R> {
}
```

```java
public interface QueryHandler<
        Q extends Query<R>, R> {

    R handle(Q query);
}
```

---

# 52. Use Case Package

```text
application.usecase
```

例如：

```java
public interface PayOrderUseCase {

    PayOrderResult execute(
            PayOrderCommand command);
}
```

---

# 53. Handler 是否就是 Use Case？

推荐框架策略：

```text
CommandHandler
=
Default Use Case Execution Model
```

即：

```java
public final class PayOrderHandler
        implements PayOrderUseCase,
                   CommandHandler<
                       PayOrderCommand,
                       PayOrderResult> {
}
```

但这不是强制要求。

---

# 54. Application Service

只有当一个 Use Case 具有明显编排复杂度时使用。

例如：

```text
TransferMoney
    ├── AccountRepository
    ├── ExchangeRatePort
    ├── TransferPolicy
    ├── Transaction
    └── Event
```

这时：

```text
Handler
   ↓
ApplicationService
```

有意义。

---

# 55. Application Service 禁止膨胀

禁止出现：

```text
OrderService
CustomerService
UserService
SystemService
CommonService
BusinessService
```

然后所有业务都塞进去。

推荐：

```text
PayOrderHandler
CancelOrderHandler
CreateOrderHandler
```

按 Use Case 划分。

---

# 56. Command Validation

分三层：

```text
Input Validation
Domain Validation
Infrastructure Validation
```

### Input Validation

例如：

```text
orderId != null
paymentId != null
```

### Domain Validation

例如：

```text
Order must be payable
```

### Infrastructure Validation

例如：

```text
Database constraint
```

---

# 57. Validation 不应混合

不要把：

```text
@NotNull
@Size
@Column
@Pattern
```

全部放进 Domain Model。

Core/Domain 应保持 Java 语义独立。

---

# 58. Command Execution Pipeline

正式流程：

```text
                    Command
                       │
                       ▼
              Input Validation
                       │
                       ▼
                 Idempotency
                       │
                       ▼
                Authorization
                       │
                       ▼
              Transaction Begin
                       │
                       ▼
              Command Handler
                       │
                       ▼
                  Repository
                       │
                       ▼
                  Aggregate
                       │
                       ▼
                Domain Behavior
                       │
                       ▼
                Domain Events
                       │
                       ▼
                  Repository
                       │
                       ▼
                 Outbox/Event
                       │
                       ▼
                Transaction Commit
                       │
                       ▼
                    Result
```

---

# 59. Query Execution Pipeline

```text
Query
 │
 ▼
Input Validation
 │
 ▼
Authorization
 │
 ▼
Query Handler
 │
 ▼
Query Repository
 │
 ▼
Read Model
 │
 ▼
Result
```

Query 默认不需要：

```text
Aggregate
Transaction
Domain Event
```

---

# 60. Command 与 Query 的一致性

CQRS 不要求：

```text
两个数据库
```

CQRS 首先是一种：

```text
Model Separation
```

即：

```text
Write Model ≠ Read Model
```

物理数据库是否分离属于部署决策。

---

# 61. Strong Consistency

Command Side：

```text
Aggregate State
+
Transaction
```

保证强一致。

---

# 62. Eventual Consistency

Query Side 如果通过 Projection 更新：

```text
Command
 ↓
Aggregate
 ↓
Domain Event
 ↓
Projection
 ↓
Read Model
```

则 Query Model 可能：

```text
Eventually Consistent
```

必须由 Application API 明确这一语义。

---

# 63. Read Model

Read Model 不属于 Domain。

例如：

```java
public record OrderSummaryView(
        String orderId,
        String customerName,
        String status,
        BigDecimal amount
) {
}
```

它可以为了查询性能而：

```text
Join
Denormalize
Flatten
Cache
Index
```

---

# 64. Domain 与 Read Model

禁止：

```text
Domain Entity → Query View
```

Domain 不应该知道 View。

推荐：

```text
Domain
   │
   ▼
Domain Event
   │
   ▼
Projection
   │
   ▼
Read Model
```

---

# 65. Application Event Boundary

Application 不应该把 Domain Event 直接当作外部消息。

后续由：

```text
ddd-event
```

负责：

```text
Domain Event
    ↓
Event Handling
    ↓
Integration Event
    ↓
Outbox
    ↓
Message Broker
```

---

# 66. Application Module Dependency

正式定义：

```text
ddd-application
    ├── ddd-core
    ├── ddd-domain
    └── ddd-port
```

禁止：

```text
ddd-application
    ├── ddd-infrastructure ❌
    ├── ddd-spring-boot ❌
    ├── spring-context ❌
    ├── hibernate ❌
    └── kafka-client ❌
```

---

# 67. Application Dependency Matrix

| Capability | Application |
|---|---:|
| Core | ✅ |
| Domain | ✅ |
| Inbound Port | ✅ |
| Outbound Port | ✅ |
| Command | ✅ |
| Query | ✅ |
| Transaction abstraction | ✅ |
| Idempotency abstraction | ✅ |
| Authorization abstraction | ✅ |
| Spring | ❌ |
| JPA | ❌ |
| JDBC | ❌ |
| Kafka | ❌ |
| Redis | ❌ |

---

# 68. Module Dependency Graph

正式版本：

```text
                      ddd-core
                         ▲
                         │
                    ddd-domain
                         ▲
              ┌──────────┴──────────┐
              │                     │
       ddd-application          ddd-port
              ▲                     ▲
              │                     │
        ddd-cqrs              ddd-infrastructure
              │                     │
              └──────────┬──────────┘
                         ▼
                  ddd-spring-boot
```

其中：

```text
ddd-cqrs → ddd-application
ddd-infrastructure → ddd-domain
ddd-infrastructure → ddd-port
ddd-spring-boot → application/cqrs/event/transaction/infrastructure
```

---

# 69. Application Layer Formal Invariants

### INV-APP-001

Application 不定义核心业务不变量。

### INV-APP-002

Command 不直接执行 Domain。

### INV-APP-003

Query 不修改 Domain State。

### INV-APP-004

Domain 不依赖 Application。

### INV-APP-005

Application 不依赖 Infrastructure Implementation。

### INV-APP-006

Transaction Boundary 位于 Application。

### INV-APP-007

Authorization 位于 Application Boundary。

### INV-APP-008

Authentication 不属于 Domain。

### INV-APP-009

Query Model 不属于 Domain。

### INV-APP-010

AggregateRepository 不承担 Query职责。

---

# 70. 完整业务调用链

最终标准：

```text
┌───────────────────────────┐
│ REST / RPC / Message      │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ Inbound Adapter            │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ Inbound Port / Use Case    │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ Application Handler        │
│                           │
│ Validation                │
│ Authorization             │
│ Idempotency               │
│ Transaction               │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ Domain                    │
│                           │
│ Aggregate                 │
│ Entity                    │
│ Value Object              │
│ Domain Service            │
│ Policy                    │
│ Rule                      │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ Repository / Outbound Port │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ Infrastructure             │
└───────────────────────────┘
```

---

# 71. Query 调用链

```text
REST / RPC
    │
    ▼
Inbound Adapter
    │
    ▼
Query Port
    │
    ▼
Query Handler
    │
    ▼
Authorization
    │
    ▼
Query Repository
    │
    ▼
Read Model
```

---

# 72. Framework 使用方式

开发者不需要学习新的 DSL。

直接编写：

```java
public record PayOrderCommand(
        OrderId orderId,
        PaymentId paymentId
) implements Command<PayOrderResult> {
}
```

然后：

```java
public final class PayOrderHandler
        implements CommandHandler<
            PayOrderCommand,
            PayOrderResult> {

    // application orchestration
}
```

Domain：

```java
public final class Order
        extends AggregateRootSupport<OrderId> {

    public void pay(PaymentId paymentId) {
        // business behavior
    }
}
```

这就是：

> Developer Framework，而不是 DSL。

---

# 73. Framework 不应隐藏 Use Case

禁止框架通过大量魔法：

```text
@AutoCommand
@AutoAggregate
@AutoRepository
@AutoHandler
```

自动推断整个系统。

优先：

```text
Explicit API
+
Convention
+
Optional Integration
```

---

# 74. Spring 集成边界

未来：

```text
ddd-spring-boot
```

可以提供：

```java
@UseCase
@Transactional
```

等集成能力。

但是：

```text
ddd-core
ddd-domain
ddd-application
```

本身不依赖 Spring。

因此可以：

```text
Spring
Quarkus
Micronaut
Jakarta EE
Plain Java
```

选择不同运行环境。

---

# 75. Application API 稳定原则

稳定程度：

```text
ddd-core
   ▲
ddd-domain
   ▲
ddd-application
   ▲
integration adapters
```

`Command`、`Query`、`Handler` 是 Application API。

REST Controller、Kafka Listener 等不属于 Framework Core API。

---

# 76. Phase IV 收敛决策

本阶段冻结：

```text
1. Application = Use Case Layer
2. Command = Intent
3. Query = Read Request
4. Handler = Execution Entry
5. Aggregate = Business State Boundary
6. Domain = Business Rule
7. Application = Orchestration
8. Repository = Aggregate Persistence Contract
9. QueryRepository = Read Access Contract
10. Transaction = Application Boundary
11. Authorization = Application Boundary
12. Authentication = External/IAM Boundary
13. Idempotency = Application Concern
14. Optimistic Concurrency = Domain + Repository Contract
15. Query Model ≠ Domain Model
16. Domain Event ≠ Integration Event
17. Infrastructure ≠ Application
18. No DSL
```

---

# 77. Phase IV 完成判定

```text
Command                    ✅
Query                      ✅
CommandHandler             ✅
QueryHandler               ✅
UseCase                    ✅
ApplicationService         ✅
Inbound Port               ✅
Outbound Port              ✅
Transaction Boundary       ✅
Authorization Boundary     ✅
Idempotency                 ✅
Concurrency                 ✅
Application Error           ✅
CQRS Separation             ✅
Read Model Boundary         ✅
```

---

# 78. 下一阶段

下一阶段进入：

# Phase V — CQRS & Event Architecture Specification

重点定义：

```text
CommandBus
QueryBus
Command Middleware
Query Middleware
Pipeline
Handler Registry
Dispatch Semantics
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
Event Schema Evolution
```

并正式解决：

```text
Command
   ↓
CommandBus
   ↓
Middleware
   ↓
Handler
   ↓
Transaction
   ↓
Aggregate
   ↓
Domain Event
   ↓
Outbox
   ↓
Event Dispatcher
   ↓
Projection / Integration
```

以及：

```text
Query
   ↓
QueryBus
   ↓
Middleware
   ↓
QueryHandler
   ↓
ReadModel
```

Phase V 完成后，整个框架将具备完整的 **DDD + Hexagonal + CQRS + Event-driven Application** 主干。

# End of Phase IV