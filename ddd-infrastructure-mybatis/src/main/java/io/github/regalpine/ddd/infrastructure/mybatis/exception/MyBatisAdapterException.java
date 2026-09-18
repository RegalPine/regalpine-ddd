package io.github.regalpine.ddd.infrastructure.mybatis.exception;

import io.github.regalpine.ddd.infrastructure.exception.InfrastructureException;

/**
 * Base exception for MyBatis adapter errors.
 *
 * <p>Phase XII §5: all MyBatis-specific exceptions derive from this base,
 * which in turn extends the framework's {@link InfrastructureException}.</p>
 *
 * @author RegalPine
 */
public class MyBatisAdapterException extends InfrastructureException {

    public MyBatisAdapterException(String message) {
        super(message);
    }

    public MyBatisAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
