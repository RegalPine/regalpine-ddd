# RegalPine DDD Framework

[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/java-17%2B-orange.svg)](https://adoptium.net)
[![Maven Central](https://img.shields.io/badge/maven--central-1.0.0-blueviolet.svg)](https://central.sonatype.com/artifact/io.github.regalpine.ddd/regalpine-ddd/1.0.0)

**[English](./README.md)**

面向企业级 Java 应用的领域驱动设计（DDD）基础设施框架。
提供从战术模式（聚合、实体、值对象）到战略基础设施（CQRS、事件溯源、事务管理、消息传递、持久化适配器）
的完整构建块，帮助团队构建可维护、可测试、可扩展的领域中心化系统。

## 特性

- **完整的 DDD 战术模式** — 聚合、实体、值对象、领域事件、规约
- **领域层抽象** — 仓储、领域服务、工厂、策略、规则、决策
- **应用层** — 命令 / 查询分离、幂等性、授权钩子、事务编排
- **CQRS 与事件架构** — 命令总线、查询总线、中间件管线、Outbox / Inbox 模式、投影
- **事务与工作单元** — 声明式事务管理、变更追踪、一致性边界
- **消息传递** — Broker 无关的消息抽象，提供 Kafka 和 RabbitMQ 适配器
- **持久化适配器** — 基于 AST 的 MyBatis 查询封装、自适应分页、方言支持
- **运行时与生命周期** — 框架运行时启动、生命周期管理、Spring Boot 自动装配
- **符合性测试套件** — 针对所有适配器 SPI 的标准化符合性测试

## 架构

```
┌──────────────────────────────────────────────────────────────────────┐
│                    Application (Spring Boot)                          │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-spring-boot  │  自动装配 & Starter                               │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-runtime      │  生命周期 │ 注册中心 │ 启动引导                    │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-application  │  命令 │ 查询 │ 幂等 │ 授权                        │
│  ddd-cqrs         │  命令总线 │ 查询总线 │ 中间件管线                  │
│  ddd-event        │  Outbox │ Inbox │ 投影 │ 信封                     │
│  ddd-messaging    │  Broker 抽象 │ 分发 │ 路由                        │
│  ddd-transaction  │  工作单元 │ 事务适配器 │ 一致性                     │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-domain       │  仓储 │ 领域服务 │ 工厂 │ 策略                     │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-core         │  聚合 │ 实体 │ 值对象 │ 事件 │ 规约                │
│  ddd-infrastructure │  时钟 │ 标识 │ 异常 │ 持久化 SPI                 │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-infrastructure-mybatis │  MyBatis 适配器 │ 查询 AST │ 自适应分页   │
│  ddd-infrastructure-jdbc    │  JDBC Outbox │ JDBC 事务                │
│  ddd-messaging-kafka        │  Kafka Broker 适配器                    │
│  ddd-messaging-rabbitmq     │  RabbitMQ Broker 适配器                 │
├──────────────────────────────────────────────────────────────────────┤
│  ddd-test         │  测试工具 & 固定数据                               │
│  ddd-conformance  │  SPI 符合性测试套件                                │
└──────────────────────────────────────────────────────────────────────┘
```

## 模块说明

| 模块 | 职责 |
|------|------|
| `ddd-core` | 核心构建块：Aggregate、Entity、ValueObject、DomainEvent、Specification、Identifier、Pagination |
| `ddd-domain` | 领域层：Repository、DomainService、Factory、Policy、Rule、Decision 接口 |
| `ddd-application` | 应用层：Command/Query 处理器、幂等性、授权、事务编排 |
| `ddd-cqrs` | CQRS 引擎：CommandBus、QueryBus、中间件管线、处理器注册表 |
| `ddd-event` | 事件基础设施：Outbox、Inbox、Envelope、Handler、Projection |
| `ddd-transaction` | 事务管理：UnitOfWork、TransactionAdapter、一致性边界 |
| `ddd-messaging` | 消息抽象：BrokerAdapter、MessageDispatch、路由 |
| `ddd-runtime` | 运行时生命周期：启动引导、服务注册、组件管理 |
| `ddd-infrastructure` | 基础设施 SPI：Clock、Identifier、ExceptionTranslator、PersistenceAdapter |
| `ddd-infrastructure-mybatis` | MyBatis 适配器：仓储实现、AST 查询封装、自适应分页、方言支持 |
| `ddd-infrastructure-jdbc` | JDBC 适配器：Outbox 存储、事务适配器 |
| `ddd-messaging-kafka` | Kafka Broker 适配器 |
| `ddd-messaging-rabbitmq` | RabbitMQ Broker 适配器 |
| `ddd-spring-boot` | Spring Boot 自动装配 |
| `ddd-test` | 测试工具与固定数据 |
| `ddd-conformance` | 适配器 SPI 符合性测试套件 |
| `examples/order-service` | 完整集成示例应用 |

## 快速开始

### 环境要求

- JDK 17+（Temurin / OpenJDK）
- Maven 3.8+

### 构建

```bash
# 构建所有模块并运行测试
mvn clean verify

# 跳过测试加速构建
mvn clean package -DskipTests
```

### 使用

在项目中添加核心依赖：

```xml
<dependency>
    <groupId>io.github.regalpine.ddd</groupId>
    <artifactId>ddd-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

Spring Boot 集成：

```xml
<dependency>
    <groupId>io.github.regalpine.ddd</groupId>
    <artifactId>ddd-spring-boot</artifactId>
    <version>1.0.0</version>
</dependency>
```

完整示例参见 [examples/order-service](./examples/order-service)。

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Java 17 |
| 持久化 | MyBatis 3.5.16 |
| 测试 | JUnit 5 + AssertJ + Mockito |
| 构建 | Maven 3.8+ |
| 框架集成 | Spring Boot |

## 文档

- [Phase I — DDD 本体形式化模型](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20I%20DDD%20Ontology%20Formal%20Model%20v0.2.md)
- [Phase II — ddd-core Java API 规范](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20II%20ddd-core%20Java%20API%20Specification%20v0.3.md)
- [Phase III — ddd-domain 架构与 API](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20III%20ddd-domain%20Architecture%20&%20API%20Specification%20v0.1.md)
- [Phase IV — 应用层与用例](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20IV%20Application%20Layer%20&%20Use%20Case%20Specification%20v0.1.md)
- [Phase V — CQRS 与事件架构](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20V%20CQRS%20&%20Event%20Architecture%20Specification%20v0.1.md)
- [Phase VI — 事务、工作单元与一致性](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20VI%20Transaction,%20Unit%20of%20Work%20&%20Consistency%20Specification%20v0.1.md)
- [Phase VII — 持久化与仓储适配器](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20VII%20Persistence%20&%20Repository%20Adapter%20Specification%20v0.1.md)
- [Phase VIII — 基础设施与适配器运行时](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20VIII%20Infrastructure%20&%20Adapter%20Runtime%20Specification%20v0.1.md)
- [Phase IX — 集成与消息传递](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20IX%20Integration%20&%20Messaging%20Specification%20v0.1.md)
- [Phase X — 开发者框架与符合性](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20X%20Developer%20Framework%20Implementation%20&%20Conformance%20Specification%20v0.1.md)
- [Phase XI — 参考实现](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20XI%20Reference%20Implementation%20Specification%20v0.1.md)
- [Phase XII — MyBatis 适配器与自适应分页](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20XII%20MyBatis%20Adapter%20&%20Adaptive%20Pagination%20Specification%20v0.1.md)
- [Phase XIII — MyBatis 查询封装与 AST](./docs/RegalPine%20DDD%20Framework%20—%20Phase%20XIII%20MyBatis%20Query%20Wrapper%20&%20AST%20Implementation%20Specification%20v0.1.md)
- [符合性报告](./docs/RegalPine%20DDD%20Framework%20符合性报告%20v1.0.md)

## 参与贡献

欢迎贡献！请先阅读：

- [贡献指南](./CONTRIBUTING.md) — 环境搭建、编码规范、PR 流程
- [行为准则](./CODE_OF_CONDUCT.md) — 社区行为规范
- [安全策略](./SECURITY.md) — 漏洞报告流程

## AI 辅助开发声明

本项目的文档及部分实现代码在开发过程中使用了 AI 工具辅助。
所有 AI 辅助产出均经过项目维护者审核、测试与批准，维护者对其正确性及
Apache-2.0 许可合规性承担全部责任。

## 许可证

[Apache License, Version 2.0](./LICENSE)

Copyright (c) 2026 Qingsong Wang
