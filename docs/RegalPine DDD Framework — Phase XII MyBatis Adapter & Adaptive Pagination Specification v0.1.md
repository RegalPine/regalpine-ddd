# RegalPine DDD Framework
## Phase XII — MyBatis Adapter & Adaptive Pagination Specification v0.1

**GroupId:** `io.github.regalpine.ddd`  
**Java:** 17+  
**Build:** Maven  
**Status:** Implementation Specification  
**Architecture Status:** Frozen  
**Target Module:** `ddd-infrastructure-mybatis`

---

# 1. 文档目的

本文定义 RegalPine DDD Framework 的 MyBatis 基础设施适配层。

目标：

1. 将 MyBatis 接入 RegalPine DDD Framework。
2. 保持 Domain/Application/CQRS 不依赖 MyBatis。
3. 提供 Aggregate Repository MyBatis 实现。
4. 提供 Query/Read Model MyBatis 实现。
5. 提供统一分页请求与分页响应。
6. 提供数据库分页方言自适应能力。
7. 提供 Transaction/UoW 与 MyBatis Session 的一致性边界。
8. 提供 Outbox/Inbox MyBatis 实现。
9. 提供 MyBatis Adapter Conformance Test。
10. 避免重新设计 ORM。
11. 不新增 DDD 核心抽象。

---

# 2. 核心设计原则

## 2.1 MyBatis 是 Adapter

MyBatis 不属于 DDD 核心。

```text
ddd-core
    ↑
ddd-domain
    ↑
ddd-application
    ↑
ddd-cqrs
    ↑
ddd-event
    ↑
ddd-transaction
    ↑
ddd-runtime
    ↑
ddd-infrastructure
    ↑
ddd-infrastructure-mybatis
    ↑
MyBatis
```

禁止：

```text
Domain → MyBatis
Application → MyBatis
Aggregate → Mapper
Domain Event → Mapper
```

---

# 3. 模块定位

新增：

```text
ddd-infrastructure-mybatis
```

Maven：

```xml
<dependency>
    <groupId>io.github.regalpine.ddd</groupId>
    <artifactId>ddd-infrastructure-mybatis</artifactId>
    <version>${revision}</version>
</dependency>
```

---

# 4. Maven 依赖原则

MyBatis Adapter 可以依赖：

```text
ddd-core
ddd-domain
ddd-application
ddd-cqrs
ddd-event
ddd-transaction
ddd-runtime
ddd-infrastructure
mybatis
```

不允许依赖：

```text
ddd-spring-boot
ddd-infrastructure-jpa
ddd-infrastructure-jdbc
```

除非后续明确形成独立共享 SPI。

---

# 5. 包结构

```text
io.github.regalpine.ddd.infrastructure.mybatis
│
├── MyBatisAdapter
│
├── configuration
│   ├── MyBatisConfiguration
│   ├── MyBatisAdapterConfiguration
│   └── MyBatisSessionFactoryProvider
│
├── session
│   ├── MyBatisSessionAdapter
│   └── MyBatisSessionContext
│
├── repository
│   ├── MyBatisAggregateRepository
│   ├── MyBatisRepositoryFactory
│   └── MyBatisRepositoryDefinition
│
├── query
│   ├── MyBatisQueryExecutor
│   ├── MyBatisReadRepository
│   └── MyBatisQueryDefinition
│
├── pagination
│   ├── PaginationInterceptor
│   ├── PaginationDialectResolver
│   ├── PaginationDialect
│   ├── OffsetPaginationDialect
│   ├── CursorPaginationDialect
│   └── CountExecutor
│
├── dialect
│   ├── PostgreSqlPaginationDialect
│   ├── MySqlPaginationDialect
│   ├── OraclePaginationDialect
│   ├── SqlServerPaginationDialect
│   └── Db2PaginationDialect
│
├── transaction
│   └── MyBatisTransactionAdapter
│
├── outbox
│   └── MyBatisOutboxStore
│
├── inbox
│   └── MyBatisInboxStore
│
├── mapping
│   ├── AggregateMapper
│   ├── DomainEventMapper
│   └── ValueObjectMapper
│
└── exception
    ├── MyBatisAdapterException
    ├── MyBatisConcurrencyException
    └── MyBatisMappingException
```

---

# 6. MyBatis Adapter 总体结构

```text
                 Application
                     │
                 QueryHandler
                     │
                QueryRepository
                     │
                     ▼
           MyBatisQueryExecutor
                     │
                     ▼
                MyBatis Mapper
                     │
                     ▼
                  Database


                 Aggregate
                     │
                     ▼
            AggregateRepository
                     │
                     ▼
         MyBatisAggregateRepository
                     │
                     ▼
                Mapper
                     │
                     ▼
                  Database
```

