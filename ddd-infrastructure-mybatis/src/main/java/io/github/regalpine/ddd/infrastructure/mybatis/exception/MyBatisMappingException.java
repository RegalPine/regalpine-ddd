package io.github.regalpine.ddd.infrastructure.mybatis.exception;

/**
 * Thrown when a mapping error occurs between domain objects
 * and MyBatis persistence records.
 *
 * <p>Phase XII §5: covers errors in {@code AggregateMapper},
 * {@code DomainEventMapper}, and {@code ValueObjectMapper}.</p>
 *
 * @author RegalPine
 */
public class MyBatisMappingException extends MyBatisAdapterException {

    public MyBatisMappingException(String message) {
        super(message);
    }

    public MyBatisMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
