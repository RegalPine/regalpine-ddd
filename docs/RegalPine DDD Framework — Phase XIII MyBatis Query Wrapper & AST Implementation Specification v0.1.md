# RegalPine DDD Framework
## Phase XIII — MyBatis Query Wrapper & AST Implementation Specification v0.1

**GroupId:** `io.github.regalpine.ddd`  
**Java:** 17+  
**Build:** Maven  
**Target Module:** `ddd-infrastructure-mybatis`  
**Status:** Implementation Specification  
**Architecture Status:** Frozen

---

# 1. 本阶段目标

Phase XIII 实现 MyBatis Query Side 的链式查询能力。

目标：

- 提供类似 MyBatis-Plus Wrapper 的开发体验
- 不依赖 MyBatis-Plus
- 不让 Wrapper 进入 Domain
- 使用 Query AST 表达查询语义
- 参数全部使用 Prepared Parameter
- 排序字段采用白名单/类型安全字段
- 支持分页
- 支持 Count
- 支持数据库分页方言
- 支持 Offset / Cursor
- 支持 XML Mapper 与 Annotation Mapper
- 支持 Query Repository
- 支持 Conformance Test

核心链路：

```text
Chain API
    ↓
QueryWrapper
    ↓
Query AST
    ↓
Query Translator
    ↓
MyBatis BoundSql
    ↓
Pagination Dialect
    ↓
Database
```

---

# 2. 设计边界

本阶段明确：

```text
Wrapper = Query Infrastructure Capability
```

不是：

```text
DDD Core Concept
Domain Concept
Aggregate Concept
Repository Domain Contract
```

因此：

```text
ddd-core
    ❌ Wrapper

ddd-domain
    ❌ Wrapper

ddd-application
    △ 可以通过 Query Contract 使用

ddd-cqrs
    ✓ Query

ddd-infrastructure-mybatis
    ✓ Wrapper
```

---

# 3. 最终模块结构

```text
regalpine-ddd/
│
├── ddd-core/
├── ddd-domain/
├── ddd-application/
├── ddd-cqrs/
├── ddd-event/
├── ddd-transaction/
├── ddd-runtime/
├── ddd-messaging/
├── ddd-infrastructure/
│
├── ddd-infrastructure-jdbc/
├── ddd-infrastructure-mybatis/
├── ddd-infrastructure-jpa/
│
├── ddd-test/
├── ddd-conformance/
└── examples/
```

本阶段只修改：

```text
ddd-infrastructure-mybatis
```

以及必要的 Query Contract。

---

# 4. Package Structure

```text
io.github.regalpine.ddd.infrastructure.mybatis.query
│
├── wrapper
│   ├── QueryWrapper
│   ├── LambdaQueryWrapper
│   ├── AbstractQueryWrapper
│   └── NestedQueryWrapper
│
├── field
│   ├── QueryField
│   ├── StringField
│   ├── NumberField
│   ├── DateTimeField
│   └── BooleanField
│
├── ast
│   ├── QueryNode
│   ├── ConditionNode
│   ├── ComparisonNode
│   ├── NullNode
│   ├── InNode
│   ├── BetweenNode
│   ├── LikeNode
│   ├── LogicalNode
│   ├── NestedNode
│   ├── OrderNode
│   ├── SelectNode
│   └── PaginationNode
│
├── parameter
│   ├── QueryParameter
│   ├── ParameterBinding
│   └── ParameterBindings
│
├── translator
│   ├── QueryTranslator
│   ├── SqlQueryTranslator
│   ├── TranslationContext
│   └── TranslationResult
│
├── pagination
│   ├── Pagination
│   ├── OffsetPagination
│   ├── CursorPagination
│   ├── CountMode
│   └── PageQueryExecutor
│
└── repository
    └── MyBatisPageQueryRepository
```

---

# 5. QueryField

不允许用户直接传递任意 SQL。

定义：

```java
public interface QueryField<T> {

    String column();

    Class<T> javaType();
}
```

默认实现：

```java
public record DefaultQueryField<T>(
        String column,
        Class<T> javaType
) implements QueryField<T> {

    public DefaultQueryField {
        if (column == null || column.isBlank()) {
            throw new IllegalArgumentException(
                    "column must not be blank");
        }

        if (javaType == null) {
            throw new IllegalArgumentException(
                    "javaType must not be null");
        }
    }
}
```

---

# 6. Field Factory

提供：

```java
public final class QueryFields {

    private QueryFields() {
    }

    public static <T> QueryField<T> of(
            String column,
            Class<T> type) {

        return new DefaultQueryField<>(
                column,
                type
        );
    }
}
```

---

# 7. 业务字段定义

推荐：

