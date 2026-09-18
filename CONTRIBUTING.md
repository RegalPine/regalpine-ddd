# Contributing to RegalPine DDD Framework

Thank you for your interest in contributing to RegalPine DDD Framework! This document
provides guidelines and information for contributors.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
- [Commit Conventions](#commit-conventions)
- [Pull Request Process](#pull-request-process)
- [Reporting Issues](#reporting-issues)
- [License](#license)

## Code of Conduct

This project adheres to the [Contributor Covenant Code of Conduct](./CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/<your-username>/regalpine-ddd.git
   cd regalpine-ddd
   ```
3. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Setup

### Prerequisites

| Tool | Minimum Version | Purpose |
|------|----------------|---------|
| JDK | 17 (Temurin) | Compilation & runtime |
| Maven | 3.8+ | Build & dependency management |
| Git | 2.30+ | Version control |

### Build & Test

```bash
# Full build with tests
mvn clean verify

# Build without tests (faster iteration)
mvn clean package -DskipTests

# Run tests for a single module
mvn test -pl ddd-core

# Run a specific test class
mvn test -pl ddd-core -Dtest=AggregateRootTest
```

### Running the Example

```bash
cd examples/order-service
mvn spring-boot:run
```

## Project Structure

```
ddd-core/                    Core building blocks: Aggregate, Entity, ValueObject, Event, Specification
ddd-domain/                  Domain layer: Repository, DomainService, Factory, Policy, Rule, Decision
ddd-application/             Application layer: Command/Query handlers, Idempotency, Authorization
ddd-cqrs/                    CQRS engine: CommandBus, QueryBus, middleware pipeline
ddd-event/                   Event infrastructure: Outbox, Inbox, Envelope, Handler, Projection
ddd-transaction/             Transaction management: UnitOfWork, TransactionAdapter
ddd-messaging/               Messaging abstraction: BrokerAdapter, MessageDispatch, routing
ddd-runtime/                 Runtime lifecycle: bootstrap, service registry
ddd-infrastructure/          Infrastructure SPIs: Clock, Identifier, ExceptionTranslator, PersistenceAdapter
ddd-infrastructure-mybatis/  MyBatis adapter: repository, AST query wrapper, adaptive pagination
ddd-infrastructure-jdbc/     JDBC adapter: outbox store, transaction adapter
ddd-messaging-kafka/         Kafka broker adapter
ddd-messaging-rabbitmq/      RabbitMQ broker adapter
ddd-spring-boot/             Spring Boot auto-configuration
ddd-test/                    Test utilities and fixtures
ddd-conformance/             SPI conformance test suites
examples/order-service/      Example application
docs/                        Specification documents and technical design docs
```

## Coding Standards

### Java

- **Source level**: JDK 17
- **Encoding**: UTF-8 (enforced by maven-compiler-plugin)
- **Warnings**: Minimize compiler warnings; no `@SuppressWarnings` without justification
- **Null safety**: Use `Objects.requireNonNull()` for public API parameters
- **Thread safety**: Document thread-safety guarantees in Javadoc for shared components
- **License header**: All new `.java` files SHOULD include:
  ```java
  /*
   * Copyright 2026 Qingsong Wang
   * SPDX-License-Identifier: Apache-2.0
   */
  ```

### DDD Patterns

- **Aggregate boundaries**: Aggregates must enforce their own invariants; never expose internal entities directly
- **Domain events**: Raise events from aggregate methods; do not mutate state from event handlers
- **Repository SPI**: Repositories operate on aggregate roots only; never expose persistence concerns
- **Value Objects**: Must be immutable and implement structural equality

### Testing

- **Framework**: JUnit 5 (Jupiter) + AssertJ assertions + Mockito
- **Naming**: Test classes end with `Test`; use descriptive method names (`should_reject_order_when_total_is_zero`)
- **Isolation**: Unit tests must not require network access or external services
- **Conformance**: New adapter SPIs must include a conformance test suite in `ddd-conformance`

### Specification Changes

Any change to core SPI interfaces or conformance contracts requires:

1. Update the relevant Phase specification document in `docs/`
2. Update the SPI interface with backward-compatible handling where possible
3. Add or update unit tests covering old and new behavior
4. Update the conformance suite if the SPI contract changed

## Commit Conventions

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`, `ci`

**Scopes**: `core`, `domain`, `application`, `cqrs`, `event`, `transaction`, `messaging`, `runtime`, `infrastructure`, `mybatis`, `jdbc`, `kafka`, `rabbitmq`, `spring-boot`, `conformance`, `docs`

Examples:
```
feat(cqrs): add middleware pipeline with retry and logging support
fix(event): prevent duplicate outbox entry on concurrent aggregate save
docs(domain): clarify aggregate root lifecycle in Phase III specification
```

## Pull Request Process

1. Ensure all CI checks pass locally before pushing
2. Keep PRs focused — one logical change per PR
3. Fill in the PR template completely
4. Add tests for new functionality or bug fixes
5. Update documentation if behavior changes
6. Squash-merge is preferred; maintainers will squash on merge
7. A maintainer must approve before merge

### PR Checklist

- [ ] `mvn clean verify` passes (build + tests)
- [ ] No new compiler warnings
- [ ] New source files include SPDX license header
- [ ] Documentation updated (if applicable)
- [ ] Specification docs updated (if SPI changed)
- [ ] No secrets or credentials in code

## Reporting Issues

- Use GitHub Issues for bug reports and feature requests
- For **security vulnerabilities**, do NOT open a public issue — see [SECURITY.md](./SECURITY.md)

## AI-Assisted Development Disclosure

Portions of this project's documentation and implementation code were developed
with the assistance of AI tools. All AI-assisted output has been reviewed,
tested, and approved by the project maintainer(s), who take full responsibility
for correctness and license compliance under Apache-2.0.

Contributors are welcome to use AI tools in their workflow. We ask that you:

- Review and understand all AI-generated code before submitting
- Ensure the output meets the same quality bar as hand-written code
- Do not include AI tool metadata or prompts in committed code

## License

By contributing, you agree that your contributions will be licensed under the
[Apache License, Version 2.0](./LICENSE). No separate Contributor License
Agreement (CLA) is required — the Apache-2.0 inbound=outbound policy applies
(Section 5 of the license).
