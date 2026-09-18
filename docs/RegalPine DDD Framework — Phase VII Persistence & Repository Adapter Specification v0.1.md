# RegalPine DDD Framework

## Phase VII — Persistence & Repository Adapter Specification

**Version:** v0.1  
**Group ID:** `io.github.regalpine.ddd`  
**Java:** 17+  
**Build:** Maven  
**Architecture:** DDD + Hexagonal Architecture + CQRS  
**Status:** Framework Specification  
**Scope:** Persistence / Repository / Mapping / Concurrency / SQL / ORM Adapter / Read Model / Schema Evolution

---

# 1. 文档定位

Phase VII 解决一个核心问题：

> Domain 中定义的 Aggregate、Entity、Value Object、Repository Contract，如何可靠地落地到数据库等持久化技术。

本阶段不重新定义：

- Aggregate
- Entity
- Value Object
- Domain Event
- Transaction
- UnitOfWork
- CQRS
- Outbox
- Idempotency

这些概念已经在前六阶段冻结。

Phase VII 只定义：

```text
Domain Model
      ↓
Repository Contract
      ↓
Persistence Adapter
      ↓
Persistence Model
      ↓
Database
```

---

# 2. 核心原则

必须保持：

```text
Domain Model
    ≠
Persistence Model
```

以及：

```text
Domain Repository
    ≠
Database DAO
```

---

# 3. 六边形架构位置

```text
                    ┌──────────────────┐
                    │    Interface     │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │   Application    │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │     Domain       │
                    │    Aggregate     │
                    └────────┬─────────┘
                             │
                       Repository
                         Contract
                             │
                             ▼
                    ┌──────────────────┐
                    │ Persistence Port │
                    │     Adapter      │
                    └────────┬─────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
             JDBC           JPA           MyBatis
              │              │              │
              └──────────────┼──────────────┘
                             ▼
                         Database
```

---

# 4. Repository 的最终定位

Repository 是：

> Domain 对 Aggregate 持久化能力提出的抽象。

Repository 不是：

- ORM DAO
- SQL Executor
- Table Gateway
- Generic CRUD Repository
- Database Session

---

# 5. Repository Contract

推荐：

```java
public interface AggregateRepository<
        A extends AggregateRoot<I>,
        I extends Identifier> {

    Optional<A> findById(I id);

    void save(A aggregate);

    void delete(A aggregate);
}
```

---

# 6. Repository 的 Aggregate Boundary

Repository 必须围绕 Aggregate。

推荐：

```text
OrderRepository
CustomerRepository
ProductRepository
```

不推荐：

```text
GenericRepository<T>
```

作为 Domain 默认抽象。

---

# 7. Generic CRUD Repository 问题

以下 API 不应成为核心 Domain Contract：

```java
interface GenericRepository<T, ID> {

    T find(ID id);

    void save(T entity);

    void delete(T entity);

    List<T> findAll();
}
```

原因：

1. 暴露 CRUD 思维
2. 忽略 Aggregate Boundary
3. 容易绕过业务行为
4. 容易形成贫血模型
5. 无法表达领域语义

---

# 8. Persistence Model

基础设施可以定义：

```java
public final class OrderRecord {

    private String id;

    private String customerId;

    private long version;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;
}
```

但：

```text
OrderRecord
```

不得成为：

```text
Domain Order
```

的替代品。

---

# 9. Domain Model

例如：

```java
public final class Order
        extends AggregateRoot<OrderId> {

    private OrderStatus status;

    public void pay() {
        // business rule
    }
}
```

Domain 不应该包含：

```java
@Table(...)
@Entity
@Column(...)
@JoinColumn(...)
```

---

# 10. Persistence Mapper

定义：

```java
public interface PersistenceMapper<D, P> {

    P toPersistence(D domain);

    D toDomain(P persistence);
}
```

其中：

```text
D = Domain Model
P = Persistence Model
```

---

# 11. Mapping Direction

读取：

```text
Database
 ↓
Persistence Model
 ↓
Mapper
 ↓
Domain Aggregate
```

写入：

```text
Domain Aggregate
 ↓
Mapper
 ↓
Persistence Model
 ↓
Database
```

---

# 12. Mapping Responsibility

Mapper 负责：

- Identity Mapping
- Value Object Mapping
- State Mapping
- Version Mapping
- Child Entity Mapping
- Collection Mapping

Mapper 不负责：

- Business Rule
- Authorization
- Transaction
- Event Publishing

---

# 13. Aggregate Reconstruction

数据库记录：

```text
OrderRecord
CustomerRecord
OrderItemRecord
```

可以重建：

```text
Order Aggregate
 ├── OrderId
 ├── CustomerId
 ├── OrderStatus
 └── OrderItems
```

但必须满足 Aggregate Boundary。

---

# 14. Aggregate Reconstruction Rule

Persistence Adapter 不得通过：

```text
Order
Customer
Product
```

多个 Repository 分别加载然后在 Application 层拼成一个 Aggregate。

如果这些对象属于一个 Aggregate：

> 应由该 Aggregate Repository 一次性恢复。

