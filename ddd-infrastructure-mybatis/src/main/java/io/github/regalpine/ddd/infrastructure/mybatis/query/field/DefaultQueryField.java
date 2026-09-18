package io.github.regalpine.ddd.infrastructure.mybatis.query.field;

import java.util.Objects;

/**
 * Default immutable implementation of {@link QueryField}.
 *
 * <p>Phase XIII §5: validates that column is non-blank and javaType is non-null.</p>
 *
 * @param column   the database column name
 * @param javaType the Java type of the field value
 * @param <T>      the Java type of the field value
 * @author RegalPine
 */
public record DefaultQueryField<T>(
        String column,
        Class<T> javaType
) implements QueryField<T> {

    public DefaultQueryField {
        if (column == null || column.isBlank()) {
            throw new IllegalArgumentException("column must not be blank");
        }
        Objects.requireNonNull(javaType, "javaType must not be null");
        if (column.contains(";") || column.contains("--") || column.contains("'")
                || column.contains("\"") || column.contains("/*") || column.contains("*/")) {
            throw new IllegalArgumentException(
                    "column must not contain SQL metacharacters: " + column);
        }
    }
}
