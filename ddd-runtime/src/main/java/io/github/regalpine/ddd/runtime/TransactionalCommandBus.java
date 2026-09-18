package io.github.regalpine.ddd.runtime;

import io.github.regalpine.ddd.application.command.Command;
import io.github.regalpine.ddd.cqrs.bus.CommandBus;
import io.github.regalpine.ddd.transaction.TransactionManager;

import java.util.Objects;

/**
 * A command bus decorator that wraps command dispatch within a transaction.
 *
 * <p>This is a transactional command bus wrapper: it ensures that command
 * execution occurs within a transaction boundary managed by the
 * {@link TransactionManager}.</p>
 *
 * <p><strong>Limitation:</strong> Domain event dispatch after commit is not
 * handled by this class. Event collection and dispatch should be implemented
 * via CQRS middleware, a {@link io.github.regalpine.ddd.transaction.TransactionSynchronization},
 * or external orchestration.</p>
 *
 * @author RegalPine
 */
public final class TransactionalCommandBus implements CommandBus {

    private final CommandBus delegate;
    private final TransactionManager transactionManager;

    public TransactionalCommandBus(
            CommandBus delegate,
            TransactionManager transactionManager) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.transactionManager = Objects.requireNonNull(transactionManager, "transactionManager must not be null");
    }

    @Override
    public <R> R dispatch(Command<R> command) {
        Objects.requireNonNull(command, "command must not be null");
        return transactionManager.execute(() -> delegate.dispatch(command));
    }
}
