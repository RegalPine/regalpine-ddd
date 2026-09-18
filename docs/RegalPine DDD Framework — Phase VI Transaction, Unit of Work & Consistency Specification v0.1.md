# RegalPine DDD Framework
## Phase VI — Transaction, Unit of Work & Consistency Specification

**Version:** v0.1  
**Group ID:** `io.github.regalpine.ddd`  
**Language:** Java 17+  
**Build:** Maven  
**Architecture:** DDD + Hexagonal Architecture + CQRS  
**Document Status:** Framework Specification  
**Scope:** Transaction / Unit of Work / Consistency / Concurrency / Outbox Atomicity / Idempotency Atomicity

---

# 1. 文档定位

Phase VI 的目标是定义 RegalPine DDD Framework 中：

- Transaction
- Unit of Work
- Aggregate Tracking
- Repository Lifecycle
- Commit
- Rollback
- Optimistic Concurrency
- Outbox Atomicity
- Command Idempotency Atomicity
- Transaction Propagation
- Isolation
- Retry
- Failure Recovery
- Async Boundary

之间的正式语义关系。

本阶段**不引入新的 DSL**。

本阶段也不要求：

- JPA
- Hibernate
- Spring Transaction
- Jakarta Transaction
- MyBatis
- JDBC
- Redis
- Kafka
- RabbitMQ

进入核心模型。

---

# 2. 核心架构原则

Phase VI 建立以下最终边界：

```text
                 CQRS
                   │
                   ▼
            Application Layer
                   │
          Transaction Boundary
                   │
                   ▼
              UnitOfWork
                   │
        ┌──────────┼──────────┐
        ▼          ▼          ▼
   Repository   Aggregate   Domain Events
        │          │          │
        └──────────┼──────────┘
                   ▼
                Outbox
                   │
                   ▼
                Commit
```

事务属于：

```text
Application + Infrastructure
```

而不是：

```text
Domain
```

Domain 不知道：

```text
Transaction
Commit
Rollback
Isolation
Propagation
Database
Connection
```

---

# 3. 四层架构映射

RegalPine DDD Framework 采用经典四层：

```text
┌──────────────────────────────────────┐
│ Interface Layer                      │
│ REST / RPC / CLI / Message Consumer  │
└──────────────────┬───────────────────┘
                   │
┌──────────────────▼───────────────────┐
│ Application Layer                    │
│ Command / Query / Handler / UoW      │
│ Transaction Boundary                 │
└──────────────────┬───────────────────┘
                   │
┌──────────────────▼───────────────────┐
│ Domain Layer                         │
│ Aggregate / Entity / VO / Service    │
│ Repository Contract / Domain Event   │
└──────────────────┬───────────────────┘
                   │
┌──────────────────▼───────────────────┐
│ Infrastructure Layer                 │
│ DB / ORM / Broker / Cache / Outbox   │
└──────────────────────────────────────┘
```

事务边界位于：

```text
Application
     │
     ▼
Infrastructure Transaction Adapter
```

Domain 不感知事务。

---

# 4. Transaction 与 UnitOfWork 的职责分离

必须严格区分：

```text
Transaction
```

和：

```text
UnitOfWork
```

二者不是同一个概念。

---

## 4.1 Transaction

Transaction 管理：

- Begin
- Commit
- Rollback
- Isolation
- Propagation
- Transaction lifecycle

它关注：

> 一组持久化操作是否作为一个原子单元完成。

---

## 4.2 UnitOfWork

UnitOfWork 管理：

- Aggregate Tracking
- Repository lifecycle
- Domain Event collection
- Change coordination
- Commit preparation
- Rollback cleanup

它关注：

> 本次业务操作中哪些领域对象参与了工作，以及如何协调它们的持久化生命周期。

---

## 4.3 二者关系

```text
Transaction
     │
     │ contains
     ▼
UnitOfWork
     │
     ├── Aggregate A
     ├── Aggregate B
     ├── Domain Events
     ├── Repository Operations
     └── Outbox Records
```

不是：

```text
UnitOfWork = Transaction
```

而是：

```text
Transaction
    owns physical atomicity

UnitOfWork
    owns domain/application work lifecycle
```

---

# 5. Transaction Boundary

## 5.1 Command Transaction

Command 默认采用：

```text
Command
  ↓
CommandBus
  ↓
Middleware
  ↓
Transaction
  ↓
CommandHandler
  ↓
Domain
  ↓
Repository
  ↓
Outbox
  ↓
Commit
```

因此：

> 一个 Command Handler 默认对应一个本地事务边界。

---

# 6. Query Transaction

Query 默认：

```text
Query
 ↓
QueryBus
 ↓
QueryHandler
 ↓
ReadModel
```

默认不要求写事务。

可以根据基础设施实现：

```text
No Transaction
```

或者：

```text
Read-only Transaction
```

---

# 7. TransactionManager

框架定义：

```java
package io.github.regalpine.ddd.transaction;

public interface TransactionManager {

    <T> T execute(TransactionCallback<T> callback);

    void execute(TransactionRunnable runnable);
}
```

---

# 8. TransactionCallback

```java
package io.github.regalpine.ddd.transaction;

@FunctionalInterface
public interface TransactionCallback<T> {

    T execute();
}
```

---

# 9. TransactionRunnable

```java
package io.github.regalpine.ddd.transaction;

@FunctionalInterface
public interface TransactionRunnable {

    void run();
}
```

---

# 10. UnitOfWork

建议 API：

```java
package io.github.regalpine.ddd.transaction;

public interface UnitOfWork {

    void register(Object aggregate);

    boolean contains(Object aggregate);

    void commit();

    void rollback();

    boolean isActive();
}
```

但框架实现不应该要求业务代码频繁直接操作 UnitOfWork。

推荐：

```text
Application
      ↓
TransactionManager
      ↓
UnitOfWorkManager
      ↓
CommandHandler
```

---

# 11. UnitOfWorkManager

```java
package io.github.regalpine.ddd.transaction;

public interface UnitOfWorkManager {

    UnitOfWork current();

    UnitOfWork begin();

    void complete();

    void rollback();

    boolean hasCurrent();
}
```

