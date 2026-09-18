package io.github.regalpine.ddd.infrastructure.mybatis.query.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mutable collector for {@link ParameterBinding} instances.
 *
 * <p>Phase XIII §30: during SQL translation, each value is registered here
 * and receives an auto-generated parameter name (p1, p2, ...). The resulting
 * bindings are then included in the {@code TranslationResult}.</p>
 *
 * @author RegalPine
 */
public final class ParameterBindings {

    private final List<ParameterBinding> bindings = new ArrayList<>();

    /**
     * Adds a value and returns the created binding.
     *
     * @param value    the parameter value
     * @param javaType the Java type
     * @return the created parameter binding
     */
    public ParameterBinding add(Object value, Class<?> javaType) {
        String name = "p" + (bindings.size() + 1);
        var binding = new ParameterBinding(name, value, javaType);
        bindings.add(binding);
        return binding;
    }

    /**
     * Returns an unmodifiable view of all bindings.
     */
    public List<ParameterBinding> toList() {
        return Collections.unmodifiableList(bindings);
    }

    /**
     * Returns the number of bindings.
     */
    public int size() {
        return bindings.size();
    }
}
