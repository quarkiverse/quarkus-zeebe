package io.quarkiverse.zeebe.runtime.metrics;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;

public class MicroprofileMetricsRecorder implements MetricsRecorder {

    private final MetricRegistry metricRegistry;

    public MicroprofileMetricsRecorder(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void increase(String name, String action, String type) {
        metricRegistry
                .counter(name, new Tag("action", action), new Tag("type", type))
                .inc();
    }

    @Override
    public void executeWithTimer(String name, Runnable method) {

    }
}