---

# 15. Aggregate Boundary 与表结构

一个 Aggregate：

```text
Order
 ├── OrderItem
 └── ShippingAddress
```

不意味着必须只有一张表。

可以：

```text
orders
order_items
shipping_addresses
```

但是：

```text
Aggregate Boundary
```

仍然是：

```text
Order
```

---

# 16. Aggregate Persistence

推荐：

```text
OrderRepository.save(order)
```

内部可以执行：

```text
UPDATE orders
INSERT order_items
UPDATE shipping_address
```

业务层无需知道这些 SQL。

---

# 17. Persistence Adapter

例如：

```java
public final class JdbcOrderRepository
        implements OrderRepository {

    private final OrderMapper mapper;
    private final OrderJdbcStore store;

    @Override
    public Optional<Order> findById(OrderId id) {

        return store.find(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public void save(Order order) {

        store.save(mapper.toPersistence(order));
    }
}
```

---

# 18. Repository Adapter 与 Transaction

Repository：

```text
执行 Persistence Operation
```

Transaction：

```text
控制 Transaction Boundary
```

因此：

```text
JdbcOrderRepository
```

不得自行：

```java
connection.commit();
```

---

# 19. Connection Ownership

Database Connection 的生命周期由：

```text
Infrastructure Transaction Manager
```

负责。

Repository 只使用当前 Persistence Context。

---

# 20. JDBC Adapter

Framework 可以提供：

```text
ddd-infrastructure-jdbc
```

或者在基础设施实现中提供 JDBC Adapter。

核心依赖：

```text
JDBC
```

不能进入：

```text
ddd-core
ddd-domain
```

---

# 21. JPA Adapter

可以提供：

```text
ddd-infrastructure-jpa
```

但是 JPA：

```text
@Entity
@Embeddable
@OneToMany
```

只允许存在于：

```text
Persistence Model
```

---

# 22. ORM Boundary

禁止：

```text
Domain
 ↓
Hibernate Proxy
```

Domain 应始终是：

```text
Plain Java Object
```

---

# 23. Lazy Loading

Domain 不应依赖：

```text
lazy loading
```

例如：

```java
order.getItems();
```

不应该在业务代码执行时触发数据库访问。

---

# 24. Persistence Completeness

Repository 返回 Aggregate 时：

> Aggregate 必须处于业务可用状态。

不允许：

```text
Order
 ├── basic fields loaded
 └── items unloaded
```

然后 Domain 行为依赖数据库 Lazy Loading。

---

# 25. N+1 问题

Aggregate Repository 应避免：

```text
Load Order
 ↓
Load Item 1
 ↓
Load Item 2
 ↓
Load Item N
```

Repository 应根据 Aggregate Mapping 使用：

```text
Join
Batch Query
Separate Bulk Query
```

完成一次 Aggregate Reconstruction。

---

# 26. Aggregate Size

如果一个 Aggregate：

```text
10,000+ child entities
```

导致：

- 巨大事务
- 巨大 UPDATE
- 巨大内存
- 巨大锁范围

则优先重新评估：

```text
Aggregate Boundary
```

而不是单纯优化 ORM。

---

# 27. Optimistic Concurrency

Persistence Adapter 必须实现：

```text
Expected Version
```

更新：

```sql
UPDATE orders
SET
    status = ?,
    version = version + 1
WHERE
    id = ?
AND
    version = ?
```

---

# 28. Version Result

如果：

```text
affectedRows = 1
```

则：

```text
Success
```

如果：

```text
affectedRows = 0
```

则：

```text
ConcurrencyConflict
```

---

# 29. Insert Version

新 Aggregate：

```text
version = 0
```

或：

```text
version = 1
```

Framework 不强制具体起始数字，但必须统一。

推荐：

```text
Initial Version = 0
```

成功第一次持久化：

```text
version = 1
```

---

# 30. Delete Version

删除：

```sql
DELETE FROM orders
WHERE id = ?
AND version = ?
```

同样必须检测：

```text
affectedRows
```

---

# 31. Version 不属于业务语义

例如：

```text
Order.version
```

表示：

```text
Persistence Concurrency Version
```

而不是：

```text
Order Revision
```

或者：

```text
Business Version
```

---

# 32. Identifier Mapping

Domain：

```java
public record OrderId(String value)
        implements Identifier {
}
```

Persistence：

```text
VARCHAR
UUID
BIGINT
```

均可。

Mapper：

```text
OrderId
 ↕
Database Identifier
```

负责转换。

---

# 33. Identifier 不应绑定数据库

Domain 不应该规定：

```text
UUID database type
```

或者：

```text
PostgreSQL UUID
```

Domain 只要求：

```text
Identity
```

---

# 34. Value Object Mapping

例如：

```java
public record Money(
        BigDecimal amount,
        Currency currency
) {}
```

可以映射：

```text
amount
currency
```

两个数据库字段。

也可以：

```text
JSON
```

或者：

```text
embedded columns
```

由 Persistence Adapter 决定。

---

# 35. Value Object Mapping Rule

必须保持：

```text
Database Representation
        ↓
Value Object
```

