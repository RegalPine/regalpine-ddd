# RegalPine DDD Framework
## Phase I — DDD Ontology Formal Model

**GroupId:** `io.github.regalpine.ddd`  
**Framework:** RegalPine DDD Framework  
**Version:** `0.2`  
**Status:** Formal Ontology Baseline  
**Language:** Java 17+  
**Build:** Maven  
**Architecture:** DDD + Four-Layer + Hexagonal + CQRS  
**DSL:** None  

---

# 1. 文档目标

本文正式定义 `io.github.regalpine.ddd` 的 DDD 本体模型。

本规范回答：

1. 什么是领域对象？
2. 什么是 Aggregate？
3. 什么是 Entity？
4. 什么是 Value Object？
5. 什么是 Domain Service？
6. 什么是 Domain Event？
7. 什么是 Repository？
8. 什么是 Command / Query？
9. 什么是 Port / Adapter？
10. 这些概念之间是什么关系？
11. 这些关系具有什么 Domain / Range？
12. 基数是多少？
13. 是否存在逆关系？
14. 是否具有时间语义？
15. 如何验证关系的一致性？
16. 如何将本体模型映射为 Java Framework API？

本规范定义的是：

> **Framework Semantic Model**

而不是：

> DSL / Modeling Language / Code Generation Language。

---

# 2. Ontology Design Principle

本框架采用：

```text
Ontology
   ↓
Semantic Concepts
   ↓
Java Types / Interfaces
   ↓
Runtime
```

而不是：

```text
Ontology
   ↓
DSL
   ↓
Compiler
   ↓
Generated Code
```

因此，本体论属于：

```text
Architecture Semantics
```

而不是：

```text
Developer Programming Syntax
```

---

# 3. Ontology Scope

本体模型分为五个层次：

```text
L0 System
   ↓
L1 Strategic Domain
   ↓
L2 Tactical Domain
   ↓
L3 Application
   ↓
L4 Infrastructure
```

对应：

```text
L0 DDD System
L1 Bounded Context
L2 Aggregate / Entity / ValueObject / DomainService / Event
L3 Command / Query / UseCase / Handler
L4 Port / Adapter / Repository Implementation
```

---

# 4. Top-Level Ontology

定义：

```text
DDDSystem
BoundedContext
DomainObject
Aggregate
Entity
ValueObject
AggregateRoot
DomainService
DomainPolicy
DomainEvent
Repository
Specification

Application
UseCase
Command
CommandHandler
Query
QueryHandler

Port
InputPort
OutputPort
Adapter

Transaction
UnitOfWork
Projection
ReadModel
IntegrationEvent
```

---

# 5. Ontology Type Hierarchy

## 5.1 Root

```text
DDDConcept
├── DDDSystem
├── DomainConcept
├── ApplicationConcept
├── InfrastructureConcept
└── ArchitecturalConcept
```

---

# 6. DomainConcept

```text
DomainConcept
├── DomainObject
│   ├── Entity
│   │   └── AggregateRoot
│   └── ValueObject
│
├── Aggregate
├── DomainService
├── DomainPolicy
├── DomainEvent
├── Specification
└── Repository
```

---

# 7. ApplicationConcept

```text
ApplicationConcept
├── Application
├── UseCase
├── Command
├── CommandHandler
├── Query
├── QueryHandler
├── Projection
├── ReadModel
├── Transaction
└── ExecutionContext
```

---

# 8. InfrastructureConcept

```text
InfrastructureConcept
├── Port
│   ├── InputPort
│   └── OutputPort
│
├── Adapter
├── PersistenceAdapter
├── MessagingAdapter
├── ExternalServiceAdapter
├── CacheAdapter
└── ObservabilityAdapter
```

---

# 9. DDDSystem

## 9.1 Definition

`DDDSystem` 表示一个完整的领域驱动应用系统。

形式化定义：

```text
DDDSystem ::= {
    identity,
    name,
    boundedContexts,
    applications,
    infrastructure
}
```

---

## 9.2 Properties

| Property | Type | Required |
|---|---|---:|
| identity | SystemId | Yes |
| name | String | Yes |
| version | Version | Yes |
| boundedContexts | Set<BoundedContext> | Yes |
| applications | Set<Application> | Yes |

