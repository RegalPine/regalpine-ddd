-- 消息基础设施表结构（应用层管理）
-- 应用层通过迁移工具（如 Flyway/Liquibase）执行此脚本
-- 框架不自动创建或修改表结构

CREATE TABLE IF NOT EXISTS outbox (
    outbox_id VARCHAR(128) PRIMARY KEY,
    event_id VARCHAR(128) NOT NULL UNIQUE,
    event_type VARCHAR(255) NOT NULL,
    event_version INTEGER NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255),
    payload TEXT NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING','IN_FLIGHT','PUBLISHED','DEAD')),
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    published_at TIMESTAMP WITH TIME ZONE,
    destination VARCHAR(512) NOT NULL,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    lease_token VARCHAR(128),
    lease_until TIMESTAMP WITH TIME ZONE,
    last_error TEXT
);

CREATE INDEX IF NOT EXISTS outbox_pending_idx ON outbox(status, next_attempt_at, created_at);
CREATE INDEX IF NOT EXISTS outbox_lease_idx ON outbox(status, lease_until);

CREATE TABLE IF NOT EXISTS inbox (
    message_id VARCHAR(128) NOT NULL,
    consumer_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    error TEXT,
    PRIMARY KEY (consumer_id, message_id)
);