---

# 7. Repository 设计

MyBatis 不直接改变已有：

```java
public interface AggregateRepository<
        A extends AggregateRoot<I>,
        I extends Identifier> {

    Optional<A> findById(I id);

    void save(A aggregate);

    void delete(A aggregate);
}
```

MyBatis 只是实现它：

```java
public final class MyBatisAggregateRepository<
        A extends AggregateRoot<I>,
        I extends Identifier>
        implements AggregateRepository<A, I> {

    // implementation
}
```

---

# 8. Repository Mapper 原则

Mapper 是 Infrastructure 内部对象。

例如：

```java
public interface OrderMapper {

    OrderRecord selectById(String id);

    int insert(OrderRecord record);

    int update(OrderRecord record);

    int deleteById(String id);
}
```

Domain 不知道：

```java
OrderMapper
OrderRecord
SqlSession
MappedStatement
ResultMap
```

---

# 9. Persistence Record

数据库模型与 Domain Aggregate 分离。

例如：

```java
public record OrderRecord(
        String id,
        String customerId,
        String status,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}
```

禁止直接使用：

```java
Order extends AggregateRoot
```

作为数据库 Record。

原因：

```text
Domain Model
     ≠
Persistence Model
```

---

# 10. Aggregate Mapping

定义：

```java
public interface AggregateMapper<A, R> {

    R toRecord(A aggregate);

    A toAggregate(R record);
}
```

例如：

```java
public final class OrderMapperAdapter
        implements AggregateMapper<Order, OrderRecord> {

    @Override
    public OrderRecord toRecord(Order order) {
        return new OrderRecord(
                order.id().value(),
                order.customerId(),
                order.status().name(),
                order.version().value(),
                order.createdAt(),
                order.updatedAt()
        );
    }

    @Override
    public Order toAggregate(OrderRecord record) {
        return Order.restore(
                record.id(),
                record.customerId(),
                record.status(),
                record.version(),
                record.createdAt(),
                record.updatedAt()
        );
    }
}
```

---

# 11. Optimistic Concurrency

MyBatis Repository 必须支持版本控制。

SQL：

```sql
UPDATE orders
SET
    status = #{status},
    version = version + 1,
    updated_at = #{updatedAt}
WHERE id = #{id}
  AND version = #{expectedVersion}
```

执行：

```java
int affected = mapper.update(record);

if (affected != 1) {
    throw new MyBatisConcurrencyException(
            record.id(),
            record.version()
    );
}
```

规则：

```text
affected = 1
    → success

affected = 0
    → concurrency conflict
```

禁止：

```text
UPDATE orders
SET version = version + 1
WHERE id = ?
```

这种没有版本条件的更新不能满足 Framework 的乐观并发约束。

---

# 12. Save 语义

`save()` 不等于 SQL `INSERT`。

Repository 必须区分：

```text
New Aggregate
    ↓
INSERT

Existing Aggregate
    ↓
UPDATE + expectedVersion
```

推荐通过 Aggregate Lifecycle/Repository State 判断。

禁止简单：

```sql
MERGE
```

或者：

```sql
INSERT ... ON CONFLICT ...
```

取代所有 Aggregate 保存语义。

原因：

Aggregate concurrency 是领域生命周期语义，而不是简单 UPSERT。

---

# 13. Transaction

MyBatis Adapter 提供：

```java
public final class MyBatisTransactionAdapter
        implements TransactionAdapter {

    @Override
    public <T> T execute(
            TransactionDefinition definition,
            TransactionCallback<T> callback) {

        // transaction boundary
    }
}
```

事务边界：

```text
TransactionAdapter
      │
      ▼
SqlSession
      │
      ├── Repository
      ├── Outbox
      └── Inbox
```

---

# 14. SqlSession 生命周期

标准：

```text
BEGIN
  ↓
open SqlSession
  ↓
Application Handler
  ↓
Repository
  ↓
Outbox
  ↓
COMMIT
  ↓
close SqlSession
```

异常：

```text
BEGIN
  ↓
open SqlSession
  ↓
Handler
  ↓
Exception
  ↓
ROLLBACK
  ↓
close
```

禁止：

```text
Repository 自己 commit()
```

---

# 15. UnitOfWork 集成

最终：

```text
Command
  ↓
CommandBus
  ↓
Middleware
  ↓
TransactionAdapter
  ↓
UnitOfWork
  ↓
CommandHandler
  ↓
Aggregate
  ↓
Repository
  ↓
Outbox
  ↓
COMMIT
```

MyBatis 不能创建第二个独立事务。

---

# 16. 分页模型

Framework 提供统一分页 API。

## 16.1 PageRequest

