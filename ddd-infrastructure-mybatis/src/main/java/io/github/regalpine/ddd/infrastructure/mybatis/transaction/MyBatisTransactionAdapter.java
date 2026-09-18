package io.github.regalpine.ddd.infrastructure.mybatis.transaction;

import io.github.regalpine.ddd.infrastructure.mybatis.session.MyBatisSessionAdapter;
import io.github.regalpine.ddd.transaction.TransactionAdapter;
import io.github.regalpine.ddd.transaction.TransactionDefinition;
import org.apache.ibatis.session.SqlSession;
import java.util.Objects;

/** 独立 MyBatis 事务适配器；不可与 Spring 管理的会话混用。 */
public final class MyBatisTransactionAdapter implements TransactionAdapter {
    private final MyBatisSessionAdapter sessions;
    public MyBatisTransactionAdapter(MyBatisSessionAdapter sessions) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }
    @Override public void begin(TransactionDefinition definition) {
        sessions.openSession(Objects.requireNonNull(definition, "definition"));
    }
    @Override public void commit() { require().commit(true); }
    @Override public void rollback() {
        if (isActive()) require().rollback(true);
    }
    @Override public void close() { sessions.closeSession(); }
    @Override public boolean isActive() { return sessions.hasCurrentSession(); }
    @Override public Object suspend() { return sessions.suspend(); }
    @Override public void resume(Object resource) { sessions.resume((SqlSession) resource); }
    private SqlSession require() {
        return Objects.requireNonNull(sessions.currentSession(), "没有活动 MyBatis 事务");
    }
}
