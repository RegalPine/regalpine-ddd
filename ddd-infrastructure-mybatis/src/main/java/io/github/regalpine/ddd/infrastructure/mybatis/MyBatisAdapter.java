package io.github.regalpine.ddd.infrastructure.mybatis;

/**
 * Marker class for the MyBatis adapter module.
 *
 * <p>Phase XII §5: identifies the adapter boundary between the
 * RegalPine DDD Framework and MyBatis infrastructure.</p>
 *
 * <p>Dependency direction (Phase XII §2):</p>
 * <pre>{@code
 * ddd-infrastructure
 *       ↑
 * ddd-infrastructure-mybatis  ← you are here
 *       ↑
 * MyBatis
 * }</pre>
 *
 * @author RegalPine
 */
public final class MyBatisAdapter {

    private MyBatisAdapter() {
        // marker class
    }
}