---

# 10. BoundedContext

## 10.1 Definition

Bounded Context 是领域语义边界。

```text
BoundedContext ⊂ DomainArchitecture
```

一个 Context 定义：

```text
Semantic Boundary
+
Model Boundary
+
Language Boundary
+
Consistency Boundary
```

---

# 11. BoundedContext Properties

| Property | Type | Cardinality |
|---|---|---:|
| id | ContextId | 1 |
| name | String | 1 |
| description | String | 0..1 |
| aggregates | Aggregate | 0..* |
| domainServices | DomainService | 0..* |
| domainEvents | DomainEvent | 0..* |
| repositories | Repository | 0..* |

---

# 12. BoundedContext Relationships

```text
DDDSystem
   │
   └── contains ──> BoundedContext
```

Formal:

```text
contains(
    DDDSystem,
    BoundedContext
)
```

Domain:

```text
DDDSystem
```

Range:

```text
BoundedContext
```

Cardinality:

```text
DDDSystem
1 ──── 1..*
BoundedContext
```

---

# 13. Aggregate

## 13.1 Definition

Aggregate 是：

> 具有明确一致性边界、生命周期边界和事务边界的领域对象集合。

形式化：

```text
Aggregate =
{
    AggregateRoot,
    Entities,
    ValueObjects,
    Invariants,
    DomainEvents
}
```

---

# 14. Aggregate Properties

| Property | Type | Cardinality |
|---|---|---:|
| id | AggregateId | 1 |
| root | AggregateRoot | 1 |
| entities | Entity | 0..* |
| valueObjects | ValueObject | 0..* |
| invariants | Invariant | 0..* |
| events | DomainEvent | 0..* |
| version | Version | 1 |

---

# 15. Aggregate Root

每一个 Aggregate：

```text
Aggregate
1 ──── 1
AggregateRoot
```

这是强制约束。

不允许：

```text
Aggregate
1 ──── 0 AggregateRoot
```

也不允许：

```text
Aggregate
1 ──── 2..* AggregateRoot
```

---

# 16. Aggregate Ownership

定义：

```text
owns(
    Aggregate,
    AggregateRoot
)
```

以及：

```text
owns(
    Aggregate,
    Entity
)
```

```text
owns(
    Aggregate,
    ValueObject
)
```

Ownership 表示：

> 生命周期由 Aggregate 管理。

---

# 17. Aggregate Entity Boundary

Aggregate 内部 Entity：

```text
Entity
    belongsTo
Aggregate
```

逆关系：

```text
Aggregate
    owns
Entity
```

因此：

```text
belongsTo ≡ inverse(owns)
```

---

# 18. Aggregate Reference Rule

Aggregate 之间推荐：

```text
Aggregate A
    │
    └── references ──> AggregateId
```

而不是：

```text
Aggregate A
    │
    └── references ──> Aggregate B object
```

正式语义：

```text
AggregateReference
    references
AggregateIdentity
```

---

# 19. Entity

Entity 的核心语义：

```text
Entity =
Identity
+
Lifecycle
+
Behavior
```

形式化：

```text
Entity(x) →
    hasIdentity(x)
    ∧ hasLifecycle(x)
```

---

# 20. Entity Identity

Entity Equality：

```text
x == y
```

主要依据：

```text
identity(x) == identity(y)
```

而不是：

```text
allAttributes(x) == allAttributes(y)
```

---

# 21. Value Object

Value Object：

```text
ValueObject =
Value
+
Semantic Type
```

核心特征：

```text
Immutable
Value Equality
No Independent Lifecycle
```

---

# 22. Value Object Equality

如果：

```text
value(x) == value(y)
```

则：

```text
x ≡ y
```

例如：

```text
Money(100, CNY)
```

与：

```text
Money(100, CNY)
```

具有相同值语义。

---

# 23. Entity / ValueObject Disjointness

同一领域对象不能同时属于：

```text
Entity
```

和：

```text
ValueObject
```

即：

```text
Entity ∩ ValueObject = ∅
```

这是 Ontology Disjointness Constraint。

