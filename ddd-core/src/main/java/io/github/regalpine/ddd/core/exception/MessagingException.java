package io.github.regalpine.ddd.core.exception;

/**
 * Exception thrown when a messaging-related error occurs.
 *
 * <p>Phase XI §111: Messaging-level framework exception.</p>
 *
 * @author RegalPine
 */
public class MessagingException extends FrameworkException {

    public MessagingException(String message) {
        super(message);
    }

    public MessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}