```java
public final class OrderFields {

    private OrderFields() {
    }

    public static final QueryField<String> ID =
            QueryFields.of("id", String.class);

    public static final QueryField<String> CUSTOMER_ID =
            QueryFields.of("customer_id", String.class);

    public static final QueryField<String> STATUS =
            QueryFields.of("status", String.class);

    public static final QueryField<Instant> CREATED_AT =
            QueryFields.of("created_at", Instant.class);

    public static final QueryField<Long> VERSION =
            QueryFields.of("version", Long.class);
}
```

---

# 8. 为什么不用字符串字段

不推荐：

```java
wrapper.eq("customer_id", customerId);
```

推荐：

```java
wrapper.eq(
    OrderFields.CUSTOMER_ID,
    customerId
);
```

原因：

- SQL Injection 防护
- 字段白名单
- 统一数据库列名
- 类型信息
- 重构安全
- IDE 自动补全

---

# 9. Query AST

Wrapper 不直接生成 SQL。

必须：

```text
Wrapper
  ↓
AST
  ↓
Translator
  ↓
SQL
```

核心：

```java
public sealed interface QueryNode
        permits ConditionNode,
                LogicalNode,
                NestedNode,
                OrderNode,
                SelectNode,
                PaginationNode {
}
```

---

# 10. ConditionNode

```java
public sealed interface ConditionNode
        extends QueryNode
        permits ComparisonNode,
                NullNode,
                InNode,
                BetweenNode,
                LikeNode {
}
```

---

# 11. ComparisonNode

```java
public record ComparisonNode<T>(
        QueryField<T> field,
        Operator operator,
        Object value
) implements ConditionNode {

    public enum Operator {
        EQ,
        NE,
        GT,
        GE,
        LT,
        LE
    }
}
```

---

# 12. NullNode

```java
public record NullNode(
        QueryField<?> field,
        boolean negated
) implements ConditionNode {
}
```

语义：

```text
negated=false
    → IS NULL

negated=true
    → IS NOT NULL
```

---

# 13. InNode

```java
public record InNode<T>(
        QueryField<T> field,
        List<T> values,
        boolean negated
) implements ConditionNode {

    public InNode {
        values = List.copyOf(values);
    }
}
```

生成：

```sql
status IN (?, ?, ?)
```

而不是：

```sql
status IN ('A','B','C')
```

---

# 14. Empty IN

必须定义：

```text
IN ()
```

不是合法 SQL。

因此：

```java
wrapper.in(field, List.of());
```

默认语义：

```text
FALSE
```

而：

```java
wrapper.notIn(field, List.of());
```

默认语义：

```text
TRUE
```

禁止生成：

```sql
IN ()
```

---

# 15. BetweenNode

```java
public record BetweenNode<T>(
        QueryField<T> field,
        T lower,
        T upper,
        boolean negated
) implements ConditionNode {
}
```

生成：

```sql
created_at BETWEEN ? AND ?
```

或者：

```sql
created_at NOT BETWEEN ? AND ?
```

---

# 16. LikeNode

```java
public record LikeNode(
        QueryField<String> field,
        String value,
        LikeMode mode,
        boolean negated
) implements ConditionNode {

    public enum LikeMode {
        ANYWHERE,
        START,
        END
    }
}
```

例如：

```java
like(OrderFields.CUSTOMER_ID, "abc")
```

最终：

```sql
customer_id LIKE ?
```

参数：

```text
%abc%
```

---

# 17. LIKE 转义

必须支持：

```text
%
_
\
```

用户输入：

```text
100%
```

不能被解释为通配符。

Framework 应提供：

```java
public interface LikeEscaper {

    String escape(String value);
}
```

---

# 18. LogicalNode

```java
public record LogicalNode(
        LogicalOperator operator,
        List<ConditionNode> children
) implements QueryNode {

    public enum LogicalOperator {
        AND,
        OR
    }
}
```

---

# 19. NestedNode

复杂查询：

```java
wrapper
    .eq(A, 1)
    .and(w ->
        w.eq(B, 2)
         .or()
         .eq(C, 3)
    );
```

AST：

```text
AND
├── A = 1
└── Nested
    └── OR
        ├── B = 2
        └── C = 3
```

SQL：

```sql
A = ?
AND (
    B = ?
    OR C = ?
)
```

---

# 20. QueryWrapper

核心 API：