---

# 12. 推荐运行模型

业务代码：

```java
transactionManager.execute(() ->
    commandHandler.handle(command)
);
```

框架内部：

```text
TransactionManager
        │
        ▼
    begin transaction
        │
        ▼
   create UnitOfWork
        │
        ▼
 CommandHandler
        │
        ▼
 Repository
        │
        ▼
 Aggregate
        │
        ▼
 Domain Events
        │
        ▼
 Outbox
        │
        ▼
     commit
        │
        ▼
 complete UoW
```

---

# 13. UnitOfWork 生命周期

正式生命周期：

```text
NEW
 ↓
ACTIVE
 ↓
COMMITTING
 ↓
COMMITTED
```

异常路径：

```text
ACTIVE
 ↓
ROLLING_BACK
 ↓
ROLLED_BACK
```

关闭：

```text
COMMITTED / ROLLED_BACK
        ↓
       CLOSED
```

---

# 14. UnitOfWork 状态

```java
public enum UnitOfWorkStatus {

    NEW,
    ACTIVE,
    COMMITTING,
    COMMITTED,
    ROLLING_BACK,
    ROLLED_BACK,
    CLOSED
}
```

---

# 15. Aggregate Tracking

UnitOfWork 必须能够识别参与当前业务操作的 Aggregate。

逻辑模型：

```text
UnitOfWork
 ├── New Aggregates
 ├── Modified Aggregates
 ├── Deleted Aggregates
 └── Unchanged Aggregates
```

---

# 16. Tracking 不等于 ORM Dirty Checking

RegalPine 不定义：

```text
JPA Dirty Checking
```

框架只定义：

```text
Aggregate lifecycle tracking
```

实现方式可以是：

### Explicit

```java
repository.save(order);
```

### Snapshot

```text
Original State
      ↓
Current State
      ↓
Compare
```

### Event-based

```text
Aggregate behavior
      ↓
Domain Event
      ↓
Changed
```

基础设施可以自行选择。

---

# 17. 推荐优先级

默认推荐：

```text
Explicit Repository Save
```

而不是：

```text
Magic Dirty Checking
```

原因：

- 行为更加明确
- 更容易测试
- 更容易跨数据库
- 不依赖 ORM
- 避免隐式持久化
- 更适合 Framework API

---

# 18. Aggregate 生命周期

```text
Load
 ↓
Modify
 ↓
Register
 ↓
Save
 ↓
Collect Events
 ↓
Commit
```

创建：

```text
Create
 ↓
Register
 ↓
Save
 ↓
Collect Events
 ↓
Commit
```

删除：

```text
Load
 ↓
Delete
 ↓
Register
 ↓
Commit
```

---

# 19. Repository 与 UnitOfWork

Repository 负责：

```text
Load
Save
Delete
```

UnitOfWork 负责：

```text
Lifecycle
Coordination
```

因此：

```text
Repository ≠ UnitOfWork
```

禁止：

```text
Repository.commit()
```

禁止：

```text
Repository.rollback()
```

Repository 不拥有事务。

---

# 20. Repository Save 语义

`save()` 的语义是：

> 将 Aggregate 纳入当前持久化工作，而不是立即完成业务事务。

例如：

```java
orderRepository.save(order);
```

并不意味着：

```text
COMMIT
```

而是：

```text
register / persist intent
```

---

# 21. Commit 语义

完整 Commit：

```text
Validate
 ↓
Detect Concurrency
 ↓
Persist Aggregate
 ↓
Persist Domain Event / Outbox
 ↓
Commit Transaction
 ↓
Finalize UnitOfWork
```

---

# 22. Commit 前置阶段

```text
UnitOfWork
    ↓
PreCommit
    ↓
Aggregate Validation
    ↓
Concurrency Check
    ↓
Repository Flush/Write
    ↓
Outbox Write
    ↓
Transaction Commit
```

这里的：

```text
Flush
```

仅是语义概念。

框架 API 不强制采用名为 `flush()` 的方法。

---

# 23. Rollback

发生异常：

```text
Command
 ↓
Transaction
 ↓
Aggregate
 ↓
Repository
 ↓
Exception
 ↓
Rollback
```

Rollback 必须：

1. 回滚持久化修改
2. 放弃 Outbox 修改
3. 放弃当前 UnitOfWork
4. 清理当前上下文
5. 不发布 Integration Event

---

# 24. Domain Event 生命周期

Domain Event：

```text
Aggregate
 ↓
record(event)
 ↓
Pending Events
 ↓
UnitOfWork
 ↓
Outbox
 ↓
Transaction Commit
```

---

# 25. Event 不得提前发布

禁止：

```text
Aggregate
 ↓
Event
 ↓
Broker.publish()
 ↓
Database.commit()
```

因为：

```text
Broker Success
Database Failure
```

会导致：

```text
Event exists
State does not exist
```

---

# 26. 正确模型

```text
Transaction
 ├── Aggregate State
 └── Outbox Record
       │
       └── same commit
```

提交成功：

```text
COMMIT
 ↓
Outbox Dispatcher
 ↓
Broker
```

---

# 27. Pending Events 清理

Aggregate 的 pending events：

```text
record
 ↓
pending
 ↓
UnitOfWork collects
 ↓
Outbox persisted
 ↓
Transaction commit
 ↓
clear
```

不能：

```text
repository.save()
 ↓
clear events
```

因为：

```text
save ≠ commit
```

---

# 28. Commit Failure

例如：

```text
Aggregate write
      ↓
Outbox write
      ↓
Database commit
      ↓
FAIL
```

则：

```text
Aggregate
Outbox
UnitOfWork
```

均视为失败。

Domain Event 不应该被标记为已发布。

---

# 29. Optimistic Concurrency

Aggregate 必须支持版本语义。

例如：

```text
Order
version = 7
```

读取：

```text
SELECT ...
WHERE id = ?
```

更新：

```text
UPDATE ...
SET ...
    version = 8
WHERE id = ?
AND version = 7
```

如果影响行数：

```text
0
```

则：

```text
Concurrency Conflict
```

---

# 30. Aggregate Version

推荐：