而不是：

```text
Database Primitive
        ↓
Domain Primitive Everywhere
```

---

# 36. Enum Mapping

Domain：

```java
enum OrderStatus {
    CREATED,
    PAID,
    SHIPPED
}
```

数据库可以使用：

```text
VARCHAR
```

推荐保存稳定业务码：

```text
CREATED
PAID
SHIPPED
```

不要依赖：

```text
ordinal()
```

---

# 37. Date / Time Mapping

Domain 使用：

```java
Instant
LocalDate
LocalDateTime
ZonedDateTime
```

必须明确语义。

例如：

```text
createdAt
```

如果表示绝对时间：

```text
Instant
```

优先。

---

# 38. Time Zone

Persistence 层必须明确：

```text
UTC
```

或：

```text
business timezone
```

不能隐式依赖服务器默认时区。

---

# 39. Soft Delete

Soft Delete 是 Persistence Policy，不是默认 Domain Behavior。

可以：

```text
deleted_at
```

但 Domain 不应被迫包含：

```java
boolean deleted;
```

除非这是明确的业务概念。

---

# 40. Soft Delete Repository

Repository 可以自动过滤：

```sql
WHERE deleted_at IS NULL
```

但必须明确：

```text
Persistence Policy
```

而不是：

```text
Domain Invariant
```

---

# 41. Hard Delete

如果 Domain 要求：

```text
delete(order)
```

Infrastructure 可以：

```sql
DELETE
```

或者：

```text
UPDATE deleted_at
```

但这种差异必须由 Persistence Policy 定义。

---

# 42. Audit Fields

数据库常见：

```text
created_at
created_by
updated_at
updated_by
```

这些不应自动成为 Domain Entity 字段。

如果：

```text
createdBy
```

是业务概念，则可以进入 Domain。

否则属于：

```text
Infrastructure / Audit
```

---

# 43. Tenant Boundary

多租户系统必须在 Persistence 层支持：

```text
tenant_id
```

例如：

```sql
SELECT *
FROM orders
WHERE tenant_id = ?
AND id = ?
```

---

# 44. Tenant Context

Tenant Context 可以位于：

```text
Application / Infrastructure
```

Domain 不需要：

```java
orderRepository.findById(tenantId, orderId);
```

除非 Tenant 本身是业务语义的一部分。

---

# 45. Tenant Isolation

Persistence Adapter 必须保证：

```text
Tenant A
    ↓
cannot access
    ↓
Tenant B
```

不能只依赖 Interface 层过滤。

---

# 46. Tenant ID 注入

推荐：

```text
Application Context
        ↓
Transaction Context
        ↓
Persistence Adapter
```

由 Infrastructure 自动加入查询条件。

---

# 47. Multi-Tenant Isolation Strategies

支持：

```text
Shared Database
Shared Schema
Shared Tables
```

或者：

```text
Separate Schema
```

或者：

```text
Separate Database
```

但 Domain Model 不随策略改变。

---

# 48. Read / Write Repository

CQRS 下：

```text
Write Repository
```

与：

```text
Read Repository
```

应分离。

Write：

```text
Aggregate
```

Read：

```text
Projection / ReadModel
```

---

# 49. Write Repository

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    void save(Order order);

    void delete(Order order);
}
```

---

# 50. Read Repository

```java
public interface OrderReadRepository {

    Optional<OrderSummary> findSummary(OrderId id);

    Page<OrderSummary> search(OrderSearchQuery query);
}
```

Read Repository 不返回：

```text
Domain Aggregate
```

而返回：

```text
Read Model
```

---

# 51. Read Model

例如：

```java
public record OrderSummary(
        String orderId,
        String customerName,
        String status,
        BigDecimal amount
) {}
```

可以直接对应：

```text
SQL View
Projection Table
Materialized View
Document
Cache
```

---

# 52. Read Model 不需要 Domain Mapper

例如：

```text
SQL
 ↓
OrderSummary
```

可以直接映射。

不必：

```text
SQL
 ↓
Persistence Model
 ↓
Domain Aggregate
 ↓
Read Model
```

---

# 53. CQRS Persistence Architecture

```text
                Application
                     │
            ┌────────┴────────┐
            ▼                 ▼
        Command            Query
            │                 │
            ▼                 ▼
      Write Repository   Read Repository
            │                 │
            ▼                 ▼
       Write Store       Read Store
            │                 │
            └────────┬────────┘
                     │
                  Events
                     │
                     ▼
                Projection
```

---

# 54. Physical Database Separation

CQRS 不要求：

```text
Write DB
≠
Read DB
```

可以：

```text
Same DB
```

也可以：

```text
Separate DB
```

Framework 不强制。

---

# 55. Specification Persistence

Domain Specification：

```java
Specification<Order>
```

不应该直接暴露：

```text
SQL
```

---

# 56. Specification Translation

如果需要数据库查询优化：

```text
Domain Specification
        ↓
Persistence Specification Translator
        ↓
SQL Predicate
```

这是 Adapter 能力。

---

# 57. 不支持翻译时

如果某个 Specification 无法安全转换成数据库查询：

禁止偷偷：

```text
Load entire table
 ↓
