package io.quarkiverse.zeebe.runtime.metrics;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.micrometer.core.instrument.MeterRegistry;

@ApplicationScoped
public class MicrometerMetricsRecorder implements MetricsRecorder {
    @Inject
    MeterRegistry registry;

    @Override
    public void increase(String name, String action, String type) {
        registry.counter(name, "action", action, "type", type).increment();
    }

    @Override
    public void executeWithTimer(String name, Runnable method) {

    }
}
