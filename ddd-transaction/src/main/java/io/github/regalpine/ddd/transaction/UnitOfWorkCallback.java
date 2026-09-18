package io.github.regalpine.ddd.transaction;

/**
 * Callback for execution within a Unit of Work boundary.
 *
 * <p>Phase XI §50: UnitOfWorkCallback is used with
 * {@link UnitOfWorkManager#execute(UnitOfWorkCallback)} to run
 * business logic inside a managed Unit of Work.</p>
 *
 * @param <T> the result type
 * @author RegalPine
 */
@FunctionalInterface
public interface UnitOfWorkCallback<T> {

    /**
     * Executes within a Unit of Work context.
     *
     * @return the result
     */
    T execute();
}
