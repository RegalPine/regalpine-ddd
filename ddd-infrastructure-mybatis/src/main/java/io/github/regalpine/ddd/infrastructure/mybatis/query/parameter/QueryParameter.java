package io.github.regalpine.ddd.infrastructure.mybatis.query.parameter;

/**
 * A named query parameter with value and type information.
 *
 * <p>Phase XIII §30: represents a single parameter in a query.</p>
 *
 * @param name     the parameter name (e.g. "p1", "p2")
 * @param value    the parameter value
 * @param javaType the Java type of the value
 * @author RegalPine
 */
public record QueryParameter(
        String name,
        Object value,
        Class<?> javaType
) {
}