```java
public record PageRequest(
        int page,
        int size,
        List<SortOrder> sorts,
        CountMode countMode
) {

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "page must be >= 0"
            );
        }

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "size must be > 0"
            );
        }

        sorts = sorts == null
                ? List.of()
                : List.copyOf(sorts);

        countMode = countMode == null
                ? CountMode.EXACT
                : countMode;
    }

    public long offset() {
        return (long) page * size;
    }
}
```

---

# 17. SortOrder

```java
public record SortOrder(
        String property,
        Direction direction
) {

    public SortOrder {
        if (property == null || property.isBlank()) {
            throw new IllegalArgumentException(
                    "property must not be blank"
            );
        }
    }

    public enum Direction {
        ASC,
        DESC
    }
}
```

---

# 18. CountMode

```java
public enum CountMode {

    /**
     * Execute exact COUNT.
     */
    EXACT,

    /**
     * Do not execute COUNT.
     */
    NONE,

    /**
     * Database estimated count where supported.
     */
    ESTIMATED,

    /**
     * Adapter determines strategy.
     */
    AUTO
}
```

---

# 19. PageResult

```java
public record PageResult<T>(
        List<T> content,
        long page,
        long size,
        long totalElements,
        long totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {

    public PageResult {
        content = List.copyOf(content);
    }

    public static <T> PageResult<T> of(
            List<T> content,
            long page,
            long size,
            long totalElements) {

        long totalPages =
                size == 0
                        ? 0
                        : (totalElements + size - 1) / size;

        return new PageResult<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                page + 1 >= totalPages,
                page + 1 < totalPages,
                page > 0
        );
    }
}
```

---

# 20. Query 分页

定义：

```java
public interface PageQuery<R>
        extends Query<PageResult<R>> {

    PageRequest pageRequest();
}
```

例如：

```java
public record ListOrdersQuery(
        String customerId,
        PageRequest pageRequest
) implements PageQuery<OrderView> {
}
```

Handler：

```java
public final class ListOrdersQueryHandler
        implements QueryHandler<
            ListOrdersQuery,
            PageResult<OrderView>> {

    private final OrderReadRepository repository;

    @Override
    public PageResult<OrderView> handle(
            ListOrdersQuery query) {

        return repository.findOrders(
                query.customerId(),
                query.pageRequest()
        );
    }
}
```

---

# 21. Query Repository

建议定义：

```java
public interface PageQueryRepository<C, R> {

    PageResult<R> query(
            C condition,
            PageRequest pageRequest
    );
}
```

该接口属于 Query/Infrastructure 边界。

Domain 不应该依赖分页。

---

# 22. PaginationDialect

MyBatis 分页方言：

```java
public interface PaginationDialect {

    String name();

    String applyOffsetLimit(
            String sql,
            long offset,
            long limit
    );

    boolean supportsOffset();

    boolean supportsLimit();

    boolean supportsKeyset();
}
```

---

# 23. PostgreSQL

```java
public final class PostgreSqlPaginationDialect
        implements PaginationDialect {

    @Override
    public String name() {
        return "postgresql";
    }

    @Override
    public String applyOffsetLimit(
            String sql,
            long offset,
            long limit) {

        return sql + " LIMIT ? OFFSET ?";
    }

    @Override
    public boolean supportsOffset() {
        return true;
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsKeyset() {
        return true;
    }
}
```

---

# 24. MySQL

```java
public final class MySqlPaginationDialect
        implements PaginationDialect {

    @Override
    public String name() {
        return "mysql";
    }

    @Override
    public String applyOffsetLimit(
            String sql,
            long offset,
            long limit) {

        return sql + " LIMIT ? OFFSET ?";
    }

    @Override
    public boolean supportsOffset() {
        return true;
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsKeyset() {
        return true;
    }
}
```

---

# 25. Oracle

Oracle 使用：

```sql
OFFSET ? ROWS
FETCH NEXT ? ROWS ONLY
```

因此：

```java
public final class OraclePaginationDialect
        implements PaginationDialect {

    @Override
    public String name() {
        return "oracle";
    }

    @Override
    public String applyOffsetLimit(
            String sql,
            long offset,
            long limit) {

        return sql
                + " OFFSET ? ROWS"
                + " FETCH NEXT ? ROWS ONLY";
    }

    @Override
    public boolean supportsOffset() {
        return true;
    }

    @Override
    public boolean supportsLimit() {
        return false;
    }

    @Override
    public boolean supportsKeyset() {
        return true;
    }
}
```

---

# 26. SQL Server

```sql
OFFSET ? ROWS
FETCH NEXT ? ROWS ONLY
```

同样由：

```text
SqlServerPaginationDialect
```

实现。

---

# 27. 方言自动发现

定义：

```java
public interface PaginationDialectResolver {

    PaginationDialect resolve(
            DatabaseMetadata metadata
    );
}
```

