# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-09-18

### Added

- **ddd-core**: Aggregate root, Entity, ValueObject, DomainEvent, Specification, Identifier (UUID / Long), Pagination, Audit fields, Versioning
- **ddd-domain**: Repository, DomainService, Factory, Policy, Rule, Decision interfaces with generic type contracts
- **ddd-application**: Command / Query handlers, Idempotency support, Authorization hooks, Transaction orchestration, Application context
- **ddd-cqrs**: CommandBus, QueryBus, middleware pipeline (logging, validation, retry, authorization), handler registry
- **ddd-event**: Outbox pattern with lease-based claiming, Inbox pattern with atomic consume, Event envelope, Handler dispatch, Projection support
- **ddd-transaction**: UnitOfWork with change tracking, TransactionAdapter SPI, consistency boundary management, 6 propagation modes (REQUIRED, REQUIRES_NEW, NOT_SUPPORTED, SUPPORTS, MANDATORY, NEVER), rollback-only support, transaction synchronizations, timeout
- **ddd-messaging**: Broker-agnostic messaging abstraction, MessageDispatch, routing, channel management, SubscriptionHandle
- **ddd-runtime**: Framework runtime bootstrap, lifecycle management, component registry, OutboxWorker with exponential backoff, LeasedOutboxDispatcher, TransactionalCommandBus
- **ddd-infrastructure**: Clock SPI, Identifier generator, ExceptionTranslator, PersistenceAdapter SPI, Inbox/Outbox store SPIs, InMemoryTransactionManager with full propagation
- **ddd-infrastructure-mybatis**: MyBatis adapter with repository implementation, AST-based query wrapper, adaptive pagination, multi-dialect support (MySQL, PostgreSQL, Oracle, SQL Server, H2), pagination interceptor with ThreadLocal context
- **ddd-infrastructure-jdbc**: JDBC outbox store with FOR UPDATE SKIP LOCKED lease claiming, JDBC transaction adapter with isolation level / read-only / timeout mapping, JDBC inbox store with ON CONFLICT DO NOTHING
- **ddd-messaging-kafka**: Kafka broker adapter with acks=all, idempotent producer, manual offset management
- **ddd-messaging-rabbitmq**: RabbitMQ broker adapter with publisher confirms, manual ACK, prefetch control
- **ddd-spring-boot**: Spring Boot auto-configuration with conditional assembly, Outbox Worker configuration, TransactionalCommandBus decoration
- **ddd-test**: Test utilities and fixtures
- **ddd-conformance**: Standardized conformance test suites for Repository, PersistenceMapper, TransactionAdapter, Messaging, InboxStore, OutboxStore
- **examples/order-service**: Complete example application demonstrating full framework integration
- Documentation: 13-phase specification covering ontology model, core API, domain layer, application layer, CQRS, transactions, persistence, infrastructure, messaging, conformance, reference implementation, MyBatis adapter, and query AST
- Maven Central publishing support (GPG signed, sources & javadoc attached)
- Integration test framework with Testcontainers (PostgreSQL)

[1.0.0]: https://central.sonatype.com/artifact/io.github.regalpine.ddd/regalpine-ddd/1.0.0