Java filter
```

除非明确允许。

推荐：

```text
UnsupportedQueryException
```

或者由 Application 明确选择另一种查询方式。

---

# 58. Pagination

Pagination 属于：

```text
Application / Query
```

和：

```text
Read Repository
```

而不是：

```text
Domain Aggregate
```

---

# 59. Page API

例如：

```java
public record PageResult<T>(
        List<T> items,
        long total,
        int page,
        int size
) {}
```

但大数据量场景推荐：

```text
Cursor Pagination
```

而非无限依赖：

```text
OFFSET
```

---

# 60. Cursor Pagination

例如：

```text
createdAt + id
```

作为稳定排序：

```sql
WHERE
    (created_at, id) > (?, ?)
ORDER BY
    created_at, id
LIMIT ?
```

---

# 61. Persistence Error Mapping

数据库异常不能直接暴露：

```text
SQLException
```

到 Application。

Infrastructure 应映射：

```text
SQLException
 ↓
PersistenceException
```

或者：

```text
ConcurrencyConflictException
```

---

# 62. Persistence Exception Taxonomy

建议：

```java
PersistenceException
 ├── PersistenceAccessException
 ├── PersistenceMappingException
 ├── PersistenceConstraintException
 ├── PersistenceConnectionException
 ├── PersistenceTimeoutException
 └── ConcurrencyConflictException
```

---

# 63. Constraint Violation

数据库：

```text
UNIQUE violation
FK violation
CHECK violation
NOT NULL violation
```

不能简单全部映射为：

```text
RuntimeException
```

需要保留足够语义供 Application 决策。

---

# 64. Database Constraint 与 Domain Rule

数据库约束：

```text
UNIQUE email
```

不等于 Domain Rule。

如果：

```text
Email must be unique
```

是业务不变量：

> Domain 应表达该规则。

数据库约束则负责：

> 最终持久化层防线。

二者可以同时存在。

---

# 65. Defense in Depth

推荐：

```text
Domain Validation
       +
Application Validation
       +
Database Constraint
```

而不是只依赖数据库。

---

# 66. Transaction 与 Persistence

Phase VI：

```text
Transaction
 ↓
UnitOfWork
 ↓
Repository
```

Phase VII：

```text
Repository
 ↓
Persistence Adapter
 ↓
Database
```

组合后：

```text
Command
 ↓
Transaction
 ↓
UoW
 ↓
Repository
 ↓
Persistence Adapter
 ↓
Database
```

---

# 67. Outbox Persistence

Outbox 必须与 Aggregate Persistence 使用：

```text
Same Local Transaction
```

例如：

```text
orders
outbox_events
```

在一个 Transaction 中写入。

---

# 68. Outbox Schema

参考结构：

```sql
CREATE TABLE outbox_event (
    event_id        VARCHAR(128) NOT NULL,
    event_type      VARCHAR(255) NOT NULL,
    event_version   INTEGER NOT NULL,
    aggregate_type  VARCHAR(255) NOT NULL,
    aggregate_id    VARCHAR(255) NOT NULL,
    sequence        BIGINT NOT NULL,
    occurred_at     TIMESTAMP NOT NULL,
    payload         BYTEA NOT NULL,
    status          VARCHAR(32) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    published_at    TIMESTAMP NULL,

    PRIMARY KEY (event_id)
);
```

具体数据库类型由 Adapter 决定。

---

# 69. Outbox Ordering

推荐唯一约束：

```text
aggregate_type
+
aggregate_id
+
sequence
```

确保：

```text
Same Aggregate
```

不能产生重复 Sequence。

---

# 70. Event Sequence

例如：

```text
OrderCreated sequence=1
OrderPaid    sequence=2
OrderShipped sequence=3
```

Dispatcher 可以据此实现：

```text
Aggregate-local ordering
```

---

# 71. Inbox Persistence

建议：

```sql
CREATE TABLE inbox_event (
    consumer_id VARCHAR(255) NOT NULL,
    event_id    VARCHAR(128) NOT NULL,
    processed_at TIMESTAMP NOT NULL,

    PRIMARY KEY (
        consumer_id,
        event_id
    )
);
```

---

# 72. Inbox Transaction

推荐：

```text
Transaction
 ├── Inbox Record
 ├── Consumer Business State
 └── Outbox
```

一次完成。

---

# 73. Inbox Processing

```text
Message
 ↓
Begin Transaction
 ↓
Insert Inbox
 ↓
Duplicate?
 ├── Yes → Commit / Ignore
 └── No
       ↓
    Process
       ↓
    State Change
       ↓
    Outbox
       ↓
    Commit
```

---

# 74. Schema Migration

数据库 Schema 必须版本化。

推荐：

```text
V001__create_orders.sql
V002__create_order_items.sql
V003__create_outbox.sql
```

Migration 工具可以使用：

- Flyway
- Liquibase
- 自研 Migration Engine

但 Migration 工具不进入 Domain。

---

# 75. Schema Evolution

Schema Evolution 必须遵循：

```text
Backward Compatible Change
        ↓