```java
public interface VersionedAggregate {

    long version();
}
```

或者 AggregateRoot 直接提供：

```java
long version();
```

---

# 31. Version 语义

版本表示：

> Aggregate 持久化状态的逻辑版本。

版本必须：

- 单调递增
- 不允许业务代码任意修改
- 不作为业务字段使用
- 不承担时间戳语义

---

# 32. Version Increment

假设：

```text
Current Version = 10
```

成功提交后：

```text
Version = 11
```

失败：

```text
Version remains 10
```

---

# 33. Concurrency Exception

定义：

```java
public class ConcurrencyConflictException
        extends RuntimeException {

    private final String aggregateType;
    private final String aggregateId;
    private final long expectedVersion;
    private final long actualVersion;

    public ConcurrencyConflictException(
            String aggregateType,
            String aggregateId,
            long expectedVersion,
            long actualVersion) {

        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }
}
```

该异常不属于 Domain Business Error。

---

# 34. Concurrency Conflict 的层级

```text
Domain Business Rule Violation
        ↓
DomainException

Persistence Concurrency Conflict
        ↓
Application / Infrastructure Exception
```

例如：

```text
Order cannot be cancelled
```

属于：

```text
Domain
```

而：

```text
Order version 7 expected, actual version 8
```

属于：

```text
Concurrency
```

---

# 35. Retry

并发冲突属于潜在 transient failure。

可以：

```text
Command
 ↓
Transaction
 ↓
Concurrency Conflict
 ↓
Retry
```

但是必须满足：

```text
Retry Policy
```

---

# 36. 禁止无限 Retry

例如：

```text
retry = 3
```

之后：

```text
FAIL
```

推荐：

```java
public record RetryPolicy(
        int maxAttempts,
        Duration backoff
) {}
```

---

# 37. 允许 Retry 的异常

推荐：

```text
ConcurrencyConflictException
TransientDatabaseException
TemporaryConnectionFailure
TemporaryBrokerInfrastructureFailure
```

但具体实现必须由 Infrastructure 判断。

---

# 38. 禁止自动 Retry

以下情况默认不得自动 Retry：

```text
DomainException
ValidationException
AuthorizationException
IllegalArgumentException
BusinessRuleViolation
```

因为重试不会改变业务结果。

---

# 39. External Side Effect 与 Retry

事务内部禁止直接执行不可逆外部操作：

```text
Transaction
 ↓
Payment API
 ↓
Transaction Retry
 ↓
Payment API again
```

可能导致：

```text
Double Charge
```

因此推荐：

```text
Domain Event
 ↓
Outbox
 ↓
Process Manager
 ↓
External Command
```

---

# 40. Transaction Idempotency

Command Idempotency 必须与业务状态保持一致。

错误模型：

```text
Mark Command Completed
 ↓
Execute Business
 ↓
Business Failure
```

会导致：

```text
Command appears completed
Business state not changed
```

---

# 41. 正确模型

```text
Transaction
 ├── Command Idempotency Record
 ├── Aggregate State
 └── Outbox
       │
       ▼
     COMMIT
```

三者成功提交。

---

# 42. Idempotency 状态

推荐：

```java
public enum IdempotencyStatus {

    PROCESSING,
    COMPLETED,
    FAILED
}
```

---

# 43. Idempotency Record

逻辑模型：

```java
public record IdempotencyRecord(
        String commandId,
        String commandType,
        IdempotencyStatus status,
        String resultReference
) {}
```

实际实现可以增加：

```text
tenantId
principalId
createdAt
completedAt
response
checksum
```

但这些不进入 Domain Core。

---

# 44. Command Idempotency Key

推荐：

```text
tenantId + commandId
```

如果系统没有多租户：

```text
commandId
```

必须保证唯一。

---

# 45. Transaction Propagation

定义抽象：

```java
public enum TransactionPropagation {

    REQUIRED,
    REQUIRES_NEW,
    MANDATORY,
    SUPPORTS,
    NOT_SUPPORTED
}
```

---

# 46. 默认传播策略

Command Handler：

```text
REQUIRED
```

含义：

```text
Existing Transaction
    ↓
Join

No Transaction
    ↓
Create
```

---

# 47. REQUIRES_NEW

语义：

```text
Outer Transaction
      │
      ├── suspended
      │
      ▼
 New Transaction
```

不应进入 Domain API。

适用于少量 Infrastructure/Application 场景。

例如：

```text
Audit Record
```

但必须谨慎使用。

---

# 48. Nested Transaction

默认不定义真正的：

```text
Nested Transaction
```

因为不同数据库/事务管理器行为不同。

推荐语义：

```text
Nested Application Call
        ↓
Join Existing Transaction
```

而不是：

```text
Nested = Independent Commit
```

---

# 49. Savepoint

Savepoint 属于：

```text
Infrastructure Capability
```

不是：

```text
Domain Capability
```

可以作为未来扩展：

```java
SavepointManager
```

但不进入 Domain API。

---

# 50. Isolation

框架定义语义级别：

```java
public enum TransactionIsolation {

    DEFAULT,
    READ_UNCOMMITTED,
    READ_COMMITTED,
    REPEATABLE_READ,
    SERIALIZABLE
}
```

---

# 51. 默认 Isolation

默认：

```text
DEFAULT
```

表示：

> 使用底层事务系统的默认隔离级别。

Framework 不强制所有数据库采用同一物理隔离实现。

---

# 52. 推荐 Isolation

一般业务：

```text
READ_COMMITTED
```

高一致性：

```text
REPEATABLE_READ
```

极高一致性：

```text
SERIALIZABLE
```

但：

```text
SERIALIZABLE
```

不能作为框架全局默认值。

---

# 53. READ_UNCOMMITTED

虽然规范允许表达：

```text
READ_UNCOMMITTED
```

但企业业务场景默认不推荐。

原因：

```text
Dirty Read
```

等风险。

---

# 54. Read-only Transaction

Query：

```text
readOnly = true
```

Command：

```text
readOnly = false
```

但：

> Read-only 是事务执行优化/约束语义，不是安全边界。

真正的禁止写入仍需要：

```text
Application policy
+
Infrastructure enforcement
```