数据库信息：

```java
public record DatabaseMetadata(
        String productName,
        String productVersion,
        int majorVersion,
        int minorVersion
) {
}
```

启动时：

```text
JDBC Connection
       ↓
DatabaseMetaData
       ↓
DatabaseMetadata
       ↓
PaginationDialectResolver
       ↓
PaginationDialect
```

---

# 28. Dialect Resolver

例如：

```java
public final class DefaultPaginationDialectResolver
        implements PaginationDialectResolver {

    @Override
    public PaginationDialect resolve(
            DatabaseMetadata metadata) {

        String name =
                metadata.productName().toLowerCase();

        if (name.contains("postgresql")) {
            return new PostgreSqlPaginationDialect();
        }

        if (name.contains("mysql")) {
            return new MySqlPaginationDialect();
        }

        if (name.contains("oracle")) {
            return new OraclePaginationDialect();
        }

        if (name.contains("sql server")
                || name.contains("microsoft")) {
            return new SqlServerPaginationDialect();
        }

        throw new IllegalStateException(
                "Unsupported database: "
                        + metadata.productName()
        );
    }
}
```

---

# 29. 不允许业务代码判断数据库

禁止：

```java
if (database.equals("mysql")) {
    ...
}
```

业务层只能：

```java
PageRequest request
```

Infrastructure 决定：

```text
Database
 ↓
Dialect
 ↓
SQL
```

---

# 30. MyBatis PaginationInterceptor

MyBatis Adapter 可以提供：

```java
public final class PaginationInterceptor
        implements Interceptor {
}
```

职责：

```text
MappedStatement
      ↓
BoundSql
      ↓
Pagination Context
      ↓
Dialect
      ↓
Paginated SQL
```

Interceptor 不负责：

- Domain Logic
- Repository Logic
- Authorization
- Transaction
- Business validation

---

# 31. 分页参数传递

建议使用内部：

```java
public record PaginationContext(
        PageRequest request
) {
}
```

由 Framework Query Executor 设置：

```text
Query
 ↓
PageRequest
 ↓
PaginationContext
 ↓
MyBatis
```

Mapper 不应该自己计算：

```java
offset = page * size
```

而由 Framework 统一计算。

---

# 32. Count 查询

分页查询通常需要：

```text
SELECT page data
+
SELECT COUNT(*)
```

例如：

```sql
SELECT
    id,
    customer_id,
    status
FROM orders
WHERE customer_id = ?
ORDER BY created_at DESC
LIMIT ? OFFSET ?
```

Count：

```sql
SELECT COUNT(*)
FROM orders
WHERE customer_id = ?
```

---

# 33. CountExecutor

```java
public interface CountExecutor {

    long count(
            String countSql,
            Object parameter
    );
}
```

---

# 34. CountMode 行为

```text
EXACT
 ├── SELECT data
 └── SELECT COUNT

NONE
 └── SELECT data

AUTO
 └── Adapter chooses

ESTIMATED
 └── DB-specific strategy
```

---

# 35. PageResult 与 NONE

当：

```java
CountMode.NONE
```

时：

```text
totalElements = -1
totalPages = -1
```

例如：

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": -1,
  "totalPages": -1,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

---

# 36. 推荐使用 LIMIT + 1

当不执行 COUNT 时：

```sql
LIMIT size + 1
```

例如：

```text
requested size = 20

database:
LIMIT 21
```

如果返回：

```text
21 rows
```

则：

```text
hasNext = true
```

然后删除第 21 条。

这样可以避免昂贵的 COUNT。

---

# 37. Cursor Pagination

对于大数据集，Framework 支持：

```java
public record CursorPageRequest(
        String cursor,
        int size,
        List<SortOrder> sorts
) {
}
```

查询：

```sql
WHERE id > ?
ORDER BY id
LIMIT ?
```

或者：

```sql
WHERE created_at < ?
ORDER BY created_at DESC
LIMIT ?
```

---

# 38. Cursor 必须有稳定排序

禁止：

```text
ORDER BY random()
```

禁止只按照非唯一字段：

```text
ORDER BY created_at
```

推荐：

```sql
ORDER BY created_at DESC, id DESC
```

Cursor：

```text
(created_at, id)
```

---

# 39. 自适应分页策略

Framework 支持：

```java
public enum PaginationMode {

    OFFSET,

    CURSOR,

    AUTO
}
```

AUTO 不意味着偷偷改变用户语义。

默认规则：

```text
显式 OFFSET
    ↓
严格 OFFSET

显式 CURSOR
    ↓
严格 CURSOR

AUTO
    ↓
Framework 根据 Adapter 能力和查询条件选择
```

---

# 40. AUTO 策略