```java
public interface QueryWrapper<T> {

    QueryWrapper<T> eq(
            QueryField<?> field,
            Object value);

    QueryWrapper<T> ne(
            QueryField<?> field,
            Object value);

    QueryWrapper<T> gt(
            QueryField<?> field,
            Object value);

    QueryWrapper<T> ge(
            QueryField<?> field,
            Object value);

    QueryWrapper<T> lt(
            QueryField<?> field,
            Object value);

    QueryWrapper<T> le(
            QueryField<?> field,
            Object value);

    QueryWrapper<T> isNull(
            QueryField<?> field);

    QueryWrapper<T> isNotNull(
            QueryField<?> field);

    QueryWrapper<T> in(
            QueryField<?> field,
            Collection<?> values);

    QueryWrapper<T> notIn(
            QueryField<?> field,
            Collection<?> values);

    QueryWrapper<T> between(
            QueryField<?> field,
            Object lower,
            Object upper);

    QueryWrapper<T> like(
            QueryField<String> field,
            String value);

    QueryWrapper<T> and(
            Consumer<QueryWrapper<T>> consumer);

    QueryWrapper<T> or(
            Consumer<QueryWrapper<T>> consumer);

    QueryWrapper<T> orderByAsc(
            QueryField<?> field);

    QueryWrapper<T> orderByDesc(
            QueryField<?> field);

    QueryWrapper<T> select(
            QueryField<?>... fields);
}
```

---

# 21. 静态 Factory

```java
public final class Wrappers {

    private Wrappers() {
    }

    public static <T> QueryWrapper<T> query() {
        return new DefaultQueryWrapper<>();
    }
}
```

使用：

```java
var wrapper =
        Wrappers.<OrderRecord>query()
            .eq(OrderFields.STATUS, "PAID")
            .orderByDesc(OrderFields.CREATED_AT);
```

---

# 22. 条件式链

推荐增加：

```java
eq(
    boolean condition,
    QueryField<?> field,
    Object value
)
```

例如：

```java
wrapper
    .eq(customerId != null,
        OrderFields.CUSTOMER_ID,
        customerId)
    .eq(status != null,
        OrderFields.STATUS,
        status);
```

这样避免：

```java
if (customerId != null) {
    ...
}
```

---

# 23. LambdaQueryWrapper

为了避免业务代码直接引用数据库列名，提供 Lambda 形式。

目标体验：

```java
Wrappers.<OrderRecord>lambdaQuery()
    .eq(OrderRecord::customerId, customerId)
    .eq(OrderRecord::status, status)
    .orderByDesc(OrderRecord::createdAt);
```

但 Java 17 的 Method Reference 本身无法可靠地从任意 getter 推导数据库列名。

因此 Lambda API 必须建立在显式 Field Metadata 上。

---

# 24. 类型安全 Lambda Field

推荐：

```java
public interface Property<T, R> {

    R get(T value);
}
```

但是：

```java
OrderRecord::customerId
```

无法天然得到：

```text customer_id
```

因此必须通过：

```java
FieldMetadataRegistry
```

或者编译期代码生成。

---

# 25. Phase XIII Lambda 策略

本版本采用：

```text
QueryField
    ↓
类型安全字段定义
```

暂不依赖反射推断 Java Bean → SQL。

例如：

```java
public final class OrderFields {

    public static final QueryField<String> CUSTOMER_ID =
        QueryFields.of(
            "customer_id",
            String.class
        );
}
```

后续如果需要：

```text
annotation processor
```

再提供自动生成。

**不为了 Lambda API 引入运行时反射黑魔法。**

---

# 26. OrderBy AST

```java
public record OrderNode(
        QueryField<?> field,
        Direction direction
) implements QueryNode {

    public enum Direction {
        ASC,
        DESC
    }
}
```

SQL：

```sql
ORDER BY created_at DESC, id DESC
```

---

# 27. OrderBy 白名单

QueryField 本身就是白名单。

因此：

```java
orderByDesc(
    OrderFields.CREATED_AT
);
```

合法。

而：

```java
orderByDesc(
    QueryFields.of(
        "created_at; DROP TABLE orders",
        String.class
    )
);
```

必须通过 Field Registry 验证。

Framework 推荐：

```text
Application-defined fields
    ↓
registered fields
    ↓
QueryWrapper
```

---

# 28. Select AST

```java
public record SelectNode(
        List<QueryField<?>> fields
) implements QueryNode {

    public SelectNode {
        fields = List.copyOf(fields);
    }
}
```

默认：

```text
SELECT *
```

显式：

```java
.select(
    OrderFields.ID,
    OrderFields.STATUS,
    OrderFields.CREATED_AT
)
```

生成：

```sql
SELECT
    id,
    status,
    created_at
```

---

# 29. SQL Translator

```java
public interface QueryTranslator {

    TranslationResult translate(
            QueryWrapper<?> wrapper);
}
```

结果：

```java
public record TranslationResult(
        String sqlFragment,
        List<ParameterBinding> parameters
) {
}
```

---

# 30. ParameterBinding

```java
public record ParameterBinding(
        String name,
        Object value,
        Class<?> javaType
) {
}
```

生成：

```text
WHERE status = #{p1}
AND customer_id = #{p2}
```