---

# 55. Transaction Context

定义：

```java
public interface TransactionContext {

    boolean active();

    boolean readOnly();

    TransactionIsolation isolation();
}
```

---

# 56. Transaction Context 不进入 Domain

Domain 方法不得：

```java
void execute(TransactionContext context)
```

禁止。

Domain 应保持：

```java
order.pay();
```

而不是：

```java
order.pay(transactionContext);
```

---

# 57. Thread Context

同步请求：

```text
Thread
 ↓
TransactionContext
 ↓
UnitOfWork
```

可以使用：

```text
ThreadLocal
```

实现。

但：

> ThreadLocal 是实现方式，不是 Framework Domain API。

---

# 58. Async Boundary

事务不能默认跨越：

```text
Thread
```

或：

```text
Async Task
```

例如：

```java
executor.submit(() -> {
    // 不应自动继承父事务
});
```

---

# 59. Message Boundary

消息消费：

```text
Message
 ↓
Consumer
 ↓
Command
 ↓
Transaction
 ↓
Handler
```

每条消息应拥有自己的事务边界。

禁止：

```text
Transaction A
 ↓
Message Queue
 ↓
Transaction magically continues
```

---

# 60. Transaction Context Propagation

允许传播：

```text
TraceId
CorrelationId
CausationId
TenantId
PrincipalId
```

但不允许将：

```text
Physical DB Transaction
```

跨：

```text
Message
HTTP
RPC
Async
```

边界传播。

---

# 61. UnitOfWork 与 Event Collection

推荐：

```text
Aggregate
   ↓
Pending Events
   ↓
UnitOfWork
   ↓
Event Collector
```

定义：

```java
public interface DomainEventCollector {

    void collect(Object aggregate);

    List<Object> events();
}
```

实际 API 可以与 UnitOfWork 合并实现。

---

# 62. Commit Preparation

提交之前：

```text
1. Aggregate Validation
2. Version Check
3. Persistence Preparation
4. Domain Event Collection
5. Outbox Mapping
6. Outbox Persistence
7. Commit
```

---

# 63. Outbox Mapping

```text
Domain Event
      ↓
Event Mapper
      ↓
Integration Event
      ↓
Outbox Record
```

Domain Event 不直接成为：

```text
Broker Message
```

---

# 64. Outbox Atomicity

必须满足：

```text
Aggregate State
      +
Outbox Record
      +
Idempotency Record
```

在需要的情况下处于同一 Local Transaction。

---

# 65. Atomicity Formal Model

定义：

```text
A = Aggregate State
O = Outbox
I = Idempotency Record
T = Transaction
```

要求：

```text
T.commit
⇒
A ∧ O ∧ I
```

失败：

```text
¬T.commit
⇒
¬A ∧ ¬O ∧ ¬I
```

这里的 `I` 是否必须参与同一物理事务取决于基础设施，但 Framework 推荐在同一资源事务中完成。

---

# 66. Distributed Transaction Boundary

Framework 默认不提供：

```text
XA
2PC
Global Transaction
Distributed Transaction
```

作为 Domain Framework 的核心机制。

推荐：

```text
Local Transaction
+
Outbox
+
Idempotent Consumer
+
Saga / Process Manager
```

---

# 67. Saga 与 Transaction

Saga：

```text
Saga Step 1
  ↓
Local Transaction
  ↓
Event
  ↓
Saga Step 2
  ↓
Local Transaction
```

不是：

```text
Saga = Long Transaction
```

---

# 68. Failure Recovery

典型：

```text
Command
 ↓
Transaction
 ↓
Aggregate
 ↓
Outbox
 ↓
Commit
```

失败：

```text
Rollback
 ↓
Retry
```

如果最终失败：

```text
Command Failed
```

而不是：

```text
Partial Commit
```

---

# 69. Outbox Dispatcher Failure

数据库已经：

```text
COMMITTED
```

但是 Broker：

```text
FAILED
```

结果：

```text
Outbox = Pending
```

Dispatcher 重试。

因此：

```text
At-Least-Once
```

---

# 70. Outbox Dispatcher 不属于 Transaction

Dispatcher：

```text
load pending
 ↓
publish
 ↓
mark published
```

属于：

```text
Event Infrastructure
```

而不是：

```text
Domain
```

---

# 71. Exactly-Once

Framework 不宣称：

```text
End-to-End Exactly-Once
```

默认语义：

```text
At-Least-Once Delivery
```

通过：

```text
Idempotency
+
Deduplication
+
Inbox
```

获得：

```text
Effectively-Once Business Processing
```

---

# 72. Command Transaction 完整流程

```text
Command
  │
  ▼
CommandBus
  │
  ▼
Validation
  │
  ▼
Authorization
  │
  ▼
Idempotency
  │
  ▼
TransactionManager
  │
  ▼
UnitOfWork
  │
  ▼
CommandHandler
  │
  ▼
Aggregate
  │
  ▼
Repository
  │
  ▼
Domain Events
  │
  ▼
Outbox
  │
  ▼
Commit
  │
  ▼
Complete UnitOfWork
```

---

# 73. Query 流程

```text
Query
 ↓
QueryBus
 ↓
QueryMiddleware
 ↓
QueryHandler
 ↓
ReadRepository
 ↓
ReadModel
```

默认：

```text
No Write Transaction
```

---

# 74. TransactionBoundary API

推荐：

```java
public interface TransactionBoundary {

    <T> T execute(
            TransactionDefinition definition,
            TransactionCallback<T> callback
    );
}
```

---

# 75. TransactionDefinition

```java
public record TransactionDefinition(
        TransactionPropagation propagation,
        TransactionIsolation isolation,
        boolean readOnly
) {

    public static TransactionDefinition required() {
        return new TransactionDefinition(
                TransactionPropagation.REQUIRED,
                TransactionIsolation.DEFAULT,
                false
        );
    }

    public static TransactionDefinition readOnly() {
        return new TransactionDefinition(
                TransactionPropagation.SUPPORTS,
                TransactionIsolation.DEFAULT,
                true
        );
    }
}
```

---

# 76. Command Transaction Definition

默认：

