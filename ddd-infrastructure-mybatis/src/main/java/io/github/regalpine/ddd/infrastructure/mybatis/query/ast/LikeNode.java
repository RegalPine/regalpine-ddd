package io.github.regalpine.ddd.infrastructure.mybatis.query.ast;

import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;

/**
 * AST node for LIKE / NOT LIKE operations.
 *
 * <p>Phase XIII §16: supports ANYWHERE (%value%), START (value%), END (%value) modes.
 * LIKE values must be properly escaped (§17) to prevent {@code %}, {@code _}, {@code \}
 * from being interpreted as wildcards.</p>
 *
 * @param field    the string query field
 * @param value    the LIKE pattern value
 * @param mode     the LIKE matching mode
 * @param negated  false for LIKE, true for NOT LIKE
 * @author RegalPine
 */
public record LikeNode(
        QueryField<String> field,
        String value,
        LikeMode mode,
        boolean negated
) implements ConditionNode {

    /**
     * LIKE matching modes.
     */
    public enum LikeMode {
        /** %value% */
        ANYWHERE,
        /** value% */
        START,
        /** %value */
        END
    }
}