参数：

```text
p1 → PAID
p2 → C001
```

---

# 31. 禁止 SQL 拼接参数

绝对禁止：

```java
sql + "'" + value + "'"
```

也禁止：

```java
sql + value
```

所有值：

```text
Value
 ↓
ParameterBinding
 ↓
Prepared Parameter
```

---

# 32. Translator 输出

例如：

```java
Wrappers.<OrderRecord>query()
    .eq(OrderFields.CUSTOMER_ID, "C001")
    .eq(OrderFields.STATUS, "PAID")
    .orderByDesc(OrderFields.CREATED_AT);
```

AST：

```text
AND
├── customer_id = ?
├── status = ?
└── ORDER BY created_at DESC
```

Translator：

```sql
customer_id = #{p1}
AND status = #{p2}
ORDER BY created_at DESC
```

Parameters：

```text
p1 = C001
p2 = PAID
```

---

# 33. Pagination Contract

```java
public sealed interface Pagination
        permits OffsetPagination,
                CursorPagination {
}
```

---

# 34. OffsetPagination

```java
public record OffsetPagination(
        long page,
        long size
) implements Pagination {

    public OffsetPagination {
        if (page < 0) {
            throw new IllegalArgumentException(
                "page must be >= 0");
        }

        if (size <= 0) {
            throw new IllegalArgumentException(
                "size must be > 0");
        }
    }

    public long offset() {
        return Math.multiplyExact(page, size);
    }
}
```

---

# 35. CursorPagination

```java
public record CursorPagination(
        String cursor,
        int size
) implements Pagination {

    public CursorPagination {
        if (size <= 0) {
            throw new IllegalArgumentException(
                "size must be > 0");
        }
    }
}
```

---

# 36. Wrapper 分页

Wrapper 提供：

```java
QueryWrapper<T> page(
        int page,
        int size
);
```

例如：

```java
var query =
    Wrappers.<OrderRecord>query()
        .eq(OrderFields.STATUS, "PAID")
        .orderByDesc(OrderFields.CREATED_AT)
        .page(0, 20);
```

但是：

**Wrapper 保存 Pagination AST，不执行查询。**

---

# 37. PageQueryExecutor

```java
public interface PageQueryExecutor {

    <T> PageResult<T> execute(
            QueryWrapper<?> wrapper,
            Class<T> resultType
    );
}
```

---

# 38. Query Repository

```java
public interface MyBatisPageQueryRepository {

    <T> PageResult<T> query(
            QueryWrapper<?> wrapper,
            Class<T> resultType
    );
}
```

---

# 39. Query 执行链

```text
QueryHandler
    ↓
QueryRepository
    ↓
QueryWrapper
    ↓
Query AST
    ↓
QueryTranslator
    ↓
Base SQL
    ↓
Pagination
    ↓
Dialect
    ↓
MyBatis
    ↓
Result Mapping
    ↓
PageResult<T>
```

---

# 40. MyBatis Mapper

基础 Mapper：

```java
public interface DynamicQueryMapper {

    List<Map<String, Object>> select(
            @Param("sql") String sql,
            @Param("parameters") Map<String, Object> parameters
    );
}
```

但生产实现**不建议允许任意 SQL**。

Framework 推荐：

```text
MappedStatement
+
Static Base SQL
+
AST generated WHERE/ORDER
```

而不是：

```text
user SQL → ${sql}
```

---

# 41. `${}` 安全规则

禁止：

```xml
${sql}
```

接受用户输入。

允许 Framework 内部生成并验证：

```text
column
operator
order
pagination
```

且必须经过 AST/Registry。

---

# 42. Base SQL

推荐 Repository 定义：

```java
public interface OrderQueryMapper {

    List<OrderView> selectOrders(
            @Param("customerId")
            String customerId
    );

    long countOrders(
            @Param("customerId")
            String customerId
    );
}
```

Wrapper 可以作为附加 Query AST：

```text
Base SQL
+
Generated WHERE
+
Generated ORDER
+
Generated Pagination
```

---

# 43. 更推荐的 Mapper 模式

定义 Query Statement：

```java
public record QueryStatement(
        String statementId,
        String baseSql,
        Class<?> resultType
) {
}
```

例如：

```text
order.selectOrders
```

Base SQL：

```sql
SELECT
    id,
    customer_id,
    status,
    created_at
FROM orders
```

Framework 添加：

```sql
WHERE ...
ORDER BY ...
LIMIT ...
```

---

# 44. 为什么不完全动态 SQL

完全动态 SQL：

```text
Wrapper → 完整 SQL
```

容易演变成：

```text
MyBatis Plus clone
```

RegalPine 的目标是：

```text
Business Query
+
Controlled Query AST
+
Static SQL structure
```

因此：

