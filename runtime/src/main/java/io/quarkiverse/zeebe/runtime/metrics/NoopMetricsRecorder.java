package io.quarkiverse.zeebe.runtime.metrics;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NoopMetricsRecorder implements MetricsRecorder {

    @Override
    public void increase(String name, String action, String type) {

    }

    @Override
    public void executeWithTimer(String name, Runnable method) {
        method.run();
    }

}