推荐：

```text
无 Cursor
+
page 较小
    → OFFSET

存在 Cursor
    → CURSOR

超大 Offset
    → 建议 Cursor

数据库不支持 Offset
    → Dialect fallback
```

但是：

**Framework 不应该无感知地把 `page=10000` 转换成 Cursor。**

因为两者 API 语义不同。

---

# 41. MyBatis Mapper 模式

推荐两种模式。

## 模式 A：XML Mapper

```xml
<select id="selectOrders"
        resultType="OrderRecord">

    SELECT
        id,
        customer_id,
        status,
        version,
        created_at,
        updated_at
    FROM orders
    WHERE customer_id = #{customerId}
    ORDER BY created_at DESC, id DESC

</select>
```

Framework 负责追加分页。

---

## 模式 B：Annotation Mapper

```java
@Select("""
    SELECT
        id,
        customer_id,
        status,
        version
    FROM orders
    WHERE customer_id = #{customerId}
    ORDER BY created_at DESC, id DESC
    """)
List<OrderRecord> selectOrders(
        String customerId
);
```

两者均支持。

---

# 42. SQL 改写边界

PaginationInterceptor 只允许修改：

```text
ORDER / LIMIT / OFFSET
```

或者在明确支持的情况下：

```text
Cursor predicate
```

禁止随意重写：

```text
JOIN
WHERE
GROUP BY
HAVING
SELECT semantics
```

避免形成一个“SQL 重写引擎”。

---

# 43. SQL 安全

排序字段不能直接来自用户输入：

危险：

```java
"ORDER BY " + request.property()
```

必须通过：

```java
SortFieldRegistry
```

映射：

```text
createdAt → created_at
customerId → customer_id
status → status
```

不存在的字段：

```text
→ reject
```

---

# 44. SortFieldRegistry

```java
public interface SortFieldRegistry {

    Optional<String> resolve(String property);
}
```

例如：

```text
createdAt → created_at
id        → id
status    → status
```

禁止任意 SQL 片段进入排序。

---

# 45. Outbox

MyBatis 实现：

```java
public final class MyBatisOutboxStore
        implements OutboxStore {
}
```

数据库：

```sql
CREATE TABLE ddd_outbox (
    message_id       VARCHAR(128) NOT NULL,
    message_type     VARCHAR(256) NOT NULL,
    schema_version   INTEGER NOT NULL,
    aggregate_type   VARCHAR(256),
    aggregate_id     VARCHAR(256),
    sequence         BIGINT,
    payload          BLOB NOT NULL,
    headers          TEXT,
    occurred_at      TIMESTAMP NOT NULL,
    published_at     TIMESTAMP NULL,
    status            VARCHAR(32) NOT NULL,
    attempts         INTEGER NOT NULL,
    last_error       TEXT,
    PRIMARY KEY (message_id)
);
```

实际字段类型由数据库方言适配。

---

# 46. Outbox 原子性

必须：

```text
Aggregate UPDATE
      +
Outbox INSERT
      ↓
同一个 SqlSession
      ↓
同一个 Transaction
      ↓
COMMIT
```

禁止：

```text
Aggregate COMMIT
    ↓
Outbox INSERT
```

这会产生事件丢失窗口。

---

# 47. Inbox

```java
public final class MyBatisInboxStore
        implements InboxStore {
}
```

数据库：

```sql
CREATE TABLE ddd_inbox (
    consumer_id VARCHAR(256) NOT NULL,
    message_id  VARCHAR(128) NOT NULL,
    processed_at TIMESTAMP NOT NULL,
    PRIMARY KEY (
        consumer_id,
        message_id
    )
);
```

保证：

```text
consumerId + messageId
```

唯一。

---

# 48. Inbox 消费语义

```text
Message
  ↓
INSERT Inbox
  ↓
success
  ↓
Business Handler
```

如果：

```text
duplicate key
```

则：

```text
already processed
→ ignore
```

业务状态与 Inbox 记录应该在需要时进入同一事务。

---

# 49. MyBatis TypeHandler

允许 Adapter 提供：

```text
IdentifierTypeHandler
VersionTypeHandler
EventIdTypeHandler
InstantTypeHandler
ValueObjectTypeHandler
```

例如：

```java
public final class IdentifierTypeHandler
        extends BaseTypeHandler<Identifier> {
}
```

但 Domain 不依赖 MyBatis `TypeHandler`。

---

# 50. Value Object Mapping

例如：

```java
record CustomerId(String value)
        implements Identifier {
}
```

数据库：

```text
VARCHAR
```

MyBatis：

```text
CustomerId
    ↕
VARCHAR
```

由 Adapter 完成。

---

# 51. JSON Value Object

对于需要 JSON 持久化的 Value Object：