```java
TransactionDefinition.required()
```

---

# 77. Query Transaction Definition

默认：

```java
TransactionDefinition.readOnly()
```

但具体 Query 是否启动物理 Transaction，由 Infrastructure 决定。

---

# 78. UnitOfWork Factory

```java
public interface UnitOfWorkFactory {

    UnitOfWork create();
}
```

---

# 79. UnitOfWork 不应成为第二个 Transaction Manager

禁止：

```text
UnitOfWork
 ├── beginTransaction()
 ├── commitTransaction()
 └── rollbackTransaction()
```

推荐：

```text
TransactionManager
 └── owns transaction

UnitOfWork
 └── owns business work lifecycle
```

---

# 80. Framework 自动化

最终推荐：

```java
transactionManager.execute(() ->
    handler.handle(command)
);
```

而不是：

```java
uow.begin();

try {
    handler.handle(command);
    uow.commit();
} catch (Exception e) {
    uow.rollback();
}
```

后者作为底层 Framework SPI 可以存在，但不作为普通开发者 API。

---

# 81. Repository Contract

Repository：

```java
public interface AggregateRepository<
        A,
        I extends Identifier> {

    Optional<A> findById(I id);

    void save(A aggregate);

    void delete(A aggregate);
}
```

它不定义：

```java
commit
rollback
transaction
```

---

# 82. Repository Concurrency Contract

Repository 实现必须：

```text
load version
 ↓
modify
 ↓
save expected version
 ↓
detect conflict
```

---

# 83. Aggregate Save Contract

对于：

```text
New Aggregate
```

执行：

```text
INSERT
```

对于：

```text
Existing Aggregate
```

执行：

```text
UPDATE WHERE id + expectedVersion
```

对于：

```text
Deleted Aggregate
```

执行：

```text
DELETE WHERE id + expectedVersion
```

---

# 84. Delete Concurrency

删除同样必须支持版本检查：

```text
DELETE
WHERE id = ?
AND version = ?
```

否则：

```text
Concurrent Update
```

可能被错误覆盖。

---

# 85. Lost Update Prevention

必须防止：

```text
User A loads version 10
User B loads version 10

A → version 11
B → version 11
```

B 不得成功覆盖 A。

必须：

```text
B expectedVersion = 10
ActualVersion = 11

→ Conflict
```

---

# 86. Transaction Retry 与 Version

发生：

```text
ConcurrencyConflict
```

Retry 时必须重新读取 Aggregate：

```text
Old Aggregate
   ↓
Conflict
   ↓
Discard
   ↓
Reload
   ↓
Reapply Command
```

禁止：

```text
same stale Aggregate
 ↓
retry save
```

---

# 87. Business Command Retry

重新执行 Command 前必须判断：

```text
Command semantics
```

例如：

```text
IncrementBalance
```

可以重新计算。

但是：

```text
CreateExternalPayment
```

必须使用独立 Idempotency。

---

# 88. Transaction Exception Taxonomy

推荐：

```text
TransactionException
 ├── TransactionBeginException
 ├── TransactionCommitException
 ├── TransactionRollbackException
 ├── ConcurrencyConflictException
 └── TransientTransactionException
```

---

# 89. Domain Exception 与 Transaction Exception

严格分离：

```text
DomainException
    ↓
Business failure
```

```text
TransactionException
    ↓
Execution infrastructure failure
```

二者不得混合。

---

# 90. Error Mapping

Interface 层可以将：

```text
ConcurrencyConflict
```

映射为：

```text
409 Conflict
```

而：

```text
DomainRuleViolation
```

可以映射为：

```text
400 / 422
```

具体协议映射不进入 Domain。

---

# 91. Observability

Transaction Framework 应支持：

```text
transaction.id
transaction.status
transaction.duration
transaction.retry
transaction.isolation
transaction.readOnly
```

但这些属于：

```text
Infrastructure / Observability
```

不是 Domain Model。

---

# 92. Correlation

Transaction 可以关联：

```text
traceId
correlationId
causationId
commandId
```

但：

```text
TransactionContext
```

不得污染 Aggregate API。

---

# 93. Thread Safety

Aggregate 默认：

```text
NOT THREAD SAFE
```

UnitOfWork 默认：

```text
NOT THREAD SAFE
```

一个 UnitOfWork 不应同时被多个线程操作。

---

# 94. Parallelism

如果需要并行：

```text
Command
 ├── Task A
 ├── Task B
 └── Task C
```

每个任务：

```text
独立 UnitOfWork
独立 Transaction
```

最终通过：

```text
Command
Saga
Process Manager
```

协调。

---

# 95. Async Command

异步 Command：

```text
Message
 ↓
Command
 ↓
New Transaction
 ↓
Handler
```

不能：

```text
Original Transaction
        ↓
Async Command
```

---

# 96. Transaction Timeout

框架可以提供：

```java
Duration timeout();
```

但：

> Timeout 的最终执行语义由 Infrastructure 实现。

Domain 不感知 timeout。

---

# 97. Transaction Boundary 与 Authorization

正确顺序：

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

如果 Authorization 不依赖数据库写操作，可以在事务之前完成。

---

# 98. Transaction Boundary 与 Idempotency

推荐：

```text
Idempotency Check
 ↓
Transaction
 ↓
Business Execution
 ↓
Idempotency Completion
 ↓
Commit
```

如果需要写入 Idempotency Store：

```text
Idempotency
+
Aggregate
+
Outbox
```

尽量保持同一 Local Transaction。

---

# 99. Transaction Boundary 与 Outbox

必须：

```text
Transaction
 ├── Aggregate Persistence
 └── Outbox Persistence
```

而不是：

```text
Transaction
 └── Aggregate

After Commit
 └── create Outbox
```

后者可能产生：

```text
Aggregate committed
Outbox lost
```

---

# 100. Transactional Event Publication

默认禁止：

```text
Domain
 ↓
Broker
```

也禁止：

```text
Repository.save()
 ↓
Broker.publish()
```

推荐：

```text
Repository
 ↓
Outbox
 ↓
Commit
 ↓
Dispatcher
 ↓
Broker
```

---

# 101. Commit Callback