**Wrapper 是 Query Builder，不是 SQL Builder。**

---

# 45. 分页方言集成

最终：

```text
Query AST
 ↓
Base SQL
 ↓
WHERE
 ↓
ORDER BY
 ↓
PaginationNode
 ↓
PaginationDialect
```

PostgreSQL：

```sql
LIMIT ? OFFSET ?
```

MySQL：

```sql
LIMIT ? OFFSET ?
```

Oracle：

```sql
OFFSET ? ROWS
FETCH NEXT ? ROWS ONLY
```

SQL Server：

```sql
OFFSET ? ROWS
FETCH NEXT ? ROWS ONLY
```

---

# 46. Count 查询

Wrapper：

```java
var query =
    Wrappers.<OrderRecord>query()
        .eq(OrderFields.STATUS, "PAID")
        .page(0, 20);
```

Framework：

```text
COUNT SQL
+
DATA SQL
```

Count：

```sql
SELECT COUNT(*)
FROM orders
WHERE status = ?
```

Data：

```sql
SELECT ...
FROM orders
WHERE status = ?
ORDER BY created_at DESC
LIMIT ? OFFSET ?
```

---

# 47. CountMode

```java
public enum CountMode {

    EXACT,
    NONE,
    ESTIMATED,
    AUTO
}
```

---

# 48. NONE 优化

使用：

```text
LIMIT size + 1
```

例如：

```text
size = 20
```

执行：

```sql
LIMIT 21
```

得到：

```text
21 rows → hasNext=true
20 rows → hasNext=false
```

避免：

```sql
COUNT(*)
```

---

# 49. Cursor Wrapper

支持：

```java
var query =
    Wrappers.<OrderRecord>query()
        .gt(OrderFields.ID, lastId)
        .orderByAsc(OrderFields.ID)
        .cursor(lastId, 50);
```

但 Cursor 不应该简单理解为：

```text
WHERE id > cursor
```

真正的 Cursor：

```text
Cursor
 ↓
Decoded Keyset
 ↓
Query Predicate
 ↓
ORDER BY
 ↓
LIMIT
```

---

# 50. Cursor 签名

生产环境推荐：

```text
cursor payload
+
version
+
query fingerprint
+
signature
```

防止：

```text
cursor tampering
```

Cursor 不应让客户端任意修改排序条件。

---

# 51. Query Fingerprint

Cursor 可以绑定：

```text
query definition
sort
tenant
```

例如：

```text
fingerprint =
SHA-256(
    queryType
    +
    sort
    +
    tenant
)
```

如果客户端拿另一个 Query 的 Cursor：

```text
fingerprint mismatch
→ reject
```

---

# 52. Tenant Isolation

如果 Framework 应用启用 Tenant Context：

```text
Tenant
 ↓
Query
 ↓
Wrapper
```

Tenant 条件不得由普通用户 Wrapper 随意覆盖。

推荐：

```text
System Predicate
    AND
User Predicate
```

例如：

```sql
WHERE tenant_id = ?
AND status = ?
```

而不是：

```text
User Wrapper
    OR tenant predicate
```

导致越权。

---

# 53. System Predicate

定义为 Infrastructure/Application 安全能力：

```java
public interface QueryPredicateProvider {

    List<ConditionNode> predicates();
}
```

用途：

- Tenant isolation
- Soft delete
- Data scope
- Security boundary

---

# 54. Soft Delete

例如：

```text
deleted = false
```

系统 Predicate：

```sql
WHERE deleted = false
```

用户：

```java
.eq(OrderFields.STATUS, "PAID")
```

最终：

```sql
WHERE deleted = false
AND status = ?
```

---

# 55. 数据权限

如果启用 Data Scope：

```text
System Predicate
      ↓
Department Scope
      ↓
Tenant Scope
      ↓
User Query
```

全部进入 AST。

避免直接拼 SQL。

---

# 56. Wrapper 与 Domain Specification

两者严格不同：

```text
Specification<T>
    =
Domain business rule

QueryWrapper<T>
    =
Persistence query construction
```

例如：

```java
Order.canBeCancelled()
```

是 Domain。

而：

```java
wrapper.eq(
    OrderFields.STATUS,
    "PAID"
)
```

是 Query Infrastructure。

禁止互相替代。

---

# 57. Wrapper 与 Repository

Domain Repository：

```java
Optional<Order> findById(OrderId id);
```

不接受：

```java
find(QueryWrapper<Order>)
```

Query Repository：

```java
PageResult<OrderView> query(
    QueryWrapper<?> wrapper
);
```

这是 CQRS 分离。

---

# 58. MyBatis-Plus Compatibility

本 Framework：

```text
兼容思想
≠
兼容 API
```

借鉴：

