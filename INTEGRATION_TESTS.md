# 集成测试指南

## 概述

本项目使用 Testcontainers 进行集成测试，需要 Docker 环境。测试会自动启动 PostgreSQL、Kafka、RabbitMQ 容器。

## 前提条件

- Docker Desktop 或兼容的容器运行时
- Docker Compose v2+
- 至少 4GB 可用内存

## 运行测试

### 1. 运行所有单元测试（不需要 Docker）

```bash
mvn test
```

### 2. 运行集成测试（需要 Docker）

```bash
# 方式一：使用 profile 启用集成测试
mvn verify -Pintegration-tests

# 方式二：直接运行 integration-test 阶段
mvn integration-test

# 方式三：运行特定模块的集成测试
mvn verify -Pintegration-tests -pl ddd-infrastructure-jdbc
```

### 3. 跳过集成测试

默认情况下，`mvn test` 会跳过集成测试。如需显式跳过：

```bash
mvn test -DexcludedGroups=integration
```

## 测试分类

测试使用 JUnit 5 的 `@Tag` 注解分类：

- `@Tag("integration")` - 需要 Docker 的集成测试
- 无标签 - 普通单元测试

## 测试模块

### ddd-infrastructure-jdbc

- **OutboxLeaseIntegrationTest** - Outbox 租约机制测试
  - 租约竞争（FOR UPDATE SKIP LOCKED）
  - 指数退避重试
  - 死信处理
  
- **OutboxWorkerIntegrationTest** - Outbox Worker 端到端测试
  - 完整发布流程
  - Broker 失败重试
  - 事务一致性

## 故障排查

### Docker 不可用

如果 Docker 未启动或不可用，集成测试会自动跳过，并显示警告：

```
[WARN] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
```

### 容器启动失败

如果容器启动失败，检查：

1. Docker 服务是否运行
2. 端口是否被占用（5432, 9092, 5672）
3. 内存是否充足

```bash
# 查看 Testcontainers 日志
docker logs <container-id>
```

### 清理残留容器

```bash
# 清理所有测试容器
docker ps -a | grep testcontainers | awk '{print $1}' | xargs docker rm -f
```

## CI/CD 集成

### GitHub Actions

```yaml
- name: Run integration tests
  uses: actions/setup-java@v3
  with:
    distribution: 'temurin'
    java-version: '17'
    
- name: Start Docker
  run: |
    sudo systemctl start docker
    
- name: Run tests
  run: mvn verify -Pintegration-tests
```

### GitLab CI

```yaml
integration-test:
  stage: test
  services:
    - docker:dind
  variables:
    DOCKER_HOST: tcp://docker:2375
    DOCKER_DRIVER: overlay2
  script:
    - mvn verify -Pintegration-tests
```

## 测试数据清理

每个测试方法执行后会自动清理：

- Testcontainers 容器在测试类结束后销毁
- 数据库表在每个测试前重建

## 性能考虑

- 首次运行会下载容器镜像（约 500MB）
- 后续运行使用缓存镜像
- 启用 `testcontainers.reuse.enable=true` 可复用容器