Framework 可以提供：

```java
public interface TransactionSynchronization {

    void beforeCommit();

    void afterCommit();

    void afterRollback();
}
```

但这些 API：

```text
Application / Infrastructure
```

可见。

Domain 不应依赖。

---

# 102. afterCommit 的语义

`afterCommit()` 可以用于：

```text
metrics
cache invalidation trigger
local notification
dispatcher wake-up
```

但不应依赖它保证：

```text
Business Event Durability
```

真正可靠事件必须已经进入：

```text
Outbox
```

---

# 103. Cache 与 Transaction

禁止 Domain：

```text
aggregate.update();
cache.put();
```

推荐：

```text
Transaction Commit
 ↓
Outbox / Event
 ↓
Cache Invalidation
```

或者由 Infrastructure 使用：

```text
afterCommit
```

处理。

---

# 104. Consistency Model

Framework 定义三种主要一致性：

## Strong Local Consistency

```text
Aggregate
+
Local Transaction
```

用于：

```text
Aggregate Invariants
```

---

## Eventual Consistency

```text
Aggregate
 ↓
Domain Event
 ↓
Outbox
 ↓
Consumer
 ↓
Projection
```

用于：

```text
Read Model
Cross Context
Integration
```

---

## Workflow Consistency

```text
Saga / Process Manager
```

用于：

```text
Multi-Aggregate
Multi-Context
Long Running Process
```

---

# 105. Aggregate Invariant 与 Transaction

必须保证：

```text
Aggregate Invariant
```

在：

```text
Single Aggregate Transaction
```

内完成。

---

# 106. 跨 Aggregate Invariant

如果要求：

```text
Aggregate A
+
Aggregate B
```

同步保持一致，应首先重新评估：

> 是否错误地划分了 Aggregate Boundary。

不要默认使用：

```text
Distributed Transaction
```

解决。

---

# 107. Cross Aggregate Workflow

推荐：

```text
Aggregate A
 ↓
Domain Event
 ↓
Process Manager
 ↓
Command
 ↓
Aggregate B
```

---

# 108. UnitOfWork 不跨 Command

禁止：

```text
Command A
 ↓
UoW
 ↓
Command B
 ↓
same UoW
```

默认：

```text
Command A → UoW A

Command B → UoW B
```

---

# 109. UnitOfWork 不跨 Message

禁止：

```text
Message A
 ↓
UoW
 ↓
Message B
```

每条消息：

```text
Message
 ↓
New UoW
 ↓
New Transaction
```

---

# 110. UnitOfWork 不跨 HTTP Request

默认：

```text
HTTP Request
 ↓
Application Operation
 ↓
UoW
 ↓
Commit
```

请求结束：

```text
UoW closed
```

---

# 111. Long Running Operation

禁止：

```text
HTTP Request
 ↓
Long Business Process
 ↓
One giant Transaction
```

改为：

```text
Process
 ├── Transaction 1
 ├── Transaction 2
 ├── Transaction 3
 └── Transaction N
```

通过：

```text
Saga
```

保持业务流程。

---

# 112. Transaction Boundary Formal Rules

## TX-001

所有 Command 默认必须具有明确的 Transaction Boundary。

## TX-002

Domain 不得依赖 Transaction API。

## TX-003

TransactionManager 负责事务生命周期。

## TX-004

UnitOfWork 负责业务工作生命周期。

## TX-005

Repository 不负责 Commit。

## TX-006

Repository 不负责 Rollback。

## TX-007

Aggregate 不负责 Commit。

## TX-008

Aggregate 不负责 Publish Event。

## TX-009

Aggregate State 与 Outbox Record 必须在同一 Local Transaction 中持久化。

## TX-010

Domain Event 不得在 Transaction Commit 前发送至外部 Broker。

## TX-011

Command Idempotency 不得在业务提交前错误标记为 Completed。

## TX-012

Optimistic Concurrency 必须使用 Aggregate Version。

## TX-013

Concurrency Conflict 不得被当作 Domain Business Error。

## TX-014

Retry 不得无限执行。

## TX-015

Retry 不得默认应用于 Domain Business Exception。

## TX-016

Retry 后必须重新加载 Aggregate。

## TX-017

Transaction 不得跨 Async Boundary 隐式传播。

## TX-018

Transaction 不得跨 Message Boundary 隐式传播。

## TX-019

Transaction 不得跨 HTTP/RPC Boundary 隐式传播。

## TX-020

UnitOfWork 不得跨 Command。

## TX-021

UnitOfWork 不得跨 Message。

## TX-022

UnitOfWork 默认不是线程安全对象。

## TX-023

Query 默认不需要 Write Transaction。

## TX-024

Read-only Transaction 不等价于业务安全控制。

## TX-025

Nested Transaction 默认采用 Join Existing Transaction 语义。

## TX-026

REQUIRES_NEW 不得进入 Domain API。

## TX-027

Savepoint 属于 Infrastructure Capability。

## TX-028

Distributed Transaction 不是 Framework Core 默认能力。

## TX-029

Saga 不等于 Distributed Transaction。

## TX-030

Saga 不等于 Long Running Transaction。

---

# 113. Consistency Rules

## CONS-001

Aggregate Invariant 必须由 Aggregate 自身维护。

## CONS-002

Aggregate 内部状态变化必须保持原子性。

## CONS-003

跨 Aggregate 一致性默认采用最终一致性。

## CONS-004

跨 Context 一致性默认采用 Event + Process Manager/Saga。

## CONS-005

Read Model 默认允许最终一致性。

## CONS-006

Projection 必须支持幂等。

## CONS-007

Projection 不得修改 Write Model。

## CONS-008

Event Replay 不得默认执行不可逆副作用。

## CONS-009

Outbox 必须具备可靠持久化语义。

## CONS-010

Consumer 必须支持重复消息。

---

# 114. Concurrency Rules

## CON-001

Aggregate 必须具有可识别版本。

## CON-002

更新必须检查 Expected Version。

## CON-003

删除必须检查 Expected Version。

## CON-004

Version Conflict 必须显式报告。

## CON-005

Conflict Retry 必须重新 Load Aggregate。