```text
eq
ne
gt
ge
lt
le
in
between
like
and
or
orderBy
page
```

但不依赖：

```text
MyBatis-Plus
```

因此：

```text
MyBatis
+
RegalPine Query Wrapper
```

可以独立运行。

---

# 59. 不实现的 API

第一版不实现：

```text
UpdateWrapper
DeleteWrapper
LambdaUpdateWrapper
LambdaDeleteWrapper
SQL Update Builder
SQL Delete Builder
JOIN DSL
Full SQL DSL
```

原因：

Query Wrapper 首先服务于：

```text
CQRS Read Side
Pagination
Read Model
```

Update/Delete 必须受到 Aggregate/Command 语义约束。

---

# 60. JOIN

Query Side 可以使用 JOIN。

但第一版不把 JOIN 设计成复杂 DSL。

推荐：

```text
Mapper/Base SQL
    +
Wrapper WHERE/ORDER/Pagination
```

例如：

```sql
SELECT
    o.id,
    c.name,
    o.status
FROM orders o
JOIN customers c
    ON c.id = o.customer_id
```

Wrapper：

```java
.eq(
    OrderFields.STATUS,
    "PAID"
)
```

---

# 61. Wrapper 生命周期

Wrapper 是短生命周期对象：

```text
create
 ↓
build
 ↓
execute
 ↓
discard
```

不允许：

```text
Singleton Wrapper
```

不保证：

```text
Thread Safe
```

因此：

**QueryWrapper 不应在多个线程共享。**

---

# 62. Immutability

AST 节点全部：

```text
immutable
```

Wrapper 本身：

```text
mutable builder
```

因此：

```text
Wrapper
    → mutable

AST
    → immutable
```

执行时：

```text
Wrapper.freeze()
    ↓
Immutable QueryPlan
```

---

# 63. QueryPlan

```java
public record QueryPlan(
        List<QueryNode> nodes,
        Pagination pagination,
        CountMode countMode
) {
    public QueryPlan {
        nodes = List.copyOf(nodes);
    }
}
```

执行前：

```text
Wrapper
 ↓
QueryPlan
```

QueryPlan 可以缓存/审计/测试。

---

# 64. Query Fingerprint

QueryPlan 可以生成：

```java
String fingerprint()
```

用于：

- Cursor
- Query Cache
- Observability
- SQL Plan Metrics
- Debugging

Fingerprint 不应包含真实参数值。

---

# 65. Query Cache

后续可以支持：

```text
QueryPlan fingerprint
+
parameter hash
```

但：

**Query Cache 不是 Phase XIII 必需能力。**

不为了缓存增加 Wrapper 复杂度。

---

# 66. 性能目标

Wrapper 本身：

```text
No reflection
No runtime proxy
No database access
```

目标：

```text
Wrapper build
    O(number of conditions)
```

AST：

```text
O(number of nodes)
```

Translator：

```text
O(number of nodes)
```

---

# 67. Benchmark

至少测试：

```text
10 conditions
50 conditions
100 conditions
500 conditions
```

比较：

```text
Wrapper creation
AST creation
Translation
Parameter binding
```

目标是 Query Builder 开销相对于数据库访问可以忽略。

---

# 68. 测试结构

```text
src/test/java/
└── io.github.regalpine.ddd.infrastructure.mybatis.query
    │
    ├── wrapper
    │   ├── QueryWrapperTest
    │   ├── NestedQueryTest
    │   ├── ConditionalQueryTest
    │   └── OrderByTest
    │
    ├── ast
    │   └── QueryAstTest
    │
    ├── translator
    │   ├── ComparisonTranslatorTest
    │   ├── LogicalTranslatorTest
    │   ├── LikeTranslatorTest
    │   └── ParameterBindingTest
    │
    ├── pagination
    │   ├── OffsetPaginationTest
    │   ├── CursorPaginationTest
    │   └── CountModeTest
    │
    ├── dialect
    │   ├── PostgreSqlDialectTest
    │   ├── MySqlDialectTest
    │   ├── OracleDialectTest
    │   └── SqlServerDialectTest
    │
    └── security
        ├── SortInjectionTest
        ├── ParameterInjectionTest
        └── CursorTamperingTest
```

---

# 69. Wrapper 测试

```java
var query =
    Wrappers.<OrderRecord>query()
        .eq(OrderFields.STATUS, "PAID")
        .ge(OrderFields.CREATED_AT, start)
        .orderByDesc(OrderFields.CREATED_AT);
```

验证 AST：

```text
status = ?
AND created_at >= ?
ORDER BY created_at DESC
```

---

# 70. Nested 测试

输入：

```java
query
    .eq(A, 1)
    .and(w ->
        w.eq(B, 2)
         .or()
         .eq(C, 3)
    );
```

必须生成：