---

# 24. Aggregate / Entity Relationship

```text
Aggregate
   │
   ├── owns ──> AggregateRoot
   │
   ├── owns ──> Entity
   │
   └── owns ──> ValueObject
```

---

# 25. Aggregate Root / Entity Relationship

由于：

```text
AggregateRoot
```

本质上是特殊 Entity：

```text
AggregateRoot ⊂ Entity
```

因此：

```text
AggregateRoot
isA
Entity
```

但是：

```text
Entity
isA
AggregateRoot
```

不成立。

---

# 26. DomainService

Domain Service：

```text
DomainService
```

用于表达不属于单一 Aggregate 的领域行为。

关系：

```text
DomainService
   │
   └── operatesOn ──> DomainObject
```

DomainObject：

```text
Aggregate
Entity
ValueObject
```

---

# 27. DomainService Constraints

DomainService：

```text
MUST NOT
```

直接依赖：

```text
Database
HTTP Client
Message Broker
Redis
ORM
Spring Container
```

如果需要外部能力：

```text
DomainService
   ↓
OutputPort
   ↓
Adapter
```

---

# 28. DomainPolicy

Policy 表示领域规则。

```text
DomainPolicy
   │
   └── evaluates ──> DomainObject
```

Policy 的结果可以是：

```text
boolean
Decision
DomainError
PolicyResult
```

---

# 29. Specification

Specification：

```text
Specification<T>
```

表示：

```text
Predicate<T>
```

但其语义是业务领域条件，而不是普通技术 Predicate。

---

# 30. Specification Composition

支持：

```text
AND
OR
NOT
```

形成：

```text
Specification A
        │
        ├── AND
        │
Specification B
```

---

# 31. DomainEvent

Domain Event 表示：

> 一个已经发生的领域事实。

定义：

```text
DomainEvent
```

必须具有：

```text
eventId
occurredAt
aggregateId
aggregateType
```

---

# 32. DomainEvent Ownership

```text
Aggregate
   │
   └── emits ──> DomainEvent
```

逆关系：

```text
DomainEvent
   │
   └── originatedFrom ──> Aggregate
```

因此：

```text
emits ≡ inverse(originatedFrom)
```

---

# 33. DomainEvent Temporal Semantics

Domain Event 至少包含：

```text
occurredAt
```

定义：

```text
occurredAt(event)
<
publicationTime(event)
```

通常：

```text
occurredAt
≤
committedAt
≤
publishedAt
```

注意：

> 事件发生时间不等于事件发布成功时间。

---

# 34. Event Ordering

同一 Aggregate：

```text
Aggregate A
   │
   ├── Event 1 sequence=1
   ├── Event 2 sequence=2
   └── Event 3 sequence=3
```

必须满足：

```text
sequence(Event1)
<
sequence(Event2)
<
sequence(Event3)
```

对于同一 Aggregate 的事件序列。

---

# 35. Repository

Repository 是 Aggregate Persistence Port。

```text
Repository
   │
   └── persists ──> Aggregate
```

Domain：

```text
Repository
```

Implementation：

```text
JpaRepositoryAdapter
JdbcRepositoryAdapter
MongoRepositoryAdapter
```

---

# 36. Repository Domain / Range

Formal：

```text
persists(
    Repository<A>,
    Aggregate<A>
)
```

Range：

```text
Aggregate
```

原则：

```text
Repository
MUST NOT
be defined around arbitrary database rows.
```

---

# 37. Application

Application 表示系统 Use Case 执行层。

```text
Application
├── UseCase
├── Command
├── Query
├── Handler
├── Transaction
└── ExecutionContext
```

---

# 38. UseCase

Use Case 表示一个完整业务应用行为。

```text
UseCase
```

例如：

```text
CreateOrder
PayOrder
CancelOrder
ApproveApplication
RegisterCustomer
```

---

# 39. Command

Command：

```text
Command
   │
   └── expressesIntent ──> UseCase
```

例如：

```text
PayOrderCommand
```

表示：

```text
Intent = PayOrder
```

而不是：

```text
SQL UPDATE
```

---

# 40. Command Handler

关系：

```text
CommandHandler
   │
   └── handles ──> Command
```

