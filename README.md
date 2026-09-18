# RegalPine DDD Framework

[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/java-17%2B-orange.svg)](https://adoptium.net)
[![Maven Central](https://img.shields.io/badge/maven--central-1.0.0-blueviolet.svg)](https://central.sonatype.com/artifact/io.github.regalpine.ddd/regalpine-ddd/1.0.0)

**[中文文档](./README.zh-CN.md)**

A comprehensive Domain-Driven Design (DDD) infrastructure framework for enterprise Java applications.
Provides a complete set of building blocks — from tactical patterns (Aggregates, Entities, Value Objects)
to strategic infrastructure (CQRS, Event Sourcing, Transaction Management, Messaging, Persistence Adapters) —
enabling teams to build maintainable, testable, and scalable domain-centric systems.

## Features

- **Complete DDD Tactical Patterns** — Aggregates, Entities, Value Objects, Domain Events, Specifications
- **Domain Layer Abstractions** — Repositories, Domain Services, Factories, Policies, Rules, Decisions
- **Application Layer** — Command / Query separation, Idempotency, Authorization hooks, Transaction orchestration
- **CQRS & Event Architecture** — Command bus, query bus, middleware pipeline, outbox / inbox pattern, projections
- **Transaction & Unit of Work** — Declarative transaction management, change tracking, consistency boundaries
- **Messaging** — Broker-agnostic messaging abstraction with Kafka and RabbitMQ adapters
- **Persistence Adapters** — MyBatis adapter with AST-based query wrapper, adaptive pagination, dialect support
- **Runtime & Lifecycle** — Framework runtime bootstrap, lifecycle management, Spring Boot auto-configuration
- **Conformance Suite** — Standardized conformance tests for all adapter SPIs

## Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                    Application (Spring Boot)                          │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-spring-boot  │  Auto-configuration & starters                   │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-runtime      │  Lifecycle │ Registry │ Bootstrap                │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-application  │  Commands │ Queries │ Idempotency │ Auth         │
│  ddd-cqrs         │  Command Bus │ Query Bus │ Middleware Pipeline   │
│  ddd-event        │  Outbox │ Inbox │ Projections │ Envelopes        │
│  ddd-messaging    │  Broker Abstraction │ Dispatch │ Routing          │
│  ddd-transaction  │  Unit of Work │ Transaction Adapter │ Consistency │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-domain       │  Repositories │ Services │ Factories │ Policies   │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-core         │  Aggregate │ Entity │ ValueObject │ Event │ Spec  │
│  ddd-infrastructure │  Clock │ ID │ Exception │ Persistence SPI       │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-infrastructure-mybatis │  MyBatis Adapter │ Query AST │ Pagination│
│  ddd-infrastructure-jdbc    │  JDBC Outbox │ JDBC Transaction        │
│  ddd-messaging-kafka        │  Kafka Broker Adapter                  │
│  ddd-messaging-rabbitmq     │  RabbitMQ Broker Adapter               │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-test         │  Test utilities & fixtures                       │
│  ddd-conformance  │  SPI conformance test suites                     │
└──────────────────────────────────────────────────────────────────────┘
```

## Modules

| Module | Description |
|--------|-------------|
| `ddd-core` | Core building blocks: Aggregate, Entity, ValueObject, DomainEvent, Specification, Identifier, Pagination |
| `ddd-domain` | Domain layer: Repository, DomainService, Factory, Policy, Rule, Decision interfaces |
| `ddd-application` | Application layer: Command/Query handlers, Idempotency, Authorization, Transaction orchestration |
| `ddd-cqrs` | CQRS engine: CommandBus, QueryBus, middleware pipeline, handler registry |
| `ddd-event` | Event infrastructure: Outbox, Inbox, Envelope, Handler, Projection |
| `ddd-transaction` | Transaction management: UnitOfWork, TransactionAdapter, consistency boundaries |
| `ddd-messaging` | Messaging abstraction: BrokerAdapter, MessageDispatch, routing |
| `ddd-runtime` | Runtime lifecycle: bootstrap, service registry, component management |
| `ddd-infrastructure` | Infrastructure SPIs: Clock, Identifier, ExceptionTranslator, PersistenceAdapter |
| `ddd-infrastructure-mybatis` | MyBatis adapter: repository implementation, AST query wrapper, adaptive pagination, dialect support |
| `ddd-infrastructure-jdbc` | JDBC adapter: outbox store, transaction adapter |
| `ddd-messaging-kafka` | Kafka broker adapter |
| `ddd-messaging-rabbitmq` | RabbitMQ broker adapter |
| `ddd-spring-boot` | Spring Boot auto-configuration |
| `ddd-test` | Test utilities and fixtures |
| `ddd-conformance` | Conformance test suites for adapter SPIs |
| `examples/order-service` | Example application demonstrating full integration |

## Quick Start

### Prerequisites

- JDK 17+ (Temurin / OpenJDK)
- Maven 3.8+

### Build

```bash
# Build all modules and run tests
mvn clean verify

# Skip tests for faster build
mvn clean package -DskipTests
```

### Usage

Add the core dependency to your project:

```xml
<dependency>
    <groupId>io.github.regalpine.ddd</groupId>
    <artifactId>ddd-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

For Spring Boot integration:

```xml
<dependency>
    <groupId>io.github.regalpine.ddd</groupId>
    <artifactId>ddd-spring-boot</artifactId>
    <version>1.0.0</version>
</dependency>
```

See [examples/order-service](./examples/order-service) for a complete demo.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Persistence | MyBatis 3.5.16 |
| Testing | JUnit 5 + AssertJ + Mockito |
| Build | Maven 3.8+ |
| Framework Integration | Spring Boot |

## Documentation

- [Phase I — DDD Ontology Formal Model](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20I%20DDD%20Ontology%20Formal%20Model%20v0.2.md)
- [Phase II — ddd-core Java API Specification](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20II%20ddd-core%20Java%20API%20Specification%20v0.3.md)
- [Phase III — ddd-domain Architecture & API](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20III%20ddd-domain%20Architecture%20&%20API%20Specification%20v0.1.md)
- [Phase IV — Application Layer & Use Case](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20IV%20Application%20Layer%20&%20Use%20Case%20Specification%20v0.1.md)
- [Phase V — CQRS & Event Architecture](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20V%20CQRS%20&%20Event%20Architecture%20Specification%20v0.1.md)
- [Phase VI — Transaction, Unit of Work & Consistency](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20VI%20Transaction,%20Unit%20of%20Work%20&%20Consistency%20Specification%20v0.1.md)
- [Phase VII — Persistence & Repository Adapter](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20VII%20Persistence%20&%20Repository%20Adapter%20Specification%20v0.1.md)
- [Phase VIII — Infrastructure & Adapter Runtime](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20VIII%20Infrastructure%20&%20Adapter%20Runtime%20Specification%20v0.1.md)
- [Phase IX — Integration & Messaging](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20IX%20Integration%20&%20Messaging%20Specification%20v0.1.md)
- [Phase X — Developer Framework & Conformance](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20X%20Developer%20Framework%20Implementation%20&%20Conformance%20Specification%20v0.1.md)
- [Phase XI — Reference Implementation](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20XI%20Reference%20Implementation%20Specification%20v0.1.md)
- [Phase XII — MyBatis Adapter & Adaptive Pagination](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20XII%20MyBatis%20Adapter%20&%20Adaptive%20Pagination%20Specification%20v0.1.md)
- [Phase XIII — MyBatis Query Wrapper & AST](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20XIII%20MyBatis%20Query%20Wrapper%20&%20AST%20Implementation%20Specification%20v0.1.md)
- [Conformance Report](./docs/RegalPine%20DDD%20Framework%20符合性报告%20v1.0.md)

## Contributing

Contributions are welcome! Please read our:

- [Contributing Guide](./CONTRIBUTING.md) — setup, coding standards, PR process
- [Code of Conduct](./CODE_OF_CONDUCT.md) — community expectations
- [Security Policy](./SECURITY.md) — vulnerability reporting

## AI-Assisted Development

Portions of this project's documentation and implementation code were developed
with the assistance of AI tools. All AI-assisted output has been reviewed,
tested, and approved by the project maintainer(s), who take full responsibility
for correctness and license compliance under Apache-2.0.

## License

[Apache License, Version 2.0](./LICENSE)

Copyright (c) 2026 Qingsong Wang
