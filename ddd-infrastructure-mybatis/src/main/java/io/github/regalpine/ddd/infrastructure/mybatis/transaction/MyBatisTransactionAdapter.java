package io.github.regalpine.ddd.infrastructure.mybatis.transaction;

import io.github.regalpine.ddd.infrastructure.exception.PersistenceAccessException;
import io.github.regalpine.ddd.infrastructure.mybatis.session.MyBatisSessionAdapter;
import io.github.regalpine.ddd.transaction.TransactionAdapter;
import io.github.regalpine.ddd.transaction.TransactionDefinition;

import java.util.Objects;

/**
 * MyBatis-based transaction adapter.
 *
 * <p>Phase XII §13: manages transaction boundaries through the MyBatis
 * session lifecycle. The adapter opens a session on {@link #begin},
 * commits on {@link #commit}, and rolls back on {@link #rollback}.</p>
 *
 * <p>Phase XII §14: the repository must NOT commit its own transaction.
 * All commit/rollback decisions are made by this adapter.</p>
 *
 * @author RegalPine
 */
public final class MyBatisTransactionAdapter implements TransactionAdapter {

    private final MyBatisSessionAdapter sessionAdapter;

    public MyBatisTransactionAdapter(MyBatisSessionAdapter sessionAdapter) {
        this.sessionAdapter = Objects.requireNonNull(sessionAdapter, "sessionAdapter must not be null");
    }

    @Override
    public void begin(TransactionDefinition definition) {
        Objects.requireNonNull(definition, "definition must not be null");
        if (sessionAdapter.hasCurrentSession()) {
            throw new IllegalStateException("Transaction already active on current thread");
        }
        sessionAdapter.openSession();
    }

    @Override
    public void commit() {
        var session = sessionAdapter.currentSession();
        if (session == null) {
            throw new IllegalStateException("No active transaction to commit");
        }
        try {
            session.commit();
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (Exception rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw new PersistenceAccessException("Failed to commit MyBatis transaction", e);
        } finally {
            sessionAdapter.closeSession();
        }
    }

    @Override
    public void rollback() {
        var session = sessionAdapter.currentSession();
        if (session == null) {
            return;
        }
        try {
            session.rollback();
        } catch (Exception e) {
            throw new PersistenceAccessException("Failed to rollback MyBatis transaction", e);
        } finally {
            sessionAdapter.closeSession();
        }
    }

    @Override
    public boolean isActive() {
        return sessionAdapter.hasCurrentSession();
    }
}