Cardinality：

```text
Command
1 ──── 1..*
CommandHandler
```

运行时必须最终选择：

```text
1 active handler
```

因此有效运行时关系：

```text
Command
1 ──── 1 CommandHandler
```

---

# 41. Query

Query：

```text
Query
   │
   └── requests ──> ReadModel
```

Query 必须具有：

```text
No Business State Mutation
```

---

# 42. QueryHandler

```text
QueryHandler
   │
   └── handles ──> Query
```

返回：

```text
ReadModel
Projection
View
DTO
```

---

# 43. Command / Query Disjointness

严格定义：

```text
Command ∩ Query = ∅
```

Command：

```text
May change business state
```

Query：

```text
Must not change business state
```

---

# 44. CQRS Ontology

```text
Application
   │
   ├── Command Side
   │      ├── Command
   │      ├── CommandHandler
   │      └── WriteModel
   │
   └── Query Side
          ├── Query
          ├── QueryHandler
          └── ReadModel
```

---

# 45. WriteModel

WriteModel：

```text
Aggregate
```

或者：

```text
Aggregate + Persistence Boundary
```

核心要求：

> Write Model 负责维护业务一致性。

---

# 46. ReadModel

ReadModel：

```text
Optimized for Query
```

可以是：

```text
DTO
Projection
Database View
Denormalized Table
Search Document
Cache Object
```

ReadModel 不需要满足 Aggregate 规则。

---

# 47. Projection

Projection：

```text
DomainEvent
   ↓
Projection
   ↓
ReadModel
```

关系：

```text
Projection
   projects
DomainEvent
   into
ReadModel
```

---

# 48. Projection Temporal Semantics

Projection 可能异步执行：

```text
Event occurred
     ↓
Event committed
     ↓
Event published
     ↓
Projection processed
     ↓
ReadModel updated
```

因此：

```text
ReadModelTime >= EventCommitTime
```

实际系统允许：

```text
EventCommitTime
<
ReadModelUpdateTime
```

形成：

```text
Eventual Consistency
```

---

# 49. Port

Port 是六边形架构中的边界抽象。

```text
Port
├── InputPort
└── OutputPort
```

---

# 50. InputPort

InputPort：

```text
External Actor
      ↓
InputPort
      ↓
Application
```

例如：

```text
CreateOrderUseCase
CommandHandler
QueryHandler
```

---

# 51. OutputPort

OutputPort：

```text
Application / Domain
      ↓
OutputPort
      ↓
Adapter
      ↓
External System
```

例如：

```text
OrderRepository
PaymentGateway
EventPublisher
NotificationGateway
```

---

# 52. Adapter

Adapter 实现 Port。

```text
Adapter
   │
   └── implements ──> Port
```

形式化：

```text
implements(Adapter, Port)
```

---

# 53. Adapter Types

```text
Adapter
├── WebAdapter
├── PersistenceAdapter
├── MessagingAdapter
├── ExternalServiceAdapter
├── CacheAdapter
└── SchedulerAdapter
```

---

# 54. Adapter Dependency Rule

```text
Adapter
    ↓
Port
```

允许：

```text
Adapter
    ↓
Application API
```

禁止：

```text
Domain
    ↓
Adapter
```

---

# 55. Transaction

Transaction 是 Application Execution Boundary。

```text
Transaction
   │
   ├── contains → UseCase
   ├── modifies → Aggregate
   └── commits → DomainEvents
```

默认：

```text
Command
=
Transaction Boundary Candidate
```

---

# 56. UnitOfWork

UnitOfWork：

```text
UnitOfWork
   ├── New
   ├── Dirty
   ├── Removed
   └── Events
```

生命周期：

```text
OPEN
 ↓
ACTIVE
 ↓
COMMITTING
 ↓
COMMITTED
```

异常：

```text
ACTIVE
 ↓
ROLLING_BACK
 ↓
ROLLED_BACK
```

---

# 57. UnitOfWork Temporal Constraint

同一个 UnitOfWork：

```text
startTime
<
commitTime
```

如果：

```text
rollback
```

则：

```text
commitTime = null
```

---

# 58. ExecutionContext

