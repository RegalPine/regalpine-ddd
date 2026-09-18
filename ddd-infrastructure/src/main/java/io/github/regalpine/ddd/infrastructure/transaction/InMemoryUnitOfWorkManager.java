package io.github.regalpine.ddd.infrastructure.transaction;

import io.github.regalpine.ddd.transaction.UnitOfWork;
import io.github.regalpine.ddd.transaction.UnitOfWorkManager;

import java.util.UUID;

/**
 * In-memory implementation of {@link UnitOfWorkManager} for testing and development.
 *
 * @author RegalPine
 */
public final class InMemoryUnitOfWorkManager implements UnitOfWorkManager {

    private final ThreadLocal<UnitOfWork> current = new ThreadLocal<>();

    @Override
    public UnitOfWork begin() {
        InMemoryUnitOfWork uow = new InMemoryUnitOfWork(UUID.randomUUID().toString());
        current.set(uow);
        return uow;
    }

    @Override
    public UnitOfWork current() {
        return current.get();
    }

    @Override
    public void complete() {
        UnitOfWork uow = current.get();
        if (uow != null) {
            current.remove();
        }
    }

    @Override
    public void rollback() {
        UnitOfWork uow = current.get();
        if (uow != null) {
            uow.rollback();
            current.remove();
        }
    }

    @Override
    public boolean hasCurrent() {
        UnitOfWork uow = current.get();
        return uow != null && uow.isActive();
    }
}