Deploy
        ↓
Data Migration
        ↓
Remove Legacy
```

避免：

```text
Application A
Database incompatible
Application B
```

同时上线。

---

# 76. Expand / Contract

推荐数据库变更：

```text
Expand
 ↓
Deploy Application
 ↓
Migrate Data
 ↓
Switch Read/Write
 ↓
Contract
```

---

# 77. Column Rename

不要直接：

```sql
ALTER COLUMN old_name RENAME TO new_name;
```

在复杂分布式部署中直接切换。

推荐：

```text
Add new column
 ↓
Dual Write
 ↓
Backfill
 ↓
Read new column
 ↓
Remove old column
```

---

# 78. Persistence Versioning

Persistence Model 可以独立版本：

```text
Persistence Schema v5
```

而 Domain Model：

```text
Domain Model v3
```

二者不要求版本号一致。

---

# 79. ORM Migration

如果使用 JPA：

```text
Domain Model
    ↓
Domain → Persistence Mapper
    ↓
@Entity Persistence Model
```

而不是：

```text
@Entity Domain Aggregate
```

---

# 80. JDBC Migration

JDBC：

```text
SQL
 ↓
Record
 ↓
Mapper
 ↓
Aggregate
```

可以完全不使用 ORM。

---

# 81. MyBatis Migration

MyBatis：

```text
Mapper XML / Annotation
 ↓
Persistence Record
 ↓
Domain Mapper
 ↓
Aggregate
```

同样不能污染 Domain。

---

# 82. Persistence Adapter SPI

可以定义：

```java
public interface PersistenceAdapter<
        D,
        P,
        I> {

    Optional<D> load(I id);

    void insert(D aggregate);

    void update(D aggregate);

    void delete(D aggregate);
}
```

但不要求所有 Repository 都必须直接实现该通用接口。

Repository 应优先保持领域语义。

---

# 83. Adapter 层次

推荐：

```text
Domain Repository
       │
       ▼
Concrete Repository Adapter
       │
       ▼
Persistence Store
       │
       ▼
Database Driver
```

而不是：

```text
Domain Repository
       ↓
Generic DAO
       ↓
Generic ORM
```

---

# 84. Database Access Boundary

数据库访问只允许：

```text
Infrastructure
```

出现。

禁止：

```text
Application
 ↓
JdbcTemplate
```

禁止：

```text
Domain
 ↓
EntityManager
```

---

# 85. SQL Boundary

SQL 可以存在于：

```text
Infrastructure
```

例如：

```text
repository/
store/
mapper/
sql/
```

但：

```text
Domain
```

不得出现 SQL。

---

# 86. Persistence Package

推荐：

```text
infrastructure.persistence
├── repository
├── mapper
├── record
├── store
├── sql
├── transaction
└── migration
```

---

# 87. Example Project

```text
order-service
├── domain
│   ├── order
│   │   ├── Order.java
│   │   ├── OrderId.java
│   │   └── OrderRepository.java
│
├── application
│   ├── command
│   └── query
│
└── infrastructure
    └── persistence
        ├── repository
        │   └── JdbcOrderRepository.java
        ├── mapper
        │   └── OrderMapper.java
        ├── record
        │   └── OrderRecord.java
        └── store
            └── OrderJdbcStore.java
```

---

# 88. Persistence Test

Repository 必须至少具备：

```text
Contract Test
Integration Test
Concurrency Test
Mapping Test
Migration Test
```

---

# 89. Repository Contract Test

例如：

```text
RepositoryContractTest
```

统一测试：

```text
save
findById
delete
version
concurrency
```

不同 Adapter：

```text
JDBC
JPA
MyBatis
```

都运行同一 Contract Test。

---

# 90. Mapping Test

必须验证：

```text
Domain
 ↓
Persistence
 ↓
Domain
```

不会发生：

```text
Information Loss
```

---

# 91. Concurrency Test

至少测试：

```text
A loads v1
B loads v1

A saves → v2
B saves → Conflict
```

---

# 92. Transaction Integration Test

测试：

```text
Aggregate
+
Outbox
```

必须：

```text
both commit
```

或者：

```text
both rollback
```

---

# 93. Multi-Tenant Test

必须测试：

```text
Tenant A
 ↓
Aggregate A
```

无法读取：

```text
Tenant B
 ↓