ExecutionContext 表示一次 Application Execution 的上下文。

包括：

```text
tenant
principal
correlation
causation
locale
timezone
trace
```

注意：

> ExecutionContext 不属于 Domain Model。

---

# 59. Context Relationship

```text
BoundedContext
      │
      ├── contains → Aggregate
      ├── contains → DomainService
      ├── contains → DomainEvent
      └── defines → DomainLanguage
```

---

# 60. Context Integration

两个 Context：

```text
Context A
     │
     └── integratesWith
                 │
                 ▼
             Context B
```

Integration 不允许直接共享内部 Aggregate。

---

# 61. Anti-Corruption Layer

```text
Context A
    │
    ▼
ACL
    │
    ▼
Context B
```

ACL：

```text
translates
ExternalModel
→
InternalModel
```

---

# 62. IntegrationEvent

Integration Event：

```text
DomainEvent
      ↓
IntegrationEvent
```

它不是 Domain Event 的简单序列化。

可以进行：

```text
Mapping
Filtering
Enrichment
Versioning
```

---

# 63. Domain Event / Integration Event Disjointness

定义：

```text
DomainEvent ∩ IntegrationEvent = ∅
```

二者可以存在：

```text
derivedFrom
```

关系。

---

# 64. Core Relationship Taxonomy

本框架正式定义以下关系：

```text
contains
owns
belongsTo
hasRoot
containsEntity
containsValueObject

emits
originatedFrom

operatesOn
evaluates
satisfies

persists
loads
removes

handles
targets
requests

projects
produces

implements
dependsOn
invokes

publishes
subscribes

integratesWith
translates
derivesFrom
```

---

# 65. Relationship Metadata

每一个 Framework Ontology Relationship 至少具有：

```text
RelationshipDefinition
├── name
├── sourceType
├── targetType
├── cardinality
├── inverse
├── temporalSemantics
├── transitivity
├── symmetry
└── constraints
```

---

# 66. contains

定义：

```text
contains(A, B)
```

表示：

> B 是 A 的组成部分。

Domain：

```text
DDDSystem
BoundedContext
Aggregate
Application
```

Range：

```text
BoundedContext
Aggregate
DomainObject
UseCase
```

特点：

```text
Transitive = false
Symmetric = false
```

---

# 67. owns

定义：

```text
owns(A, B)
```

表示：

> A 对 B 具有生命周期所有权。

特点：

```text
Inverse = belongsTo
Symmetric = false
Transitive = false
```

---

# 68. belongsTo

定义：

```text
belongsTo(B, A)
```

表示：

> B 属于 A 的生命周期边界。

逆关系：

```text
inverse(owns)
```

---

# 69. hasRoot

定义：

```text
hasRoot(Aggregate, AggregateRoot)
```

约束：

```text
Aggregate → exactly 1 AggregateRoot
```

---

# 70. emits

定义：

```text
emits(Aggregate, DomainEvent)
```

表示 Aggregate 产生领域事实。

逆关系：

```text
originatedFrom
```

---

# 71. operatesOn

定义：

```text
operatesOn(DomainService, DomainObject)
```

Domain：

```text
DomainService
```

Range：

```text
DomainObject
```

---

# 72. evaluates

定义：

```text
evaluates(DomainPolicy, DomainObject)
```

返回：

```text
Decision
```

或者：

```text
boolean
```

---

# 73. persists

定义：

```text
persists(Repository, Aggregate)
```

Repository 是：

```text
OutputPort
```

因此：

```text
Repository ⊂ OutputPort
```

---

# 74. handles

定义：

```text
handles(CommandHandler, Command)
```

或者：

```text
handles(QueryHandler, Query)
```

运行时：

```text
one handler
per dispatchable message type
```

---

# 75. targets

定义：

```text
targets(Command, UseCase)
```

例如：

```text
PayOrderCommand
    targets
PayOrderUseCase
```

---

# 76. projects

定义：

```text
projects(Projection, DomainEvent)
```

表示 Projection 消费事件并产生 ReadModel。

---

# 77. produces

定义：

```text
produces(Projection, ReadModel)
```

---

# 78. implements

定义：

