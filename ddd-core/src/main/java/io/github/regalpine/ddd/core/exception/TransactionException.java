package io.github.regalpine.ddd.core.exception;

/**
 * Exception thrown when a transaction-related error occurs.
 *
 * <p>Phase XI §111: Transaction-level framework exception.</p>
 *
 * @author RegalPine
 */
public class TransactionException extends FrameworkException {

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