## CON-006

Framework 不得静默覆盖并发修改。

---

# 115. Failure Rules

## FAIL-001

Commit Failure 必须导致当前 UnitOfWork 失败。

## FAIL-002

Rollback 后不得继续提交当前 UnitOfWork。

## FAIL-003

Commit 后不得执行 Rollback 语义。

## FAIL-004

Outbox Publish Failure 不得回滚已经提交的 Aggregate Transaction。

## FAIL-005

Outbox Publish Failure 必须由 Dispatcher Retry 机制处理。

## FAIL-006

外部副作用不得依赖数据库事务回滚保证。

---

# 116. API Boundary Rules

## API-001

Core 不依赖 Transaction。

## API-002

Domain 不依赖 Transaction。

## API-003

Domain 不依赖 UnitOfWork API。

## API-004

Application 可以依赖 Transaction Abstraction。

## API-005

Infrastructure 实现 Transaction Abstraction。

## API-006

Interface Layer 不直接操作 UnitOfWork。

---

# 117. Module Architecture

Phase VI 对 Maven 模块进行进一步收敛。

最终建议：

```text
io.github.regalpine.ddd
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

---

# 118. Module Dependency

核心依赖方向：

```text
ddd-core
   ↑
ddd-domain
   ↑
ddd-application
   ↑
ddd-cqrs
```

Transaction：

```text
ddd-transaction
   ↓
Application / Domain abstractions
```

Infrastructure：

```text
ddd-infrastructure
 ├── ddd-domain
 ├── ddd-application
 ├── ddd-cqrs
 ├── ddd-event
 └── ddd-transaction
```

Spring Boot：

```text
ddd-spring-boot
        ↓
ddd-infrastructure
ddd-cqrs
ddd-transaction
```

---

# 119. ddd-port 的治理规则

Phase VI 对 `ddd-port` 做明确限制。

`ddd-port` **不是万能接口仓库**。

禁止：

```text
everything → ddd-port
```

`ddd-port` 只放：

> 具有独立跨边界生命周期、且无法合理归属于 Domain/Application/Transaction/Event 的通用 Port。

---

# 120. Repository 的最终位置

Repository Contract：

```text
ddd-domain
```

例如：

```text
ddd-domain
 └── repository
      └── AggregateRepository
```

而不是：

```text
ddd-port.repository
```

原因：

> Repository 是 Domain 对持久化能力提出的抽象。

---

# 121. Transaction Port 的最终位置

Transaction：

```text
ddd-transaction
```

因为：

```text
Transaction
```

不是 Domain Business Concept。

它属于：

```text
Application Infrastructure Boundary
```

---

# 122. Event Infrastructure

Domain Event：

```text
ddd-domain
```

Event infrastructure：

```text
ddd-event
```

Outbox：

```text
ddd-event / ddd-infrastructure
```

具体数据库实现：

```text
ddd-infrastructure
```

---

# 123. Maven Dependency Matrix

| Module | core | domain | application | cqrs | event | transaction | infrastructure |
|---|---:|---:|---:|---:|---:|---:|---:|
| ddd-core | - | | | | | | |
| ddd-domain | ✓ | - | | | | | |
| ddd-application | ✓ | ✓ | - | | | | |
| ddd-cqrs | ✓ | ✓ | ✓ | - | | | |
| ddd-event | ✓ | ✓ | | | - | | |
| ddd-transaction | ✓ | optional | ✓ | | | - | |
| ddd-infrastructure | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | - |

原则：

```text
Dependency Direction → inward
```

---

# 124. 禁止循环依赖

禁止：

```text
ddd-domain
    ↓
ddd-application
    ↓
ddd-domain
```

禁止：

```text
ddd-transaction
    ↓
ddd-infrastructure
    ↓
ddd-transaction
```

Infrastructure 只能实现 abstraction。

---

# 125. Spring Boot Boundary

`ddd-spring-boot` 可以提供：

```text
@Transactional Adapter
TransactionManager Adapter
UnitOfWork Integration
Repository Auto Configuration
```

但：

```text
ddd-core
ddd-domain
ddd-application
```

不得依赖 Spring。

---

# 126. Java 17 Example

完整业务结构：

```java
public final class PayOrderHandler
        implements CommandHandler<PayOrderCommand, PaymentResult> {

    private final OrderRepository repository;

    @Override
    public PaymentResult handle(PayOrderCommand command) {

        Order order = repository
                .findById(command.orderId())
                .orElseThrow();

        order.pay();

        repository.save(order);

        return new PaymentResult(order.id());
    }
}
```

Handler 不需要：

```java
transaction.commit();
```

---

# 127. Application Execution

```java
transactionManager.execute(() ->
    commandHandler.handle(command)
);
```

Framework 负责：

```text
Transaction
+
UnitOfWork
+
Repository lifecycle
+
Outbox
+
Commit
```

---

# 128. Framework Internal Execution

推荐最终模型：

```java
public <R> R execute(Command<R> command) {

    return transactionManager.execute(() -> {

        UnitOfWork uow = unitOfWorkManager.begin();

        try {
            R result = handler.handle(command);

            uow.commit();

            return result;

        } catch (RuntimeException e) {

            uow.rollback();

            throw e;
        }
    });
}
```

实际实现中：

```text
commit
rollback
close
```

应由 Framework Lifecycle Manager 统一控制，而不是要求用户手工管理。

---

# 129. Recommended Internal Lifecycle

更严格的实现：

```text
CommandBus
   ↓
TransactionInterceptor
   ↓
UoWInterceptor
   ↓
CommandHandler
   ↓
UoW prepare
   ↓
Transaction commit
   ↓
UoW complete
```

---

# 130. Final Command Lifecycle

```text
                    Command
                       │
                       ▼
                 CommandBus
                       │
                       ▼
                  Middleware
                       │
              ┌────────┴────────┐
              │                 │
         Authorization      Idempotency
              │                 │
              └────────┬────────┘
                       ▼
                Transaction
                       │
                       ▼
                  UnitOfWork
                       │
                       ▼
                CommandHandler
                       │
                       ▼
                  Aggregate
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
       Repository            Domain Event
             │                   │
             │                   ▼
             │                Outbox
             │                   │
             └─────────┬─────────┘
                       ▼
                    Commit
                       │
                       ▼
                  UoW Complete
                       │
                       ▼
                Command Result