```sql
A = ?
AND (
    B = ?
    OR C = ?
)
```

不能生成：

```sql
A = ?
AND B = ?
OR C = ?
```

---

# 71. SQL Injection Test

输入：

```text
field =
"status DESC; DROP TABLE orders"
```

必须：

```text
reject
```

值：

```text
status = "PAID' OR '1'='1"
```

必须作为参数：

```sql
status = ?
```

而不是 SQL：

```sql
status = 'PAID' OR '1'='1'
```

---

# 72. Pagination Test

验证：

```text
page=0 size=20
→ offset=0

page=1 size=20
→ offset=20

page=10 size=50
→ offset=500
```

同时验证 overflow：

```java
Math.multiplyExact()
```

溢出：

```text
→ IllegalArgumentException
```

---

# 73. Count Test

```text
EXACT
→ count query

NONE
→ no count query

AUTO
→ strategy

ESTIMATED
→ dialect-specific
```

---

# 74. Dialect Test

输入：

```text
baseSql = SELECT * FROM orders
offset = 20
limit = 10
```

PostgreSQL：

```sql
SELECT * FROM orders
LIMIT ? OFFSET ?
```

Oracle：

```sql
SELECT * FROM orders
OFFSET ? ROWS
FETCH NEXT ? ROWS ONLY
```

---

# 75. Integration Test 数据库

推荐 Testcontainers 作为 Integration Test 工具。

测试：

```text
PostgreSQL
MySQL
Oracle XE / compatible test environment
SQL Server
```

但：

**Testcontainers 不进入 Framework Runtime Production Dependency。**

---

# 76. Conformance Contract

所有 PaginationDialect 必须通过：

```text
PaginationDialectConformanceTest
```

所有 MyBatis Repository：

```text
MyBatisRepositoryConformanceTest
```

所有 Query Executor：

```text
QueryExecutorConformanceTest
```

---

# 77. Formal Rules

## WRAPPER-001

Wrapper 只属于 Query Infrastructure。

## WRAPPER-002

Wrapper 不得进入 Domain Model。

## WRAPPER-003

Wrapper 不得直接生成包含用户值的 SQL。

## WRAPPER-004

Query Value 必须 Parameter Binding。

## WRAPPER-005

排序字段必须经过 Field Registry。

## WRAPPER-006

AST 必须与 SQL Translator 分离。

## WRAPPER-007

AST 节点必须 immutable。

## WRAPPER-008

Wrapper 默认不是 Thread Safe。

## WRAPPER-009

空 IN 不得生成非法 SQL。

## WRAPPER-010

LIKE 参数必须正确 Escape。

## WRAPPER-011

Pagination 必须经过 Dialect。

## WRAPPER-012

Query Wrapper 不承担 Transaction。

## WRAPPER-013

Query Wrapper 不承担 Authorization。

## WRAPPER-014

Query Wrapper 不承担 Domain Business Rule。

## WRAPPER-015

Query Wrapper 不得自动修改 Aggregate。

---

# 78. Pagination Rules

## PAGE-001

分页请求必须验证 page/size。

## PAGE-002

Offset 计算必须检测整数溢出。

## PAGE-003

分页 SQL 必须由 Dialect 生成。

## PAGE-004

COUNT 不得被强制绑定到所有分页请求。

## PAGE-005

NONE 模式允许通过 `size + 1` 判断 hasNext。

## PAGE-006

Cursor 必须使用稳定排序。

## PAGE-007

Cursor 不得跨 Query Definition 随意复用。

## PAGE-008

大规模数据推荐 Cursor/Keyset。

---

# 79. Security Rules

## QUERY-SEC-001

用户输入不得直接进入 SQL。

## QUERY-SEC-002

Sort 字段必须白名单。

## QUERY-SEC-003

Sort Direction 只能 ASC/DESC。

## QUERY-SEC-004

Column Name 不得来自未经验证的用户输入。

## QUERY-SEC-005

Cursor 必须防篡改。

## QUERY-SEC-006

Tenant Predicate 必须由系统控制。

## QUERY-SEC-007

Soft Delete Predicate 不得被普通 Query Wrapper 绕过。

---

# 80. 最终开发体验

典型 Query：

```java
PageResult<OrderView> result =
    orderQueryRepository.query(
        Wrappers.<OrderRecord>query()
            .eq(
                OrderFields.CUSTOMER_ID,
                customerId
            )
            .eq(
                OrderFields.STATUS,
                "PAID"
            )
            .ge(
                OrderFields.CREATED_AT,
                startTime
            )
            .le(
                OrderFields.CREATED_AT,
                endTime
            )
            .like(
                OrderFields.CUSTOMER_NAME,
                keyword
            )
            .orderByDesc(
                OrderFields.CREATED_AT
            )
            .orderByDesc(
                OrderFields.ID
            )
            .page(0, 20)
    );
```

