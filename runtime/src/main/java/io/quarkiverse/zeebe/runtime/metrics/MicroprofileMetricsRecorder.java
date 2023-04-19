package io.quarkiverse.zeebe.runtime.metrics;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;

@ApplicationScoped
public class MicroprofileMetricsRecorder implements MetricsRecorder {

    @Inject
    MetricRegistry metricRegistry;

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