```

---

# 131. Phase VI Architectural Invariants

整个框架必须保持：

```text
Domain
  ↓
Business Behavior

Application
  ↓
Use Case + Transaction Boundary

Infrastructure
  ↓
Physical Transaction

UnitOfWork
  ↓
Business Work Lifecycle

Repository
  ↓
Persistence Contract

Outbox
  ↓
Reliable Event Delivery

CQRS
  ↓
Read/Write Responsibility Separation
```

---

# 132. 关键设计决策冻结

Phase VI 对以下设计进行冻结：

### Decision 01

```text
Transaction ≠ UnitOfWork
```

### Decision 02

```text
TransactionManager owns Transaction
```

### Decision 03

```text
UnitOfWork owns Aggregate lifecycle
```

### Decision 04

```text
Repository does not own Transaction
```

### Decision 05

```text
Domain is Transaction-Unaware
```

### Decision 06

```text
Aggregate + Outbox = Same Local Transaction
```

### Decision 07

```text
Optimistic Concurrency = Default Aggregate Concurrency Strategy
```

### Decision 08

```text
At-Least-Once = Default Event Delivery
```

### Decision 09

```text
Idempotent Consumer = Required
```

### Decision 10

```text
Saga = Cross-Transaction Workflow
```

### Decision 11

```text
Distributed Transaction ≠ Default
```

### Decision 12

```text
CQRS ≠ Event Sourcing
```

---

# 133. Phase VI Completion Matrix

| Capability | Status |
|---|---|
| Transaction Model | ✅ |
| Transaction Boundary | ✅ |
| TransactionManager | ✅ |
| UnitOfWork | ✅ |
| UnitOfWork Lifecycle | ✅ |
| Aggregate Tracking | ✅ |
| Explicit Persistence | ✅ |
| Commit | ✅ |
| Rollback | ✅ |
| Optimistic Concurrency | ✅ |
| Aggregate Version | ✅ |
| Concurrency Conflict | ✅ |
| Retry | ✅ |
| Transaction Propagation | ✅ |
| Nested Transaction Semantics | ✅ |
| Isolation | ✅ |
| Read-only Transaction | ✅ |
| Transaction Context | ✅ |
| Async Boundary | ✅ |
| Message Boundary | ✅ |
| Outbox Atomicity | ✅ |
| Idempotency Atomicity | ✅ |
| Event Flush Semantics | ✅ |
| Failure Recovery | ✅ |
| Saga Boundary | ✅ |
| Distributed Transaction Boundary | ✅ |
| Module Dependency | ✅ |
| Java 17 API Direction | ✅ |

---

# 134. Phase VI 与前五阶段关系

完整架构现在形成：

```text
Phase I
Ontology / DDD Conceptual Foundation
        │
        ▼
Phase II
ddd-core
        │
        ▼
Phase III
ddd-domain
        │
        ▼
Phase IV
ddd-application
        │
        ▼
Phase V
CQRS + Event Architecture
        │
        ▼
Phase VI
Transaction + UnitOfWork + Consistency
```

最终 Command 主链：

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

已经形成完整闭环。

---

# 135. Phase VI 收敛判断

Phase VI 之后：

> Transaction、UnitOfWork、Aggregate、Repository、CQRS、Domain Event、Outbox、Idempotency、Concurrency 的核心语义已经闭合。

后续阶段不应重新设计这些核心概念。

后续只能：

```text
Implementation
Adapter
Extension
Optimization
Conformance Test
```

不得再次修改核心语义，除非发现明确的架构矛盾。

---

# 136. 下一阶段

下一阶段进入：

# Phase VII — Persistence & Repository Adapter Specification

重点：

```text
Aggregate Repository
Persistence Model
Mapping
Identity Mapping
Version Mapping
Database Adapter
Write Repository
Read Repository
Query Repository
Pagination
Specification Translation
Connection Management
ORM Adapter Boundary
JDBC Adapter Boundary
SQL Mapping
Persistence Error Mapping
Schema Evolution
Migration
Multi-Tenancy Boundary
Soft Delete
Audit Fields
```

核心目标：

```text
Domain Aggregate
      │
      ▼
Repository Contract
      │
      ▼
Persistence Adapter
      │
      ├── JDBC
      ├── JPA
      ├── MyBatis
      └── Other Adapter
```

并确保：

```text
Domain
    不依赖 ORM

Domain
    不依赖 Database

Repository Contract
    不暴露 Persistence Model

Persistence Model
    不污染 Domain Model
```

---

# 137. Phase VII 前的架构冻结点

进入 Phase VII 后，以下内容原则上冻结：

```text
DDD Core
Aggregate
Entity
Value Object
Domain Event
Repository Contract
Application Command
Application Query
CommandBus
QueryBus
CQRS
UnitOfWork
Transaction
Optimistic Concurrency
Outbox
Idempotency
Saga Boundary
```

Phase VII 只解决：

> **这些抽象如何可靠落地到持久化基础设施。**

而不再重新讨论：

> **这些抽象应该是什么。**

---

# 138. Framework 总体结构

Phase VI 完成后，RegalPine DDD Framework 的目标架构为：

```text
                         ┌──────────────────────┐
                         │     Interface        │
                         │ REST / RPC / Message  │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │    Application       │
                         │ Command / Query      │
                         │ Handler / UseCase    │
                         └──────────┬───────────┘
                                    │
                  ┌─────────────────┼─────────────────┐
                  │                 │                 │
                  ▼                 ▼                 ▼
             Transaction          CQRS            UoW
                  │                 │                 │
                  └─────────────────┼─────────────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │       Domain         │
                         │ Aggregate / Entity   │
                         │ VO / Service / Event │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     Repository       │
                         │       Contract       │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │   Infrastructure     │
                         │ DB / ORM / Outbox    │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ External Systems     │
                         │ Broker / Cache / API  │
                         └──────────────────────┘
```

**Phase VI 状态：Core Semantics Frozen。**