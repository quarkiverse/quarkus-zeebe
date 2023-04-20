package io.quarkiverse.zeebe.runtime.tracing;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;

import io.camunda.zeebe.client.api.response.ActivatedJob;

@ApplicationScoped
public class DefaultTracingRecorder implements TracingRecorder {

    private static final TracingRecorder.TracingContext CONTEXT = new TracingContext() {

        @Override
        public void error(String key, Throwable t) {

        }

        @Override
        public void close() {

        }

        @Override
        public void ok() {

        }
    };

    @Override
    public Collection<String> fields() {
        return null;
    }

    @Override
    public TracingContext createTracingContext(String clazz, String method, String name, ActivatedJob job) {
        return CONTEXT;
    }
}