```text
ValueObject
    ↓
Explicit Serializer
    ↓
JSON
```

禁止：

```text
Java native serialization
```

尤其禁止：

```java
ObjectInputStream
```

处理不可信数据。

---

# 52. MyBatis 与 Aggregate Event

Repository 保存 Aggregate：

```text
Aggregate
 ├── State
 └── Pending Events
```

Transaction 中：

```text
save aggregate
    ↓
persist state
    ↓
extract pending events
    ↓
Outbox INSERT
    ↓
commit
```

Repository 不直接发送 Message。

---

# 53. Read Model

MyBatis 非常适合 Query Side。

例如：

```java
public record OrderView(
        String id,
        String customerId,
        String status,
        Instant createdAt
) {
}
```

Mapper：

```java
public interface OrderQueryMapper {

    List<OrderView> selectOrders(
            OrderQueryParameter parameter
    );

    long countOrders(
            OrderQueryParameter parameter
    );
}
```

Query Handler：

```text
QueryHandler
    ↓
ReadRepository
    ↓
MyBatis Mapper
    ↓
Read Model
```

---

# 54. Query 与 Aggregate 分离

Query 不应该：

```text
SELECT → Aggregate → Domain
```

除非明确需要。

推荐：

```text
SELECT
 ↓
OrderView
 ↓
PageResult<OrderView>
```

避免不必要的 Aggregate hydration。

---

# 55. Transaction 与 Query

默认：

```text
Query
 ↓
MyBatis Session
 ↓
SELECT
```

可以使用：

```text
readOnly = true
```

但具体是否建立物理事务由 Adapter 决定。

---

# 56. MyBatis Session Context

```java
public interface MyBatisSessionContext {

    SqlSession current();

    boolean hasCurrent();
}
```

原则：

```text
one transaction
    =
one managed SqlSession
```

Repository 从当前上下文获取 Session。

---

# 57. Thread Context

传统同步模型可以使用：

```text
ThreadLocal
```

但不能跨：

```text
CompletableFuture
Executor
Message Consumer
Reactive Pipeline
```

传播。

异步边界：

```text
old transaction
      X
new transaction
```

必须重新建立。

---

# 58. Spring Boot

MyBatis Adapter 本身：

```text
不依赖 Spring
```

如果使用：

```text
Spring Boot
+
MyBatis
```

则：

```text
ddd-spring-boot
      ↓
ddd-infrastructure-mybatis
      ↓
MyBatis
```

Spring 只负责：

- Bean 生命周期
- Configuration
- DataSource
- Transaction integration
- MyBatis SessionFactory

而不是改变 DDD Contract。

---

# 59. DataSource

`ddd-infrastructure-mybatis` 不强制定义 DataSource。

可以接受：

```java
DataSource
```

也可以由 Adapter Provider 注入：

```text
DataSource
 ↓
SqlSessionFactory
 ↓
MyBatisSessionAdapter
```

---

# 60. Mapper 注册

支持：

```text
XML mapper
Annotation mapper
Programmatic mapper registration
```

推荐：

```text
Explicit Registration
```

避免自动扫描造成隐式行为。

---

# 61. MyBatis Adapter Factory

```java
public interface MyBatisRepositoryFactory
        extends RepositoryFactory {
}
```

初始化：

```text
Repository Definition
       ↓
Mapper
       ↓
AggregateMapper
       ↓
MyBatisAggregateRepository
```

保证：

```text
one Aggregate Type
    =
one active Repository
```

---

# 62. 配置

```java
public record MyBatisAdapterConfiguration(
        boolean lazyInitialization,
        boolean mapUnderscoreToCamelCase,
        boolean paginationEnabled,
        boolean optimisticLockEnabled,
        CountMode defaultCountMode
) {
}
```

默认：

```text
paginationEnabled       = true
optimisticLockEnabled   = true
mapUnderscoreToCamelCase = false
```

不建议 Framework 偷偷改变 MyBatis 原始配置。

---

# 63. 配置优先级

沿用 Runtime 既定规则：

```text
Programmatic
    >
Environment
    >
File
    >
Default
```

---

# 64. Adapter 启动检查

启动时检查：

```text
MyBatis Configuration
       ↓
Database
       ↓
Dialect
       ↓
Mapper
       ↓
Repository
```

失败条件：

```text
Unsupported DB
Missing mapper
Duplicate mapper
Invalid dialect
Invalid transaction configuration
```

则：

```text
Runtime = FAILED
```

---

# 65. Conformance Test

新增：

```text
ddd-conformance
└── mybatis
    ├── RepositoryConformanceTest
    ├── TransactionConformanceTest
    ├── PaginationConformanceTest
    ├── ConcurrencyConformanceTest
    ├── OutboxConformanceTest
    └── InboxConformanceTest
```

