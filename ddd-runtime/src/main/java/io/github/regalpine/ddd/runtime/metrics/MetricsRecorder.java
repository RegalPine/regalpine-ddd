package io.github.regalpine.ddd.runtime.metrics;

import java.time.Duration;

/**
 * Records runtime metrics such as command counts, durations, and failures.
 *
 * <p>Provides a simple API for incrementing counters and recording durations.
 * Implementations may delegate to Micrometer, OpenTelemetry, or other
 * metrics backends.</p>
 *
 * @author RegalPine
 */
public interface MetricsRecorder {

    /**
     * Increments a named counter by one.
     *
     * @param name the metric name (e.g. "command.dispatched", "transaction.failure")
     */
    void increment(String name);

    /**
     * Records a duration for a named metric.
     *
     * @param name     the metric name (e.g. "command.duration", "transaction.duration")
     * @param duration the measured duration
     */
    void record(String name, Duration duration);
}