```text
implements(Adapter, Port)
```

这是 Infrastructure 与 Hexagonal Architecture 的关键关系。

---

# 79. dependsOn

`dependsOn` 是架构依赖关系。

允许：

```text
Application → Domain
Infrastructure → Port
Adapter → External Technology
```

禁止：

```text
Domain → Infrastructure
Domain → Adapter
```

---

# 80. invokes

```text
ApplicationService
    invokes
DomainService
```

或者：

```text
CommandHandler
    invokes
Aggregate
```

---

# 81. publishes

```text
EventPublisher
    publishes
DomainEvent
```

或：

```text
MessagingAdapter
    publishes
IntegrationEvent
```

---

# 82. subscribes

```text
EventSubscriber
    subscribes
IntegrationEvent
```

---

# 83. Relationship Consistency

必须满足：

```text
emits(A, E)
⇒
originatedFrom(E, A)
```

以及：

```text
owns(A, E)
⇒
belongsTo(E, A)
```

如果定义了 inverse relationship，则两个方向必须语义一致。

---

# 84. Aggregate Consistency Rules

必须满足：

```text
∀ Aggregate A:
cardinality(hasRoot(A)) = 1
```

并且：

```text
root(A) ∈ Entity
```

---

# 85. Entity Consistency Rules

必须满足：

```text
∀ Entity E:
identity(E) ≠ null
```

Entity 不得：

```text
同时被多个 Aggregate owns
```

即：

```text
∀ E:
|owners(E)| ≤ 1
```

---

# 86. Value Object Consistency Rules

Value Object：

```text
identity-independent
```

不允许要求独立 Aggregate Identity。

---

# 87. Repository Consistency

Repository：

```text
must operate on Aggregate Root boundary
```

禁止：

```text
Repository<Entity>
```

如果 Entity 是 Aggregate 内部 Entity。

允许：

```text
Repository<AggregateRoot>
```

---

# 88. Command Consistency

Command：

```text
must have exactly one semantic intent
```

Command 不应：

```text
直接描述 SQL
```

Command 应：

```text
描述业务意图
```

---

# 89. Query Consistency

Query：

```text
MUST NOT
change Domain state
```

因此：

```text
QueryHandler
```

不能：

```text
save(Aggregate)
publish(DomainEvent)
```

---

# 90. Port Consistency

OutputPort：

```text
must be defined by consumer need
```

因此：

```text
Domain/Application
```

定义 Port。

Infrastructure：

```text
implements Port
```

而不是反过来。

---

# 91. Layer Consistency

正式依赖矩阵：

| From | Domain | Application | Infrastructure | Interface |
|---|---:|---:|---:|---:|
| Domain | ✓ | ✗ | ✗ | ✗ |
| Application | ✓ | ✓ | ✗ | ✗ |
| Infrastructure | ✓* | ✓* | ✓ | ✗ |
| Interface | ✗ | ✓ | ✗* | ✓ |

`*` 表示通过明确 Port / API 边界，而非内部实现依赖。

---

# 92. Hexagonal Consistency

正式规则：

```text
Input Adapter
    ↓
Input Port
    ↓
Application
```

以及：

```text
Application / Domain
    ↓
Output Port
    ↓
Output Adapter
```

---

# 93. Four-Layer / Hexagonal Mapping

| Four-Layer | Hexagonal |
|---|---|
| Interface | Input Adapter |
| Application | Input Port + Application Core |
| Domain | Domain Core |
| Infrastructure | Output Adapter |

因此：

```text
Four-Layer
=
Organization Model
```

而：

```text
Hexagonal
=
Dependency Model
```

---

# 94. CQRS / DDD Mapping

```text
Command
    ↓
Application
    ↓
Aggregate
    ↓
Domain Event
```

Query：

```text
Query
    ↓
Application
    ↓
ReadModel
```

因此：

```text
Command → Domain Model
Query   → Read Model
```

是推荐默认路径。

---

# 95. Transaction Consistency

默认：

```text
One Command
   ↓
One Application Transaction
```

事务内：

```text
Aggregate changes
+
Outbox changes
```

必须原子提交。

---

# 96. Domain Event Consistency

