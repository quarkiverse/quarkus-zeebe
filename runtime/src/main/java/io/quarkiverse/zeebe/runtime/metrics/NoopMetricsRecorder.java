package io.quarkiverse.zeebe.runtime.metrics;

import javax.inject.Singleton;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;

@Singleton
@Unremovable
@DefaultBean
public class NoopMetricsRecorder implements MetricsRecorder {

    @Override
    public void increase(String name, String action, String type) {

    }

    @Override
    public void executeWithTimer(String name, Runnable method) {
        method.run();
    }

}
