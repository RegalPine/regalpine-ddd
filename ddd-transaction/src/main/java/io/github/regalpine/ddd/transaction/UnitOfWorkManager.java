package io.github.regalpine.ddd.transaction;

/**
 * Manages the lifecycle of Unit of Work instances.
 *
 * @author RegalPine
 */
public interface UnitOfWorkManager {

    /**
     * Creates a new Unit of Work.
     *
     * @return the new Unit of Work
     */
    UnitOfWork begin();

    /**
     * Returns the current active Unit of Work, if any.
     *
     * @return the current Unit of Work, or null if none is active
     */
    UnitOfWork current();

    /**
     * Completes the current Unit of Work (commit or rollback).
     */
    void complete();

    /**
     * Rolls back the current Unit of Work.
     */
    void rollback();

    /**
     * Returns whether there is an active Unit of Work.
     *
     * @return {@code true} if a Unit of Work is currently active
     */
    boolean hasCurrent();

    /**
     * Executes the given callback within a managed Unit of Work.
     *
     * <p>Phase XI §50: begins a Unit of Work, executes the callback,
     * commits on success, rolls back on exception.</p>
     *
     * @param callback the business logic to execute
     * @param <T>      the result type
     * @return the callback result
     */
    default <T> T execute(UnitOfWorkCallback<T> callback) {
        UnitOfWork uow = begin();
        try {
            T result = callback.execute();
            uow.commit();
            return result;
        } catch (Exception e) {
            uow.rollback();
            throw e;
        }
    }
}
