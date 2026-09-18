package io.github.regalpine.ddd.runtime;

import java.util.Set;

/**
 * Describes a runtime component and its dependencies.
 *
 * <p>The runtime uses this descriptor to build a dependency DAG
 * and determine the startup order.</p>
 *
 * @author RegalPine
 */
public interface RuntimeComponentDescriptor {

    /**
     * Returns the stable name of the component.
     *
     * @return the component name
     */
    String name();

    /**
     * Returns the names of components this component depends on.
     *
     * @return the set of dependency names
     */
    Set<String> dependencies();
}