Aggregate B
```

---

# 94. Persistence Security

Persistence 层必须防止：

- SQL Injection
- Tenant Leakage
- Unauthorized Direct Access
- Sensitive Data Leakage
- Improper Logging

---

# 95. SQL Injection

禁止字符串拼接：

```java
"SELECT * FROM orders WHERE id = '" + id + "'"
```

必须使用：

```text
Prepared Statement
Parameterized Query
```

---

# 96. Sensitive Data Logging

Persistence 层不得默认记录：

```text
Password
Access Token
Secret
Payment Data
Sensitive Personal Data
```

完整 SQL 日志也必须谨慎。

---

# 97. Encryption

Domain 不需要知道：

```text
AES
KMS
Database Encryption
```

Persistence 可以实现：

```text
Encrypted Column
```

但加密是否属于业务语义必须明确判断。

---

# 98. Optimistic Lock vs Pessimistic Lock

默认：

```text
Optimistic Lock
```

原因：

- 更适合 Web
- 更适合分布式应用
- 减少长时间 DB Lock
- 更适合 Aggregate 模型

Pessimistic Lock：

```text
SELECT ... FOR UPDATE
```

可以作为：

```text
Infrastructure Capability
```

而非 Domain 默认能力。

---

# 99. Pessimistic Lock 使用原则

只有在明确需要时使用：

```text
High Contention
Critical Serialization
Short Transaction
```

不能用数据库锁替代 Aggregate Modeling。

---

# 100. Lock Boundary

锁的生命周期：

```text
Transaction
```

不能：

```text
HTTP Request
```

或者：

```text
Business Workflow
```

级别长期持有。

---

# 101. Read Consistency

Query 可能看到：

```text
Eventual Consistency
```

因此：

```text
Command Success
```

后：

```text
Query Immediately
```

不一定立即看到最新 Projection。

Framework 不把这种情况视为错误。

---

# 102. Read-Your-Writes

如果业务明确要求：

```text
Command
 ↓
Query
```

立即看到自己的修改，可以：

```text
same write model query
```

或者：

```text
projection acknowledgement
```

或者：

```text
version-aware read
```

但不能假设所有 Read Model 都强一致。

---

# 103. Persistence Health

Infrastructure 应提供：

```text
Database Connectivity
Migration Status
Connection Pool
Transaction Status
Outbox Backlog
```

这些不进入 Domain。

---

# 104. Connection Pool

Connection Pool 属于：

```text
Infrastructure
```

框架不在 Domain 中定义：

```text
HikariConfig
DataSource
```

---

# 105. Database Transaction 与 UnitOfWork

最终关系：

```text
Physical Transaction
        │
        ▼
UnitOfWork
        │
        ├── Aggregate A
        ├── Aggregate B
        ├── Repository Writes
        ├── Domain Events
        └── Outbox
```

---

# 106. Persistence Formal Rules

## PERSIST-001

Domain Model 不得依赖 Persistence Technology。

## PERSIST-002

Domain Model 不得依赖 ORM Annotation。

## PERSIST-003

Repository Contract 必须围绕 Aggregate。

## PERSIST-004

Repository 不得暴露数据库表模型。

## PERSIST-005

Persistence Model 不得作为 Domain Model 的替代。

## PERSIST-006

Persistence Mapper 必须负责 Domain/Persistence 转换。

## PERSIST-007

Repository 不得负责 Transaction Commit。

## PERSIST-008

Repository 不得负责 Transaction Rollback。

## PERSIST-009

数据库连接生命周期属于 Infrastructure。

## PERSIST-010

Aggregate Reconstruction 必须满足 Aggregate Boundary。

## PERSIST-011

Lazy Loading 不得成为 Domain 行为执行的隐式前提。

## PERSIST-012

Optimistic Concurrency 必须检查 Aggregate Version。

## PERSIST-013

Delete 必须支持 Concurrency Check。

## PERSIST-014

Persistence Exception 必须进行边界映射。

## PERSIST-015

Database Constraint 不替代 Domain Invariant。

## PERSIST-016

Database Constraint 可以作为 Defense-in-Depth。

## PERSIST-017

CQRS Read Repository 不得返回 Write Aggregate。

## PERSIST-018

Read Model 可以直接映射 Persistence Result。

## PERSIST-019

CQRS 不强制物理数据库分离。

## PERSIST-020

Schema Migration 必须版本化。

---

# 107. Mapping Rules

## MAP-001

Identifier 必须可映射。

## MAP-002

Value Object 必须保持语义完整性。

## MAP-003

Enum 不得依赖数据库 ordinal。

## MAP-004

时间字段必须明确时区语义。

## MAP-005

Mapper 不得执行 Domain Business Rule。

## MAP-006

Mapper 不得执行 Authorization。

## MAP-007

Mapper 不得执行 Transaction。

---

# 108. Multi-Tenant Rules

## TENANT-001

Tenant Isolation 必须在 Persistence 层得到保障。

## TENANT-002

不能只依赖前端 Tenant Filter。

## TENANT-003

不能只依赖 Interface Layer Authorization。

## TENANT-004

Persistence Query 必须确保 Tenant Scope。

## TENANT-005

跨 Tenant 查询必须具有明确的系统级授权。

---

# 109. Concurrency Rules

## PCON-001

Aggregate Update 必须携带 Expected Version。

## PCON-002

Version Conflict 必须显式报告。

## PCON-003

Persistence Adapter 不得静默覆盖并发修改。

## PCON-004

Retry 必须重新 Load Aggregate。

## PCON-005

Pessimistic Lock 不得作为 Domain API。

---

# 110. CQRS Persistence Rules

## CQRS-P-001

Command 使用 Write Repository。

## CQRS-P-002

Query 使用 Read Repository。

## CQRS-P-003

Write Repository 返回 Aggregate。

## CQRS-P-004

Read Repository 返回 Read Model。

## CQRS-P-005

Projection 不得直接修改 Write Aggregate。

## CQRS-P-006

Read Model 可以独立于 Domain Model 演进。

---

# 111. Outbox Persistence Rules

## OUTBOX-P-001

Outbox 必须与 Aggregate State 使用同一 Local Transaction。

## OUTBOX-P-002

Outbox Record 必须具有唯一 Event ID。

## OUTBOX-P-003

同一 Aggregate 的 Event 必须具有 Sequence。

## OUTBOX-P-004

Dispatcher 不得破坏 Aggregate Transaction。

## OUTBOX-P-005

Publish Failure 不得回滚已经成功提交的 Aggregate Transaction。

---

# 112. Persistence Module Architecture

Phase VII 后建议进一步拆分基础设施 Adapter：

```text
io.github.regalpine.ddd
│
├── ddd-core
├── ddd-domain
├── ddd-application
├── ddd-cqrs
├── ddd-event
├── ddd-transaction
├── ddd-port
│
├── ddd-infrastructure
│
├── ddd-infrastructure-jdbc
├── ddd-infrastructure-jpa
├── ddd-infrastructure-mybatis
│
├── ddd-test
└── ddd-spring-boot
```

---

# 113. Adapter Dependency

```text
ddd-infrastructure-jdbc
        ↓