业务开发者不需要知道：

```text
MyBatis
SQL Session
PreparedStatement
LIMIT
OFFSET
Oracle OFFSET
PostgreSQL LIMIT
MySQL LIMIT
COUNT
```

---

# 81. 最终运行链路

```text
ListOrdersQuery
        │
        ▼
ListOrdersQueryHandler
        │
        ▼
OrderQueryRepository
        │
        ▼
QueryWrapper
        │
        ▼
Immutable QueryPlan
        │
        ▼
Query AST
        │
        ▼
SQL Translator
        │
        ├── WHERE
        ├── ORDER BY
        └── Parameters
        │
        ▼
PaginationDialect
        │
        ▼
MyBatis
        │
        ▼
Database
        │
        ▼
OrderView
        │
        ▼
PageResult<OrderView>
```

---

# 82. 与 Framework 总体架构对齐

```text
                 DDD
                  │
       ┌──────────┴──────────┐
       │                     │
   Write Side             Read Side
       │                     │
 Aggregate              QueryWrapper
       │                     │
 Repository              Query AST
       │                     │
 Transaction             MyBatis
       │                     │
 Outbox                  Pagination
       │                     │
 Database               Read Model
```

CQRS 边界保持清晰：

```text
Write Side
    → Aggregate

Read Side
    → Query
    → Wrapper
    → Read Model
```

---

# 83. Phase XIII 完成标准

必须满足：

- [x] QueryWrapper 设计
- [x] 链式 API
- [x] Query AST
- [x] QueryField
- [x] 类型安全字段
- [x] 条件表达式
- [x] AND / OR / Nested
- [x] IN / NOT IN
- [x] BETWEEN
- [x] LIKE
- [x] ORDER BY
- [x] Select
- [x] Pagination
- [x] CountMode
- [x] Offset
- [x] Cursor SPI
- [x] Query Translator
- [x] Parameter Binding
- [x] Pagination Dialect
- [x] Sort 安全
- [x] Tenant Predicate SPI
- [x] MyBatis Integration Boundary
- [x] Conformance Test 设计

---

# 84. 本阶段不再增加的内容

以下暂不进入 Framework Core：

```text
UpdateWrapper
DeleteWrapper
复杂 JOIN DSL
完整 SQL DSL
MyBatis-Plus Dependency
ORM Entity Enhancement
Runtime Reflection Mapping
通用 SQL Builder
通用 Database Abstraction Layer
```

只有真实实现需求出现时才考虑。

---

# 85. 架构收敛状态

```text
DDD Model                 ████████████████████ Frozen
Application               ████████████████████ Frozen
CQRS                      ████████████████████ Frozen
Event                     ████████████████████ Frozen
Transaction               ████████████████████ Frozen
Runtime                   ████████████████████ Frozen
Infrastructure SPI        ████████████████████ Frozen
MyBatis Adapter            ████████████████████ Frozen
Query Wrapper              ████████████████████ Frozen
Query AST                  ████████████████████ Frozen
Pagination                 ████████████████████ Frozen
Dialect                    ████████████████████ Frozen
Implementation             ███████████████░░░░░ In Progress
```

---

# 86. 下一实施阶段

从 Phase XIV 开始进入实际代码落地：

```text
Phase XIV
    ↓
ddd-infrastructure-mybatis
完整 Java 17 源码
    ↓
Phase XV
    ↓
MyBatis + PostgreSQL Integration
    ↓
Phase XVI
    ↓
MyBatis + MySQL / Oracle / SQL Server
    ↓
Phase XVII
    ↓
Conformance + Benchmark
```

不再重新定义：

```text
DDD
CQRS
Repository
Transaction
Event
Runtime
Wrapper
Pagination
```

后续工作原则：

```text
Frozen Contract
      ↓
Production Implementation
      ↓
Test
      ↓
Benchmark
      ↓
Conformance
```

---

# 87. 最终结论

RegalPine DDD Framework 的 MyBatis 适配层最终采用：

```text
MyBatis
+
RegalPine QueryWrapper
+
Query AST
+
Type-safe QueryField
+
Pagination
+
Cursor/Keyset
+
Count Strategy
+
Dialect Resolver
+
Tenant Predicate
+
Conformance Test
```

而不是：

```text
MyBatis-Plus
+
Domain
+
ORM Wrapper
```

因此 Framework 可以获得：

**链式查询体验 + 类型安全 + SQL 参数安全 + CQRS Read Model + 自适应分页 + 多数据库方言 + MyBatis 解耦**

同时保持核心架构不变。

> **Phase XIII 至此完成 Wrapper/Query AST 设计冻结，下一阶段直接进入 Java 17 实际源码实现。**