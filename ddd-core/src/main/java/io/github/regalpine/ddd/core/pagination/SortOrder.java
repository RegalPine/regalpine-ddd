package io.github.regalpine.ddd.core.pagination;

import java.util.Objects;

/**
 * Defines a sort order for a single property.
 *
 * <p>Phase XII §17: used within {@link PageRequest} to specify
 * the ordering of paginated query results.</p>
 *
 * @param property  the property name to sort by, must not be blank
 * @param direction the sort direction (ASC or DESC)
 * @author RegalPine
 */
public record SortOrder(
        String property,
        Direction direction
) {

    public SortOrder {
        if (property == null || property.isBlank()) {
            throw new IllegalArgumentException("property must not be blank");
        }
        Objects.requireNonNull(direction, "direction must not be null");
    }

    /**
     * Sort direction.
     */
    public enum Direction {
        ASC,
        DESC
    }
}