ddd-infrastructure
        ↓
ddd-domain
```

JPA：

```text
ddd-infrastructure-jpa
        ↓
ddd-infrastructure
        ↓
ddd-domain
```

MyBatis：

```text
ddd-infrastructure-mybatis
        ↓
ddd-infrastructure
        ↓
ddd-domain
```

---

# 114. Core Dependency Rule

绝对禁止：

```text
ddd-core
 ↓
JPA
```

禁止：

```text
ddd-domain
 ↓
JDBC
```

禁止：

```text
ddd-domain
 ↓
Spring Data
```

---

# 115. Framework Lock-in

RegalPine DDD Framework 的核心价值之一是：

```text
Domain
        ↓
Stable Contract
        ↓
Technology Adapter
```

因此：

```text
JPA → JDBC
```

或者：

```text
MyBatis → JPA
```

不应该导致：

```text
Domain Rewrite
```

---

# 116. Adapter Replacement

理想情况下：

```text
                    Domain
                      │
              Repository Contract
                      │
          ┌───────────┼───────────┐
          ▼           ▼           ▼
         JDBC        JPA        MyBatis
```

可以替换：

```text
JPA
```

而：

```text
Domain
Application
CQRS
```

无需改变。

---

# 117. Persistence Technology Matrix

| Capability | JDBC | JPA | MyBatis |
|---|---:|---:|---:|
| Domain Independence | ✅ | ✅ | ✅ |
| Repository Adapter | ✅ | ✅ | ✅ |
| Optimistic Lock | ✅ | ✅ | ✅ |
| Transaction Integration | ✅ | ✅ | ✅ |
| Outbox | ✅ | ✅ | ✅ |
| Read Model | ✅ | ✅ | ✅ |
| SQL Control | ★★★★★ | ★★ | ★★★★★ |
| ORM Automation | ★ | ★★★★★ | ★★★ |
| Framework Lock-in | Low | Medium | Medium |

---

# 118. Recommended Default

RegalPine Framework Core 不选择：

```text
JPA First
```

也不选择：

```text
JDBC First
```

而是：

```text
Persistence Adapter Neutral
```

官方 Adapter 可以提供：

```text
JDBC
JPA
MyBatis
```

开发者根据项目选择。

---

# 119. Test Architecture

最终：

```text
ddd-test
 ├── Repository Contract Test
 ├── Mapping Test
 ├── Transaction Test
 ├── Concurrency Test
 ├── Outbox Test
 └── Projection Test
```

---

# 120. Framework Conformance

Persistence Adapter 必须通过：

```text
PCK
Persistence Conformance Kit
```

验证：

```text
Repository
Concurrency
Transaction
Mapping
Tenant Isolation
Outbox
```

---

# 121. Repository Conformance

每个 Repository Adapter 必须通过：

```text
save
load
delete
version
concurrency
```

测试。

---

# 122. Transaction Conformance

必须验证：

```text
Aggregate Commit
+
Outbox Commit
```

和：

```text
Aggregate Rollback
+
Outbox Rollback
```

---

# 123. Adapter Contract

一个 JDBC Adapter：

```text
必须
    满足 Domain Repository Contract

可以
    使用 JDBC

不得
    修改 Domain Contract
```

---

# 124. Phase VII 最终架构

```text
                         Command
                            │
                            ▼
                       CommandBus
                            │
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
                            ▼
                   Repository Contract
                            │
                            ▼
                 Repository Adapter
                            │
                 ┌──────────┼──────────┐
                 ▼          ▼          ▼
               JDBC        JPA       MyBatis
                 │          │          │
                 └──────────┼──────────┘
                            ▼
                         Database
```

---

# 125. Read Path

```text
Query
 ↓