Domain Event 必须：

```text
occurred after domain state transition
```

事件不得在状态变化之前表示“已经发生”。

---

# 97. Outbox Consistency

如果启用 Outbox：

```text
Aggregate State
+
Outbox Event
```

必须属于同一个事务边界。

即：

```text
Commit(Aggregate)
=
Commit(Outbox)
```

---

# 98. Event Publication Consistency

允许：

```text
Commit
<
Publish
```

不允许依赖：

```text
Publish
<
Commit
```

作为默认可靠性模型。

---

# 99. Temporal Model

框架统一定义以下时间：

```text
createdAt
occurredAt
committedAt
publishedAt
processedAt
```

逻辑顺序：

```text
createdAt
 ≤
occurredAt
 ≤
committedAt
 ≤
publishedAt
 ≤
processedAt
```

注意：

> 这是正常成功路径的逻辑顺序，不要求所有时间戳具有严格物理时钟顺序。

---

# 100. Eventual Consistency

CQRS Projection：

```text
Domain State
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

允许：

```text
Domain State ≠ Read Model
```

在短暂时间窗口内成立。

最终：

```text
ReadModel
→
Converges
→
Domain-derived State
```

---

# 101. Strong Consistency Boundary

强一致性默认只要求：

```text
Aggregate
```

内部。

因此：

```text
Aggregate
=
Consistency Boundary
```

而：

```text
BoundedContext
=
Semantic Boundary
```

两者不能混淆。

---

# 102. Ontology Fundamental Distinctions

框架明确区分：

```text
Entity
≠
Aggregate

Aggregate
≠
BoundedContext

DomainEvent
≠
IntegrationEvent

Command
≠
DomainEvent

Query
≠
Command

Repository
≠
DAO

Port
≠
Adapter

DomainService
≠
ApplicationService
```

---

# 103. Domain Service / Application Service

## DomainService

解决：

```text
Business Logic
```

## ApplicationService

解决：

```text
Use Case Orchestration
Transaction
Authorization Boundary
Idempotency
```

因此：

```text
ApplicationService
    invokes
Domain
```

而不是：

```text
DomainService
    invokes
ApplicationService
```

---

# 104. Repository / DAO

DAO：

```text
Persistence Technology Abstraction
```

Repository：

```text
Domain Aggregate Persistence Abstraction
```

因此：

```text
DAO
≠
Repository
```

框架推荐：

```text
Domain
  ↓
Repository
  ↓
Persistence Adapter
  ↓
DAO / ORM
```

---

# 105. Framework Ontology → Java Mapping

| Ontology | Java |
|---|---|
| Entity | `Entity<ID>` |
| ValueObject | `ValueObject` |
| AggregateRoot | `AggregateRoot<ID>` |
| DomainEvent | `DomainEvent` |
| Repository | `Repository<A, ID>` |
| Specification | `Specification<T>` |
| DomainService | `DomainService` |
| Command | `Command<R>` |
| CommandHandler | `CommandHandler<C,R>` |
| Query | `Query<R>` |
| QueryHandler | `QueryHandler<Q,R>` |
| InputPort | `InputPort` |
| OutputPort | `OutputPort` |
| Adapter | Java implementation |
| UnitOfWork | `UnitOfWork` |
| ExecutionContext | `ExecutionContext` |

---

# 106. Ontology Is Not Runtime Metadata

Ontology 本身不要求：

```text
Reflection
Annotation scanning
Runtime graph database
DSL parsing
Code generation
```

框架可以在未来提供：

```text
Architecture Metadata
```

但不能让：

```text
Runtime Ontology
```

成为业务代码运行的必要条件。

---

# 107. Ontology Validation

如果提供架构验证能力，应验证：

```text
Aggregate Root uniqueness
Entity ownership
Layer dependency
Port implementation
Command handler uniqueness
Query handler uniqueness
Event origin
Repository aggregate boundary
CQRS separation
```

---

# 108. Architecture Validation Example

逻辑：

```text
if Domain dependsOn Infrastructure
then INVALID
```

```text
if Aggregate hasRoot count != 1
then INVALID
```

```text
if Entity owners > 1
then INVALID
```

```text
if QueryHandler mutates Aggregate
then INVALID
```

---

# 109. Framework Semantic Kernel

最终本体核心可以收敛到：

```text
                 DDDSystem
                     │
              BoundedContext
                     │
                 Aggregate
              ┌──────┼──────┐
              ▼      ▼      ▼
           Entity    VO    Event
              │
              ▼
        Domain Behavior
              │
              ▼
        Application UseCase
           ┌──┴──┐
           ▼     ▼
       Command  Query
           │     │
           ▼     ▼
       Write   Read
        Model  Model
           │
           ▼
          Ports
           │
           ▼
        Adapters