---

# 66. Repository Conformance

必须验证：

```text
CREATE
READ
UPDATE
DELETE
```

以及：

```text
Identity
Version
Concurrency
Mapping
```

---

# 67. Pagination Conformance

每种 Dialect 至少测试：

```text
page=0,size=10
page=1,size=10
empty result
exact boundary
last page
large offset
sorting
multiple sorting
```

---

# 68. Count Conformance

测试：

```text
EXACT
NONE
AUTO
```

特别测试：

```text
0 rows
1 row
size rows
size + 1 rows
multiple pages
```

---

# 69. Dialect Conformance

每个数据库方言验证：

```text
PostgreSQL
MySQL
Oracle
SQL Server
DB2
```

不能只通过字符串检查。

必须执行实际 SQL Integration Test。

---

# 70. SQL Injection Conformance

必须测试：

```text
sort property
sort direction
page
size
cursor
```

例如：

```text
created_at DESC
```

合法。

而：

```text
created_at DESC; DROP TABLE orders
```

必须被拒绝。

---

# 71. Concurrency Test

两个事务：

```text
T1 read version=10
T2 read version=10

T1 update
→ version=11

T2 update
→ affected=0
→ ConcurrencyConflict
```

必须通过。

---

# 72. Outbox Atomicity Test

场景：

```text
Aggregate UPDATE
+
Outbox INSERT
+
COMMIT
```

成功：

```text
state exists
outbox exists
```

失败：

```text
state rollback
outbox rollback
```

不能出现：

```text
state exists
outbox missing
```

---

# 73. Inbox Idempotency Test

第一次：

```text
message M1
→ process
```

第二次：

```text
message M1
→ duplicate
→ no business execution
```

必须通过。

---

# 74. Adapter 不变量

## MYBATIS-001

MyBatis Adapter 不得成为 Domain API。

## MYBATIS-002

Domain 不得依赖 MyBatis。

## MYBATIS-003

Application 不得直接访问 Mapper。

## MYBATIS-004

一个 Aggregate Type 只能存在一个活动 Repository。

## MYBATIS-005

Repository 更新必须支持 Optimistic Concurrency。

## MYBATIS-006

Repository 不得自行提交 Transaction。

## MYBATIS-007

Aggregate State 与 Outbox 必须共享事务。

## MYBATIS-008

Query 不得修改 Aggregate Business State。

## MYBATIS-009

分页必须使用 PaginationDialect。

## MYBATIS-010

业务代码不得判断数据库类型。

## MYBATIS-011

用户排序字段必须经过白名单映射。

## MYBATIS-012

分页不能通过字符串拼接用户输入实现。

## MYBATIS-013

MyBatis Adapter 不得成为第二套 ORM。

## MYBATIS-014

Mapper 属于 Infrastructure。

## MYBATIS-015

Read Model 不要求 Hydrate Aggregate。

---

# 75. 分页架构最终定型

```text
                 PageQuery
                     │
                     ▼
                PageRequest
                     │
                     ▼
              QueryRepository
                     │
                     ▼
           MyBatisQueryExecutor
                     │
                     ▼
          PaginationInterceptor
                     │
                     ▼
          PaginationDialectResolver
                     │
          ┌──────────┼──────────┐
          ▼          ▼          ▼
      PostgreSQL   MySQL      Oracle
          │          │          │
          └──────────┼──────────┘
                     ▼
                   SQL
                     │
                     ▼
                 PageResult
```

---

# 76. Framework 不变量

最终用户看到：

```java
PageResult<OrderView> result =
        repository.query(
                condition,
                new PageRequest(
                        0,
                        20,
                        List.of(
                            new SortOrder(
                                "createdAt",
                                SortOrder.Direction.DESC
                            )
                        ),
                        CountMode.EXACT
                )
        );
```

而无需知道：

```text
PostgreSQL
MySQL
Oracle
SQL Server
DB2
```

---

# 77. 推荐模块最终结构

```text
regalpine-ddd/
│
├── ddd-core
├── ddd-domain
├── ddd-application
├── ddd-cqrs
├── ddd-event
├── ddd-transaction
├── ddd-runtime
├── ddd-messaging
├── ddd-infrastructure
│
├── ddd-infrastructure-jdbc
├── ddd-infrastructure-mybatis
├── ddd-infrastructure-jpa
│
├── ddd-messaging-kafka
├── ddd-messaging-rabbitmq
│
├── ddd-test
├── ddd-conformance
│
├── ddd-spring-boot
│
└── examples/
    └── order-service
```

---

# 78. MyBatis 与 JDBC 的关系

不是：

```text
MyBatis
   ↓
ddd-infrastructure-jdbc
```

而是：