QueryBus
 ↓
QueryHandler
 ↓
ReadRepository
 ↓
Read Model Store
 ↓
Read Model
```

---

# 126. Event Projection Path

```text
Aggregate
 ↓
Domain Event
 ↓
Outbox
 ↓
Commit
 ↓
Dispatcher
 ↓
Event Consumer
 ↓
Projection
 ↓
Read Store
```

---

# 127. 完整数据生命周期

```text
                    Command
                       │
                       ▼
                  Application
                       │
                       ▼
                   Aggregate
                       │
                       ▼
                  Domain Event
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
       Write Repository       Outbox
             │                   │
             ▼                   │
         Write Store             │
             │                   │
             └─────────┬─────────┘
                       ▼
                    COMMIT
                       │
                       ▼
                  Dispatcher
                       │
                       ▼
                    Event
                       │
                       ▼
                  Projection
                       │
                       ▼
                  Read Store
                       │
                       ▼
                     Query
```

---

# 128. Phase VII 冻结内容

Phase VII 完成后，以下内容原则上冻结：

```text
Domain → Repository Contract
Domain → Persistence Independence
Aggregate Persistence Boundary
Persistence Mapper
Write Repository
Read Repository
Optimistic Concurrency
Tenant Persistence Boundary
Outbox Persistence
Inbox Persistence
Schema Migration
JDBC/JPA/MyBatis Adapter Boundary
```

后续阶段不再重新讨论：

> Domain 是否应该直接使用 ORM。

答案已经冻结：

```text
NO
```

---

# 129. Phase VII Completion Matrix

| Capability | Status |
|---|---|
| Repository Contract | ✅ |
| Persistence Model | ✅ |
| Domain/Persistence Mapping | ✅ |
| Aggregate Reconstruction | ✅ |
| Identity Mapping | ✅ |
| Value Object Mapping | ✅ |
| Version Mapping | ✅ |
| Optimistic Lock | ✅ |
| Delete Concurrency | ✅ |
| JDBC Boundary | ✅ |
| JPA Boundary | ✅ |
| MyBatis Boundary | ✅ |
| Read Repository | ✅ |
| Write Repository | ✅ |
| Read Model | ✅ |
| CQRS Persistence | ✅ |
| Outbox Persistence | ✅ |
| Inbox Persistence | ✅ |
| Multi-Tenant Persistence | ✅ |
| Soft Delete | ✅ |
| Audit Boundary | ✅ |
| Schema Migration | ✅ |
| Error Mapping | ✅ |
| Pagination | ✅ |
| Cursor Pagination | ✅ |
| Repository Contract Test | ✅ |
| Concurrency Test | ✅ |
| Transaction Test | ✅ |
| Adapter Conformance | ✅ |

---

# 130. Phase VII 收敛判断

Phase VII 完成后，RegalPine DDD Framework 已经建立：

```text
DDD Model
     ↓
Application
     ↓
CQRS
     ↓
Transaction
     ↓
UnitOfWork
     ↓
Repository
     ↓
Persistence Adapter
     ↓
Database
```

并且：

```text
Domain
    不依赖 ORM

Domain
    不依赖 Database

Application
    不依赖具体 Persistence Technology

CQRS
    不依赖具体 Database

Transaction
    不依赖 Domain Business Logic
```

因此：

> **DDD Framework 的核心抽象层已经基本完成。**

后续阶段的重点应该从“定义核心抽象”逐渐转向：

```text
Infrastructure
Runtime
Integration
Testing
Observability
Developer Experience
```

---

# 131. 下一阶段

下一阶段建议进入：

# Phase VIII — Infrastructure & Adapter Runtime Specification

重点定义：

```text
Database Adapter Runtime
Transaction Runtime
Connection Management
Repository Factory
Outbox Dispatcher
Inbox Processor
Event Publisher
Message Broker Adapter
Cache Adapter
Clock
ID Generator
Serialization
Configuration
Lifecycle
Health Check
Observability
```

形成：

```text
ddd-core
      ↓
ddd-domain
      ↓
ddd-application
      ↓
ddd-cqrs
      ↓
ddd-transaction
      ↓
ddd-infrastructure
      ↓
Runtime Adapters
```

Phase VIII 将重点回答：

> **这些已经定义好的 Framework Contract，运行时到底如何组装和启动。**

---

# 132. Framework 收敛路线

截至 Phase VII：

```text
Phase I
DDD Ontology / Conceptual Model       ✅

Phase II
ddd-core                              ✅

Phase III
ddd-domain                            ✅

Phase IV
ddd-application                       ✅

Phase V
CQRS + Event                          ✅

Phase VI
Transaction + UoW                     ✅

Phase VII
Persistence + Repository              ✅

Phase VIII
Infrastructure Runtime               → NEXT

Phase IX
Integration + Messaging               → planned

Phase X
Testing + Conformance                 → planned

Phase XI
Developer Experience + Bootstrapping  → planned
```

核心原则：

> **Phase VIII 以后原则上不再改变前七阶段的 Domain 核心语义，只实现、集成、验证和产品化。**