```

---

# 110. Ontology Kernel

经过第一轮形式化后，核心概念控制在：

```text
DDDSystem
BoundedContext

Aggregate
AggregateRoot
Entity
ValueObject
DomainService
DomainPolicy
DomainEvent
Repository
Specification

Application
UseCase
Command
CommandHandler
Query
QueryHandler
ReadModel
Projection

Port
InputPort
OutputPort
Adapter

Transaction
UnitOfWork
ExecutionContext
IntegrationEvent
```

这组概念作为后续 Framework API 的稳定候选集合。

---

# 111. 收敛原则

从 v0.2 开始：

> 不再通过增加新的 DDD 名词来扩展核心本体。

新能力必须优先通过：

```text
Interface
Composition
Extension
Adapter
Middleware
Policy
```

实现。

例如：

```text
Saga
```

不增加为 Core Domain Object，而作为：

```text
Application Process Extension
```

---

# 112. 本体论最终目标

本体论的最终作用不是让开发人员学习更多概念。

而是保证：

```text
Java API
     ↓
Architecture
     ↓
DDD Semantics
```

三者一致。

---

# 113. Phase I Exit Criteria

Phase I 完成条件：

- [x] DDDSystem
- [x] BoundedContext
- [x] Aggregate
- [x] AggregateRoot
- [x] Entity
- [x] ValueObject
- [x] DomainService
- [x] DomainPolicy
- [x] DomainEvent
- [x] Repository
- [x] Specification
- [x] Command
- [x] Query
- [x] Handler
- [x] Port
- [x] Adapter
- [x] Transaction
- [x] UnitOfWork
- [x] Projection
- [x] ReadModel
- [x] IntegrationEvent
- [x] Relationship semantics
- [x] Domain / Range
- [x] Cardinality
- [x] Inverse relationships
- [x] Temporal semantics
- [x] Consistency constraints
- [x] Four-Layer mapping
- [x] Hexagonal mapping
- [x] CQRS mapping

---

# 114. Phase II

Phase II 不再扩充 Ontology。

进入：

# Core Java API Specification

下一阶段正式定义：

```text
io.github.regalpine.ddd.core
```

包括：

```text
Identifier
Entity
ValueObject
AggregateRoot
DomainEvent
DomainException
Specification
Version
DomainError
```

并正式确定：

```text
interface
abstract class
record
sealed interface
generic
exception
package
```

的 Java 17 API 设计。

然后进入：

```text
ddd-domain
ddd-application
ddd-cqrs
ddd-event
ddd-transaction
ddd-port
```

的正式 API。

---

# 115. Phase I Architecture Decision

本规范最终确定：

```text
                    ┌────────────────────┐
                    │   DDD Ontology     │
                    │  Semantic Layer    │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │ Java Core API      │
                    │ Technical Contract │
                    └─────────┬──────────┘
                              │
              ┌───────────────┼────────────────┐
              ▼               ▼                ▼
         Application         CQRS             Event
              │               │                │
              └───────────────┼────────────────┘
                              ▼
                            Ports
                              │
                              ▼
                          Adapters
                              │
                    ┌─────────┼─────────┐
                    ▼         ▼         ▼
                   DB         MQ       HTTP
```

最终原则：

> **Ontology 定义“是什么”，Java API 定义“怎么使用”，Adapter 定义“怎么接入具体技术”。**

因此 `io.github.regalpine.ddd` 的核心不会演变成 DSL，而会成为真正可以被 Java 项目直接依赖的 **DDD Infrastructure / Development Framework**。