```text
                 ddd-infrastructure
                  /             \
                 /               \
                ↓                 ↓
       JDBC Adapter         MyBatis Adapter
```

两者都是数据库访问 Adapter。

MyBatis 内部当然可以使用 JDBC，但这属于：

```text
MyBatis implementation detail
```

而不是 Framework 模块依赖关系。

---

# 79. MyBatis 与 JPA 的关系

同理：

```text
ddd-infrastructure
       │
       ├── JDBC
       ├── MyBatis
       └── JPA
```

三者互相独立。

---

# 80. 不引入 MyBatis-Plus

RegalPine DDD Framework 的基础 Adapter **不以 MyBatis-Plus 为基础依赖**。

原因：

```text
MyBatis
    =
SQL Mapping Framework

RegalPine DDD
    =
DDD + Application + CQRS + Transaction + Runtime
```

分页、Repository、Concurrency 等由 RegalPine Adapter Contract 管理。

如果未来需要 MyBatis-Plus，可以另建：

```text
ddd-infrastructure-mybatis-plus
```

但当前没有必要。

---

# 81. 与前面架构的对齐

```text
DDD Core
    ↓
Domain
    ↓
Application
    ↓
CQRS
    ↓
Transaction
    ↓
Runtime
    ↓
Infrastructure SPI
    ↓
MyBatis Adapter
```

没有新增：

```text
ORM Domain
DAO Domain
Database Domain
SQL Domain
```

因此不会破坏之前已经冻结的架构。

---

# 82. Implementation Track

本阶段直接进入：

```text
Track M — MyBatis Adapter
```

实施顺序：

```text
M1  Maven Module
 ↓
M2  Session Adapter
 ↓
M3  Transaction Adapter
 ↓
M4  Repository Adapter
 ↓
M5  Mapping
 ↓
M6  Optimistic Concurrency
 ↓
M7  Query Executor
 ↓
M8  Pagination
 ↓
M9  Dialect Resolver
 ↓
M10 Count Strategy
 ↓
M11 Outbox
 ↓
M12 Inbox
 ↓
M13 Conformance Test
 ↓
M14 Integration Test
 ↓
M15 Benchmark
```

---

# 83. 版本收敛原则

Phase XII 不新增：

```text
新的 DDD Entity
新的 Aggregate 模型
新的 CQRS 模型
新的 Event 模型
新的 Transaction 模型
新的 Runtime 模型
```

只实现既有 Contract：

```text
Contract
   ↓
MyBatis Adapter
   ↓
Integration Test
   ↓
Conformance
```

---

# 84. Phase XII Acceptance Criteria

Phase XII 完成必须满足：

- [ ] `ddd-infrastructure-mybatis` 可以独立编译
- [ ] Java 17+
- [ ] MyBatis 可插拔
- [ ] Domain 零 MyBatis 依赖
- [ ] Repository 可运行
- [ ] Optimistic Concurrency 可运行
- [ ] Transaction 可运行
- [ ] UnitOfWork 可运行
- [ ] Outbox 可运行
- [ ] Inbox 可运行
- [ ] PageRequest 可运行
- [ ] PageResult 可运行
- [ ] Offset Pagination 可运行
- [ ] Cursor Pagination SPI 已定义
- [ ] Dialect Resolver 可运行
- [ ] PostgreSQL 方言
- [ ] MySQL 方言
- [ ] Oracle 方言
- [ ] SQL Server 方言
- [ ] DB2 方言
- [ ] Sort whitelist
- [ ] CountMode
- [ ] `LIMIT + 1` 模式
- [ ] Conformance Test
- [ ] Integration Test
- [ ] Benchmark

---

# 85. 当前架构收敛判断

至此：

```text
DDD Model             ████████████████████ Frozen
Application           ████████████████████ Frozen
CQRS                  ████████████████████ Frozen
Event                 ████████████████████ Frozen
Transaction           ████████████████████ Frozen
Runtime               ████████████████████ Frozen
Infrastructure SPI    ████████████████████ Frozen
MyBatis Adapter       ██████████████████░░ Implementation
```

因此从现在开始，**不再通过新增抽象解决问题**。

下一阶段原则：

```text
设计问题
    ↓
是否已有 Contract？
    ↓
Yes → 实现
No  → 只有存在真实语义缺口才增加 Contract
```

最终目标：

```text
Architecture = Frozen
Contract     = Frozen
MyBatis      = Implement
Pagination   = Implement
Tests        = Verify
Benchmark    = Measure
Conformance  = Certify
```

**Phase XII 的核心结论：RegalPine DDD Framework 正式具备 MyBatis 一等适配能力，同时具备统一分页 API + 数据库方言自适应能力；业务代码不感知 MyBatis 和具体数据库。**