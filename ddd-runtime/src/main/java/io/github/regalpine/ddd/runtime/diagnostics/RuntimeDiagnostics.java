package io.github.regalpine.ddd.runtime.diagnostics;

/**
 * Provides diagnostic information about the runtime state.
 *
 * <p>Used for runtime debugging, monitoring, and operational insight.
 * Diagnostics must be authorized before access in production environments.</p>
 *
 * <p>The snapshot includes:</p>
 * <ul>
 *   <li>Runtime lifecycle state</li>
 *   <li>Component states</li>
 *   <li>Overall health</li>
 *   <li>Timestamp</li>
 * </ul>
 *
 * @author RegalPine
 */
public interface RuntimeDiagnostics {

    /**
     * Returns a point-in-time snapshot of the runtime state.
     *
     * @return the runtime snapshot
     */
    RuntimeSnapshot snapshot();
}
