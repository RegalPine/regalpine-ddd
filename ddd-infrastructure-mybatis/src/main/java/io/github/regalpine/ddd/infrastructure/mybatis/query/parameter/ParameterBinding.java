package io.github.regalpine.ddd.infrastructure.mybatis.query.parameter;

/**
 * An immutable binding of a named parameter to a value with type information.
 *
 * <p>Phase XIII §30: all query values must go through parameter binding (§31).
 * Direct SQL string concatenation with user values is strictly forbidden.</p>
 *
 * <p>Example generated SQL:</p>
 * <pre>
 * WHERE status = #{p1}
 * AND customer_id = #{p2}
 * </pre>
 *
 * @param name     the parameter name (e.g. "p1")
 * @param value    the bound value
 * @param javaType the Java type of the value
 * @author RegalPine
 */
public record ParameterBinding(
        String name,
        Object value,
        Class<?> javaType
) {
}
